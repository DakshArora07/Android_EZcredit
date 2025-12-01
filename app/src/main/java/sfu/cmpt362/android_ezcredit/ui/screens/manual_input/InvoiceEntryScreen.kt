package sfu.cmpt362.android_ezcredit.ui.screens.manual_input

import android.content.Context
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.InvoiceScreenViewModel
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import sfu.cmpt362.android_ezcredit.utils.PdfUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InvoiceEntryScreen(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    invoiceId: Long,
    ocrResult: InvoiceScreenViewModel.OcrInvoiceResult? = null,
    onBack: () -> Unit) {
    val context = LocalContext.current

    // Check for the modes:
    // Add mode: id == -1
    // View/Edit mode: id >= 0
    if (invoiceId >= 0L) {
        ViewEditInvoiceScreen(invoiceViewModel, customerViewModel, invoiceId, onBack)
    } else {
        AddInvoiceScreen(invoiceViewModel, customerViewModel, ocrResult, onBack)
    }
}

// Add Screen Setup
// Display empty views of OCR data (if any)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddInvoiceScreen(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    ocrResult: InvoiceScreenViewModel.OcrInvoiceResult?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var invoiceNumber by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var localIssueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.invDate) }
    var localDueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.dueDate) }
    var selectedStatus by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.status) }
    var customerSearchQuery by rememberSaveable { mutableStateOf("") }

    val customers by customerViewModel.customersLiveData.observeAsState(emptyList())

    LaunchedEffect(ocrResult) {
        ocrResult?.let {
            invoiceNumber = it.invoiceNumber ?: ""
            amountText = it.amount ?: ""
            customerSearchQuery = it.customerName ?: ""
            it.issueDate?.let { date -> localIssueDate = parseDate(date) }
            it.dueDate?.let { date -> localDueDate = parseDate(date) }
        }
    }

    SetupUIViews(
        isEditMode = false,
        context = context,
        title = "Add New Invoice",
        subtitle = "Fill in the invoice details below",
        invoiceNumber = invoiceNumber,
        onInvoiceNumberChange = { invoiceNumber = it },
        customerSearchQuery = customerSearchQuery,
        onCustomerSearchQueryChange = {
            customerSearchQuery = it
            customerViewModel.customerFromUserInputOnAddMode = null
        },
        selectedCustomer = customerViewModel.customerFromUserInputOnAddMode,
        onCustomerSelect = { customerViewModel.customerFromUserInputOnAddMode = it },
        onCustomerClear = {
            customerViewModel.customerFromUserInputOnAddMode = null
            customerSearchQuery = ""
        },
        customers = customers,
        localIssueDate = localIssueDate,
        onIssueDateChange = { localIssueDate = it },
        localDueDate = localDueDate,
        onDueDateChange = { localDueDate = it },
        amountText = amountText,
        onAmountChange = { amountText = it },
        selectedStatus = selectedStatus,
        onStatusChange = { selectedStatus = it },
        isEditable = true,
        showEditButton = false,
        showDeleteButton = false,
        onSave = {
            val selectedCustomer = customerViewModel.customerFromUserInputOnAddMode
            if (invoiceNumber.isBlank() || amountText.isBlank()){
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@SetupUIViews
            }
            if (selectedCustomer == null) {
                Toast.makeText(context, "Please enter a valid customer", Toast.LENGTH_SHORT).show()
                return@SetupUIViews
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(context, "Amount must be a number", Toast.LENGTH_SHORT).show()
                return@SetupUIViews
            }
            invoiceViewModel.updateAmountText(amountText)
            invoiceViewModel.updateInvoice(0, invoiceNumber, selectedCustomer.id,
                localIssueDate, localDueDate, amount, selectedStatus)
            invoiceViewModel.insert()

            val updatedCredit = if (selectedStatus == InvoiceStatus.Unpaid || selectedStatus == InvoiceStatus.PastDue) {
                selectedCustomer.credit + amount
            } else selectedCustomer.credit

            customerViewModel.update(selectedCustomer.copy(
                credit = updatedCredit))

            Toast.makeText(context, "Invoice added", Toast.LENGTH_SHORT).show()
            onBack()


        },
        onCancel = onBack,
        onDelete = {}
    )
}

