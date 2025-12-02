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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.selects.select
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.repository.CompanyRepository
import sfu.cmpt362.android_ezcredit.data.viewmodel.CompanyViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.CompanyViewModelFactory
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.ReceiptViewModel
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import sfu.cmpt362.android_ezcredit.utils.PdfUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReceiptEntryScreen(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    receiptViewModel: ReceiptViewModel,
    receiptId: Long?,
    isViewing: Boolean,
    onBack: () -> Unit
) {
    if(isViewing) {
        ReceiptViewScreen(invoiceViewModel, customerViewModel, receiptViewModel, receiptId, onBack)
    } else {
        ReceiptAdd(invoiceViewModel, customerViewModel, receiptViewModel, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReceiptAdd(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    receiptViewModel: ReceiptViewModel,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var receiptNumber by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var localIssueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.invDate) }
    var localDueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.dueDate) }
    val currentCustomerName by invoiceViewModel.customerName.collectAsState()
    var invoiceSearchQuery by rememberSaveable { mutableStateOf("") }

    val invoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())
    val customers by customerViewModel.customersLiveData.observeAsState(emptyList())

    SetupUI(
        title = "Add New Receipt",
        subtitle = "Fill in the receipt details below",
        receiptNumber = receiptNumber,
        onReceiptNumberChange = { receiptNumber = it },
        invoiceSearchQuery = invoiceSearchQuery,
        onInvoiceSearchQueryChange = {
            invoiceSearchQuery = it
            invoiceViewModel.selectedInvoice = null
        },
        selectedInvoice = invoiceViewModel.selectedInvoice,
        onInvoiceSelect = {
            invoiceViewModel.selectedInvoice = it;
            amountText = it.amount.toString();
            invoiceViewModel.loadCustomerName(it.id)
        },
        onInvoiceClear = {
            invoiceViewModel.selectedInvoice = null
            invoiceSearchQuery = ""
        },
        invoices = invoices,
        localIssueDate = localIssueDate,
        onIssueDateChange = { localIssueDate = it },
        localDueDate = localDueDate,
        onDueDateChange = { localDueDate = it },

        amountText = amountText,
        onAmountChange = { amountText = it },
        customerName = currentCustomerName,
        onCustomerNameChange = {  },
        isEditable = true,
        showEditButton = false,
        showDeleteButton = false,
        onSave = {
            val selectedInvoice= invoiceViewModel.selectedInvoice
            if (receiptNumber.isBlank() || amountText.isBlank()){
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@SetupUI
            }
            if (selectedInvoice == null) {
                Toast.makeText(context, "Please enter a valid invoice", Toast.LENGTH_SHORT).show()
                return@SetupUI
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(context, "Amount must be a number", Toast.LENGTH_SHORT).show()
                return@SetupUI
            }
            if (selectedInvoice.invDate.toUtcStartOfDayMillis() > localIssueDate.toUtcStartOfDayMillis()) {
                Toast.makeText(context, "Due date cannot be earlier than issue date", Toast.LENGTH_SHORT).show()
                return@SetupUI
            }

            receiptViewModel.updateReceipt(0, receiptNumber,
                localIssueDate, selectedInvoice.id
            )
            receiptViewModel.insert()

            Toast.makeText(context, "Receipt added", Toast.LENGTH_SHORT).show()
            invoiceViewModel.clearCustomerName()
            invoiceViewModel.selectedInvoice = null
            invoiceSearchQuery = ""
            onBack()

        },
        onCancel = {
            invoiceViewModel.clearCustomerName()
            invoiceViewModel.selectedInvoice = null
            invoiceSearchQuery = ""
            onBack()
        },
        onDelete = {},
        context = context
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReceiptViewScreen(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    receiptViewModel: ReceiptViewModel,
    receiptId: Long?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var receiptNumber by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var localIssueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.invDate) }
    var localDueDate by rememberSaveable { mutableStateOf(invoiceViewModel.invoice.dueDate) }
    val currentCustomerName by invoiceViewModel.customerName.collectAsState()
    var invoiceSearchQuery by rememberSaveable { mutableStateOf("") }

    val invoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())
    val customers by customerViewModel.customersLiveData.observeAsState(emptyList())

    val receipt by receiptViewModel.currentReceipt.collectAsState()

    LaunchedEffect(receiptId) {
        receiptId?.let { receiptViewModel.loadReceipt(it) }
    }

    if (receipt == null) {CircularProgressIndicator()} else {
        val currentReceipt = receipt!!
        val selectedInvoice = invoices.find { it.id == currentReceipt.invoiceId }
        val currentCustomer = customers.find { it.id == selectedInvoice?.customerId }

        SetupUI(
            title = "Receipt Information",
            subtitle = "",
            receiptNumber = currentReceipt.receiptNumber,
            onReceiptNumberChange = { },
            invoiceSearchQuery = selectedInvoice?.invoiceNumber ?: "ID: ${currentReceipt.invoiceId}",
            onInvoiceSearchQueryChange = { },
            selectedInvoice = selectedInvoice,
            onInvoiceSelect = {},
            onInvoiceClear = { },
            invoices = invoices,
            localIssueDate = currentReceipt.receiptDate,
            onIssueDateChange = { localIssueDate = it },
            localDueDate = localDueDate,
            onDueDateChange = { localDueDate = it },

            amountText = selectedInvoice?.amount.toString(),
            onAmountChange = { amountText = it },
            customerName = currentCustomer?.name,
            onCustomerNameChange = { },
            isEditable = false,
            showEditButton = false,
            showDeleteButton = false,
            onSave = { },
            onCancel = {
                onBack()
            },
            onDelete = {},
            context = context
        )
    }
}
// Common UI setup for both modes
// Each mode passes the details that UI must show in that mode
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupUI(
    title: String,
    subtitle: String,
    receiptNumber: String,
    onReceiptNumberChange: (String) -> Unit,
    invoiceSearchQuery: String,
    onInvoiceSearchQueryChange: (String) -> Unit,
    selectedInvoice: Invoice?,
    onInvoiceSelect: (Invoice) -> Unit,
    onInvoiceClear: () -> Unit,
    invoices: List<Invoice>,
    localIssueDate: Calendar,
    onIssueDateChange: (Calendar) -> Unit,
    localDueDate: Calendar,
    onDueDateChange: (Calendar) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    customerName: String?,
    onCustomerNameChange: (String?) -> Unit,
    isEditable: Boolean,
    showEditButton: Boolean,
    context: Context,
    onEditToggle: () -> Unit = {},
    showDeleteButton: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var showInvoiceDropdown by rememberSaveable { mutableStateOf(false) }
    var expandedStatus by rememberSaveable { mutableStateOf(false) }
    var showIssueDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDueDatePicker by rememberSaveable { mutableStateOf(false) }

    val filteredInvoices = remember(invoiceSearchQuery, invoices) {
        if (invoiceSearchQuery.isBlank()) emptyList()
        else invoices.filter { it.invoiceNumber.contains(invoiceSearchQuery, ignoreCase = true) }
            .take(5)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (title == "Receipt Information" && selectedInvoice != null) {

                val companyRepository = remember {
                    val db = AppDatabase.getInstance(context)
                    CompanyRepository(db.companyDao)
                }
                val companyViewModel: CompanyViewModel = viewModel(
                    factory = CompanyViewModelFactory(companyRepository)
                )

                var companyName by remember { mutableStateOf("EZCredit") }

                LaunchedEffect(Unit) {
                    val companyId = CompanyContext.currentCompanyId
                    if (companyId != null) {
                        val company = companyViewModel.getCompanyById(companyId)
                        companyName = company.name
                    }
                }
                Button(
                    onClick = {
                        PdfUtils.generateReceiptPdf(
                            context = context,
                            receiptNumber = receiptNumber,
                            invoiceNumber = selectedInvoice.invoiceNumber,
                            amount = amountText,
                            issueDate = localIssueDate.time.toString(),
                            companyName = companyName
                        )
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Generate PDF")
                }
            }
        }

        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Invoice Number
        OutlinedTextField(
            value = receiptNumber,
            onValueChange = onReceiptNumberChange,
            label = { Text("Receipt Number") },
            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
            singleLine = true,
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        )

        // Invoice Dropdown
        ExposedDropdownMenuBox(
            expanded = showInvoiceDropdown && filteredInvoices.isNotEmpty(),
            onExpandedChange = { showInvoiceDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedInvoice?.invoiceNumber ?: invoiceSearchQuery,
                onValueChange = { onInvoiceSearchQueryChange(it); showInvoiceDropdown = true },
                label = { Text("Invoice Number") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingIcon = {
                    if (selectedInvoice != null) {
                        IconButton(onClick = onInvoiceClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                enabled = isEditable,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showInvoiceDropdown && filteredInvoices.isNotEmpty(),
                onDismissRequest = { showInvoiceDropdown = false }
            ) {
                filteredInvoices.forEach { invoice ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        invoice.invoiceNumber,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "ID: ${invoice.id}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    invoice.amount.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onInvoiceSelect(invoice)
                            showInvoiceDropdown = false
                        }
                    )
                }
            }
        }

        // Dates
        DateField("Issue Date", localIssueDate, dateFormat, isEditable) {
            showIssueDatePicker = true
        }

        // CustomerName
        OutlinedTextField(
            value = customerName ?: "",
            onValueChange = onCustomerNameChange,
            label = { Text("Customer") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            singleLine = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Amount
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            singleLine = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )


        Spacer(modifier = Modifier.height(4.dp))

        // Buttons
        if (isEditable) {
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Save Receipt", style = MaterialTheme.typography.titleMedium)
            }
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(if (isEditable) "Cancel" else "Back", style = MaterialTheme.typography.titleMedium)
        }
        if (showDeleteButton) {
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Delete", style = MaterialTheme.typography.titleMedium)
            }
        }
        val forIssueDate = true
        // Date Pickers
        if (showIssueDatePicker) {
            DatePickerDialog(
                issueDatePickerState,
                onIssueDateChange,
                { showIssueDatePicker = false },
                null,
                forIssueDate
            )
        }
        if (showDueDatePicker) {
            DatePickerDialog(
                dueDatePickerState,
                onDueDateChange,
                { showDueDatePicker = false },
                localIssueDate,
                !forIssueDate
            )
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