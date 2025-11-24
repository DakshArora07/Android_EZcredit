package sfu.cmpt362.android_ezcredit.ui.screens.manual_input

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.InvoiceScreenViewModel
import sfu.cmpt362.android_ezcredit.utils.CreditScoreCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEntryScreen(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    invoiceId: Long,
    ocrResult: InvoiceScreenViewModel.OcrInvoiceResult? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val IS_EDIT_MODE = invoiceId >= 0
    val coroutineScope = rememberCoroutineScope()
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var customer by remember { mutableStateOf<Customer?>(null) }
    var invoiceIDFromDB: Long by rememberSaveable { mutableStateOf(-1) }
    var invoiceNumberFromDB by rememberSaveable { mutableStateOf("") }
    var invoiceTotalFromDB by rememberSaveable { mutableStateOf("") }
    var selectedStatusFromDB by rememberSaveable { mutableStateOf("") }
    var localIssueDateFromDB by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    var localDueDateFromDB by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    val selectedCustomerFromDB = customerViewModel.customerFromDB
    var selectedCustomerId by rememberSaveable { mutableStateOf<Long?>(null) }

    var customerSearchQuery by rememberSaveable { mutableStateOf("") }

    var hasLoadedFromDb by rememberSaveable { mutableStateOf(false) }
    if(IS_EDIT_MODE && !hasLoadedFromDb){
        LaunchedEffect(invoiceId) {
            invoiceViewModel.getInvoiceById(invoiceId) { fetchedInvoice ->
                invoice = fetchedInvoice
                invoiceIDFromDB = fetchedInvoice.id
                invoiceNumberFromDB = fetchedInvoice.invoiceNumber
                invoiceTotalFromDB = fetchedInvoice.amount.toString()
                selectedStatusFromDB = fetchedInvoice.status
                localIssueDateFromDB  = fetchedInvoice.invDate
                localDueDateFromDB = fetchedInvoice.dueDate
                val id:Long = fetchedInvoice.customerID
                customerViewModel.getCustomerById(id){fetchedCustomer ->
                    customer = fetchedCustomer
                    customerViewModel.customerFromDB = fetchedCustomer
                    selectedCustomerId = fetchedCustomer.id
                    customerSearchQuery = fetchedCustomer.name
                }
            }
            hasLoadedFromDb=true
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    var invoiceNumber by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }

    var localIssueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.invDate) }
    var localDueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.dueDate) }

    var expandedStatus by rememberSaveable { mutableStateOf(false) }
    var selectedStatus by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.status) }

    val customers by customerViewModel.customersLiveData.observeAsState(emptyList())
    var selectedCustomer = customers.firstOrNull { it.id == selectedCustomerId }

    var showCustomerDropdown by rememberSaveable { mutableStateOf(false) }

    val filteredCustomers = remember(customerSearchQuery, customers) {
        if (customerSearchQuery.isBlank()) emptyList()
        else customers.filter { it.name.contains(customerSearchQuery, ignoreCase = true) }.take(5)
    }

    var showIssueDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDueDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(ocrResult) {
        ocrResult?.let { result ->
            if (!IS_EDIT_MODE) {
                invoiceNumber = result.invoiceNumber ?: ""
                amountText = result.amount ?: ""
                customerSearchQuery = result.customerName ?: ""

                result.issueDate?.let { dateStr ->
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        val cal = Calendar.getInstance()
                        cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        localIssueDate = cal
                    }
                }
                result.dueDate?.let { dateStr ->
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        val cal = Calendar.getInstance()
                        cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        localDueDate = cal
                    }
                }
            }

        }
    }


    // Helpers: Calendar <-> LocalDate <-> millis (UTC start of day)
    @RequiresApi(Build.VERSION_CODES.O)
    fun Calendar.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.toLocalMidnightCalendar(): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthValue - 1)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.toUtcStartOfDayMillis(): Long =
        atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    @RequiresApi(Build.VERSION_CODES.O)
    fun millisUtcToLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()

    val issueInitialMillis = remember(localIssueDate) {
        localIssueDate.toLocalDate().toUtcStartOfDayMillis()
    }
    val dueInitialMillis = remember(localDueDate) {
        localDueDate.toLocalDate().toUtcStartOfDayMillis()
    }

    val issueDatePickerState = rememberDatePickerState(initialSelectedDateMillis = issueInitialMillis)
    val dueDatePickerState = rememberDatePickerState(initialSelectedDateMillis = dueInitialMillis)

    val statusOptions = listOf("Paid", "Unpaid", "PastDue")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if(IS_EDIT_MODE)"Update Invoice" else "Add New Invoice",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if(IS_EDIT_MODE)"edit the invoice details below" else "Fill in the invoice details below",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Invoice number
        OutlinedTextField(
            value = if(IS_EDIT_MODE) invoiceNumberFromDB else invoiceNumber,
            onValueChange = {if(IS_EDIT_MODE) invoiceNumberFromDB = it else invoiceNumber = it },
            label = { Text("Invoice Number") },
            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Customer dropdown
        ExposedDropdownMenuBox(
            expanded = showCustomerDropdown && filteredCustomers.isNotEmpty(),
            onExpandedChange = {showCustomerDropdown = it}
        ) {
            OutlinedTextField(
                value = if(IS_EDIT_MODE)selectedCustomerFromDB?.name ?: customerSearchQuery else selectedCustomer?.name ?: customerSearchQuery,
                onValueChange = { newValue->
                    customerSearchQuery = newValue
                    if (IS_EDIT_MODE) {
                        customerViewModel.customerFromDB=null
                    } else {
                        selectedCustomer = null
                    }
                },
                label = { Text("Customer Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingIcon = {
                    if (selectedCustomer != null) {
                        IconButton(onClick = {
                            selectedCustomer = null
                            customerSearchQuery = ""
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showCustomerDropdown && filteredCustomers.isNotEmpty(),
                onDismissRequest = { showCustomerDropdown = false }
            ) {
                filteredCustomers.forEach { customer ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        customer.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "ID: ${customer.id}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    customer.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            customerSearchQuery = customer.name
                            if (IS_EDIT_MODE) {
                                customerViewModel.customerFromDB = customer
                                selectedCustomerId = customer.id
                            } else {
                                selectedCustomer = customer
                                selectedCustomerId = customer.id
                            }
                            showCustomerDropdown = false
                        }
                    )
                }
            }
        }

        // Issue date
        OutlinedTextField(
            value = if (IS_EDIT_MODE) dateFormat.format(localIssueDateFromDB.time)
            else dateFormat.format(localIssueDate.time),
            onValueChange = {},
            label = { Text("Issue Date") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showIssueDatePicker = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Select Date")
                }
            },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Due date
        OutlinedTextField(
            value = if (IS_EDIT_MODE) dateFormat.format(localDueDateFromDB.time) else dateFormat.format(localDueDate.time),
            onValueChange = {},
            label = { Text("Due Date") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showDueDatePicker = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Select Date")
                }
            },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Amount
        OutlinedTextField(
            value = if (IS_EDIT_MODE) invoiceTotalFromDB else amountText,
            onValueChange = {  if (IS_EDIT_MODE) {
                invoiceTotalFromDB = it
            } else {
                amountText = it
            }},
            label = { Text("Invoice Total") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Status dropdown - fixed with selectedStatus update and expanded toggle
        ExposedDropdownMenuBox(
            expanded = expandedStatus,
            onExpandedChange = { expandedStatus = it }
        ) {
            OutlinedTextField(
                value = if(IS_EDIT_MODE) selectedStatusFromDB.ifEmpty { "Select Status" } else selectedStatus.ifEmpty { "Select Status" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Status") },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { expandedStatus = !expandedStatus }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedStatus,
                onDismissRequest = { expandedStatus = false }
            ) {
                statusOptions.forEach { statusOption ->
                    DropdownMenuItem(
                        text = { Text(statusOption) },
                        onClick = {
                            if (IS_EDIT_MODE) {
                                selectedStatusFromDB = statusOption
                            } else {
                                selectedStatus = statusOption
                            }
                            expandedStatus = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save button
        Button(
            onClick = {
                if (IS_EDIT_MODE) {
                    val newAmount = invoiceTotalFromDB.toDoubleOrNull()
                    if (invoiceNumberFromDB.isBlank() || newAmount == null || selectedStatusFromDB.isBlank()) {
                        Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedCustomerFromDB == null) {
                        Toast.makeText(context, "Please enter a valid Customer", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val oldAmount = invoice?.amount ?: 0.0
                    val amountDifference = newAmount - oldAmount
                    if ((selectedStatusFromDB == "Unpaid" || selectedStatusFromDB == "PastDue") && amountDifference != 0.0) {
                        selectedCustomerFromDB.let { cust ->
                            val updatedCustomer = cust.copy(
                                credit = cust.credit + amountDifference
                            )
                            customerViewModel.update(updatedCustomer)
                        }
                    }

                    invoiceViewModel.updateInvoice(
                        invoiceIDFromDB,
                        invoiceNumber = invoiceNumberFromDB,
                        customerId = selectedCustomerFromDB?.id ?: 0,
                        issueDate = localIssueDateFromDB,
                        dueDate = localDueDateFromDB,
                        amount = newAmount,
                        status = selectedStatusFromDB
                    )
                    invoiceViewModel.update()
                    Toast.makeText(context, "Invoice updated", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    coroutineScope.launch {
                        if (invoiceNumber.isBlank() ||
                            amountText.isBlank() ||
                            selectedStatus.isBlank()
                        ) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@launch
                        } else if (selectedCustomer == null) {
                            Toast.makeText(context, "Please enter a valid customer name", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val amount = amountText.toDoubleOrNull()
                        if (amount == null) {
                            Toast.makeText(context, "Amount must be a number", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        try {
                            invoiceViewModel.updateAmountText(amountText)
                            invoiceViewModel.updateInvoice(
                                invoiceId,
                                invoiceNumber = invoiceNumber,
                                customerId = selectedCustomer?.id ?: 0,
                                issueDate = localIssueDate,
                                dueDate = localDueDate,
                                amount = amount,
                                status = selectedStatus
                            )

                            withContext(Dispatchers.IO) {
                                invoiceViewModel.insert()
                            }
                            selectedCustomer?.let { customer ->
                                withContext(Dispatchers.IO) {
                                    val updatedCredit = if (selectedStatus == "Unpaid" || selectedStatus == "PastDue") {
                                        customer.credit + amount
                                    } else {
                                        customer.credit
                                    }
                                    val invoices = invoiceViewModel.getInvoicesByCustomerId(customer.id)
                                    val newCreditScore = CreditScoreCalculator.calculateCreditScore(invoices)
                                    val updatedCustomer = customer.copy(
                                        credit = updatedCredit,
                                        creditScore = newCreditScore
                                    )

                                    customerViewModel.update(updatedCustomer)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Invoice added", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            e.printStackTrace()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Save Invoice", style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Cancel", style = MaterialTheme.typography.titleMedium)
        }
    }

    // Issue Date Picker Dialog
    if (showIssueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showIssueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    issueDatePickerState.selectedDateMillis?.let { millis ->
                        val selectedLocalDate = millisUtcToLocalDate(millis)
                        if (IS_EDIT_MODE) {
                            localIssueDateFromDB = selectedLocalDate.toLocalMidnightCalendar()
                        } else {
                            localIssueDate = selectedLocalDate.toLocalMidnightCalendar()
                        }
                    }
                    showIssueDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIssueDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = issueDatePickerState)
        }
    }

    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDatePickerState.selectedDateMillis?.let { millis ->
                        val selectedLocalDate = millisUtcToLocalDate(millis)
                        if (IS_EDIT_MODE)
                            localDueDateFromDB = selectedLocalDate.toLocalMidnightCalendar()
                        else
                            localDueDate = selectedLocalDate.toLocalMidnightCalendar()
                    }
                    showDueDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = dueDatePickerState)
        }
    }
}