// View/Edit Screen Setup
// Displays the invoice details from database
// Keeps track of view and edit button clicks
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ViewEditInvoiceScreen(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    invoiceId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var allowToEdit by rememberSaveable { mutableStateOf(false) }
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var invoiceNumber by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var selectedStatus: InvoiceStatus by rememberSaveable { mutableStateOf(InvoiceStatus.Unpaid)}
    var localIssueDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    var localDueDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    var customerSearchQuery by rememberSaveable { mutableStateOf("") }

    val customers by customerViewModel.customersLiveData.observeAsState(emptyList())
    var hasLoadedFromDb by rememberSaveable { mutableStateOf(false) }

    if (!hasLoadedFromDb) {
        LaunchedEffect(invoiceId) {
            val fetchedInvoice = invoiceViewModel.getInvoiceById(invoiceId)
            invoice = fetchedInvoice
            invoiceNumber = fetchedInvoice.invoiceNumber
            amountText = fetchedInvoice.amount.toString()
            selectedStatus = fetchedInvoice.status
            localIssueDate = fetchedInvoice.invDate
            localDueDate = fetchedInvoice.dueDate

            val fetchedCustomer = customerViewModel.getCustomerById(fetchedInvoice.customerId)
            customerViewModel.customerFromDB = fetchedCustomer
            customerSearchQuery = fetchedCustomer.name

            hasLoadedFromDb = true
        }
    }

    SetupUIViews(
        isEditMode = true,
        context = context,
        title = "Update Invoice",
        subtitle = "Edit the invoice details below",
        invoiceNumber = invoiceNumber,
        onInvoiceNumberChange = { invoiceNumber = it },
        customerSearchQuery = customerSearchQuery,
        onCustomerSearchQueryChange = {
            customerSearchQuery = it
            customerViewModel.customerFromDB = null
        },
        selectedCustomer = customerViewModel.customerFromDB,
        onCustomerSelect = { customerViewModel.customerFromDB = it },
        onCustomerClear = {
            customerViewModel.customerFromDB = null
            customerSearchQuery = ""
        },
        customers = customers,
        localIssueDate = localIssueDate,
        onIssueDateChange = { localIssueDate = it },
        localDueDate = localDueDate,
        onDueDateChange = { localDueDate = it },
        amountText = amountText,
        onAmountChange = { amountText = it },
        selectedStatus = selectedStatus,
        onStatusChange = { selectedStatus = it },
        isEditable = allowToEdit,
        showEditButton = true,
        onEditToggle = { allowToEdit = !allowToEdit },
        showDeleteButton = allowToEdit,
        onSave = {
            val selectedCustomer = customerViewModel.customerFromDB
            val newAmount = amountText.toDoubleOrNull()

            if (invoiceNumber.isBlank() || newAmount == null) {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@SetupUIViews
            }
            if (selectedCustomer == null) {
                Toast.makeText(context, "Please enter a valid customer", Toast.LENGTH_SHORT).show()
                return@SetupUIViews
            }

            val oldAmount = invoice?.amount ?: 0.0
            val amountDifference = newAmount - oldAmount
            if ((selectedStatus == InvoiceStatus.Unpaid || selectedStatus == InvoiceStatus.PastDue) && amountDifference != 0.0) {
                customerViewModel.update(selectedCustomer.copy(credit = selectedCustomer.credit + amountDifference))
            }

            invoiceViewModel.updateInvoice(invoiceId, invoiceNumber, selectedCustomer.id,
                localIssueDate, localDueDate, newAmount, selectedStatus)
            invoiceViewModel.update()
            Toast.makeText(context, "Invoice updated", Toast.LENGTH_SHORT).show()
            onBack()
        },
        onCancel = onBack,
        onDelete = {
            invoiceViewModel.delete(invoiceId)
            Toast.makeText(context, "Invoice deleted", Toast.LENGTH_SHORT).show()
            onBack()
        }
    )
}

// Common UI setup for both modes
// Each mode passes the details that UI must show in that mode
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupUIViews(
    isEditMode: Boolean,
    context: Context,
    title: String,
    subtitle: String,
    invoiceNumber: String,
    onInvoiceNumberChange: (String) -> Unit,
    customerSearchQuery: String,
    onCustomerSearchQueryChange: (String) -> Unit,
    selectedCustomer: Customer?,
    onCustomerSelect: (Customer) -> Unit,
    onCustomerClear: () -> Unit,
    customers: List<Customer>,
    localIssueDate: Calendar,
    onIssueDateChange: (Calendar) -> Unit,
    localDueDate: Calendar,
    onDueDateChange: (Calendar) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    selectedStatus: InvoiceStatus,
    onStatusChange: (InvoiceStatus) -> Unit,
    isEditable: Boolean,
    showEditButton: Boolean,
    onEditToggle: () -> Unit = {},
    showDeleteButton: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var showCustomerDropdown by rememberSaveable { mutableStateOf(false) }
    var expandedStatus by rememberSaveable { mutableStateOf(false) }
    var showIssueDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDueDatePicker by rememberSaveable { mutableStateOf(false) }

    val filteredCustomers = remember(customerSearchQuery, customers) {
        if (customerSearchQuery.isBlank()) emptyList()
        else customers.filter { it.name.contains(customerSearchQuery, ignoreCase = true) }.take(5)
    }

    val issueDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localIssueDate.toUtcStartOfDayMillis()
    )
    val dueDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localDueDate.toUtcStartOfDayMillis()
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        if (showEditButton) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                Button(onClick = onEditToggle, shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        } else {
            Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        }
        Button(onClick = {
            PdfUtils.generateInvoicePdf(
                context = context,
                invoiceNumber = invoiceNumber,
                customerName = selectedCustomer?.name ?: "Unknown",
                amount = amountText,
                issueDate = localIssueDate.time.toString(),
                dueDate = localDueDate.time.toString(),
                status = selectedStatus.name
            )
        }){
            Text("Generate PDF", style = MaterialTheme.typography.titleMedium)
        }

        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Invoice Number
        OutlinedTextField(
            value = invoiceNumber,
            onValueChange = onInvoiceNumberChange,
            label = { Text("Invoice Number") },
            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
            singleLine = true,
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        )

        // Customer Dropdown
        ExposedDropdownMenuBox(
            expanded = showCustomerDropdown && filteredCustomers.isNotEmpty(),
            onExpandedChange = { showCustomerDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedCustomer?.name ?: customerSearchQuery,
                onValueChange = onCustomerSearchQueryChange,
                label = { Text("Customer Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingIcon = {
                    if (selectedCustomer != null) {
                        IconButton(onClick = onCustomerClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                enabled = isEditable,
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
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(customer.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("ID: ${customer.id}", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                Text(customer.email, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        onClick = {
                            onCustomerSelect(customer)
                            showCustomerDropdown = false
                        }
                    )
                }
            }
        }

        // Dates
        DateField("Issue Date", localIssueDate, dateFormat, isEditable) { showIssueDatePicker = true }
        DateField("Due Date", localDueDate, dateFormat, isEditable) { showDueDatePicker = true }

        // Amount
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = { Text("Invoice Total") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            singleLine = true,
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        if (!isEditMode){
            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus.name,
                    onValueChange = {},
                    enabled = isEditable,
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
                    InvoiceStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                onStatusChange(status)
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Buttons
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Save Invoice", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Cancel", style = MaterialTheme.typography.titleMedium)
        }
        if (showDeleteButton) {
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Delete", style = MaterialTheme.typography.titleMedium)
            }
        }
        val forIssueDate=true
        // Date Pickers
        if (showIssueDatePicker) {
            DatePickerDialog(issueDatePickerState, onIssueDateChange,{ showIssueDatePicker = false }, null, forIssueDate )
        }
        if (showDueDatePicker) {
            DatePickerDialog(dueDatePickerState, onDueDateChange,  { showDueDatePicker = false }, localIssueDate, !forIssueDate)
        }
    }
}

@Composable
private fun DateField(
    label: String,
    date: Calendar,
    dateFormat: SimpleDateFormat,
    enabled: Boolean,
    onEditClick: () -> Unit
) {
    OutlinedTextField(
        value = dateFormat.format(date.time),
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Select Date")
            }
        },
        readOnly = true,
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DatePickerDialog(
    state: DatePickerState,
    onDateChange: (Calendar) -> Unit,
    onDismiss: () -> Unit,
    localIssueDate: Calendar?,
    forIssueDate:Boolean
) {
    val context = LocalContext.current
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val selectedCalender =millisUtcToLocalDate(millis).toLocalMidnightCalendar()
                    if(forIssueDate){
                        onDateChange(selectedCalender)
                    }
                    if(!forIssueDate && localIssueDate!=null){
                        if(selectedCalender.before(localIssueDate)){
                            Toast.makeText(context, "Due date cannot be earlier than issue date", Toast.LENGTH_SHORT).show()
                            state.selectedDateMillis = localIssueDate.toUtcStartOfDayMillis()
                        }else{
                            onDateChange(selectedCalender)
                        }
                    }
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = state)
    }
}

// Helper functions
private fun parseDate(dateStr: String): Calendar {
    val parts = dateStr.split("-")
    return if (parts.size == 3) {
        Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
    } else Calendar.getInstance()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun Calendar.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.toLocalMidnightCalendar(): Calendar =
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
private fun Calendar.toUtcStartOfDayMillis(): Long =
    toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

@RequiresApi(Build.VERSION_CODES.O)
private fun millisUtcToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()