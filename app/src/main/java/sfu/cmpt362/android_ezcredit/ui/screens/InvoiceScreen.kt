package sfu.cmpt362.android_ezcredit.ui.screens

import android.icu.text.SimpleDateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.ui.viewmodel.InvoiceScreenViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.Dispatchers
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    invoiceViewModel: InvoiceViewModel,
    invoiceScreenViewModel: InvoiceScreenViewModel,
    customerViewModel: CustomerViewModel,
    onAddInvoice: (invoiceId: Long) -> Unit,
    onScanCompleted: (InvoiceScreenViewModel.OcrInvoiceResult) -> Unit
) {
    // Live data and state holders
    val invoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())
    val customers = customerViewModel.customersLiveData.value ?: emptyList()
    val context = LocalContext.current

    var filterListExpanded by rememberSaveable { mutableStateOf(false) }

    // Status filter
    var filterByStatus by rememberSaveable { mutableStateOf(false) }
    var selectedStatuses by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showStatusMenu by rememberSaveable { mutableStateOf(false) }

    // Due date filter
    var filterByDueDate by rememberSaveable { mutableStateOf(false) }
    // store selected date as LocalDate to avoid timezone issues
    var selectedDueDateLocal by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // Customer search/filter
    var customerSearchQuery by rememberSaveable { mutableStateOf("") }
    val selectedCustomer by invoiceScreenViewModel.customerFilter.collectAsState()
    var showCustomerDropdown by rememberSaveable { mutableStateOf(false) }

    // Filter customers for dropdown
    val filteredCustomers = if (customerSearchQuery.isNotEmpty() && selectedCustomer == null) {
        customers.filter {
            it.name.contains(customerSearchQuery, ignoreCase = true) ||
                    it.email.contains(customerSearchQuery, ignoreCase = true)
        }
    } else {
        emptyList()
    }

    // Apply filters to invoices
    var filteredInvoices = invoices

    // Customer filter
    if (selectedCustomer != null) {
        filteredInvoices = filteredInvoices.filter { it.customerId == selectedCustomer!!.id }
    }

    // Status filter
    if (filterByStatus && selectedStatuses.isNotEmpty()) {
        filteredInvoices = filteredInvoices.filter { invoice ->
            selectedStatuses.any { status ->
                when (status) {
                    "Paid" -> invoice.status == InvoiceStatus.Paid
                    "Unpaid" -> invoice.status == InvoiceStatus.Unpaid
                    "Past Due" -> invoice.status == InvoiceStatus.PastDue
                    "Late Payment" -> invoice.status == InvoiceStatus.LatePayment
                    else -> false
                }
            }
        }
    }

    // Due date filter - FIX: compare LocalDate values to avoid timezone shift
    if (filterByDueDate && selectedDueDateLocal != null) {
        filteredInvoices = filteredInvoices.filter { invoice ->
            val invoiceLocalDate = Instant.ofEpochMilli(invoice.dueDate.timeInMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            invoiceLocalDate == selectedDueDateLocal
        }
    }

    // Update invoiceViewModel's defInvoicesOrSorted (keep original behavior)
    invoiceViewModel.defInvoicesOrSorted = filteredInvoices

    // Camera / OCR state from viewmodel
    val cameraRequest by invoiceScreenViewModel.cameraRequest.collectAsState()
    val showDialog by invoiceScreenViewModel.showDialog.collectAsState()
    val ocrResult by invoiceScreenViewModel.ocrResult.collectAsState()

    // When OCR result available, callback and clear
    LaunchedEffect(ocrResult) {
        ocrResult?.let { result ->
            onScanCompleted(result)
            invoiceScreenViewModel.clearOcrResult()
        }
    }

    // Permission and camera launchers
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            invoiceScreenViewModel.onScanInvoiceOptionClicked()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { invoiceScreenViewModel.onBitmapCaptured(it) }
        invoiceScreenViewModel.onCameraHandled()
    }

    if (cameraRequest) {
        cameraLauncher.launch(null)
    }

    // Date Picker Dialog handling: NOTE - DatePicker returns an instant representing UTC midnight of chosen date.
    if (showDatePicker) {
        val initialMillis = selectedDueDateLocal?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
            ?.toEpochMilli() ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // dpState.selectedDateMillis is typically UTC midnight for chosen date.
                    // Interpret it as a UTC date to extract the chosen calendar date,
                    // then compare with invoice LocalDate (systemDefault) when filtering.
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val chosenDateUtc = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        // store chosen date (calendar date) as LocalDate — compares correctly against invoice LocalDate
                        selectedDueDateLocal = chosenDateUtc
                        filterByDueDate = true
                    } else {
                        selectedDueDateLocal = null
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header row: title, filter FAB, add invoice FAB & menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.invoices),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.invoiceScreenSubHeading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Filters and add button area
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Filter FAB
                    Box {
                        FloatingActionButton(onClick = { filterListExpanded = true }) {
                            Icon(
                                Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Main Filter Menu
                        DropdownMenu(
                            expanded = filterListExpanded,
                            onDismissRequest = { filterListExpanded = false }
                        ) {

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Icon(
                                                Icons.Default.FilterList,
                                                contentDescription = "Status options available",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Filter By Status")
                                        }
                                        if (selectedStatuses.isNotEmpty()) {
                                            Badge { Text("${selectedStatuses.size}") }
                                        }
                                    }
                                },
                                onClick = {
                                    filterByStatus = true
                                    showStatusMenu = true
                                    filterListExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Checkbox(
                                                checked = filterByDueDate,
                                                onCheckedChange = {
                                                    filterByDueDate = it
                                                    if (!it) selectedDueDateLocal = null
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Filter By Due Date")
                                        }
                                        if (filterByDueDate && selectedDueDateLocal != null) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    filterByDueDate = true
                                    showDatePicker = true
                                    filterListExpanded = false
                                }
                            )

                            Divider()

                            DropdownMenuItem(
                                text = { Text("Clear All Filters") },
                                onClick = {
                                    filterByStatus = false
                                    selectedStatuses = emptySet()
                                    filterByDueDate = false
                                    selectedDueDateLocal = null
                                    invoiceScreenViewModel.setCustomerFilter(null)
                                    customerSearchQuery = ""
                                    filterListExpanded = false
                                }
                            )
                        }

                        // Status Filter Submenu
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            Text(
                                text = "Select Statuses",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Divider()

                            listOf("Paid", "Unpaid", "Past Due", "Late Payment").forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = selectedStatuses.contains(status),
                                                onCheckedChange = {
                                                    selectedStatuses = if (it) {
                                                        selectedStatuses + status
                                                    } else {
                                                        selectedStatuses - status
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(status)
                                        }
                                    },
                                    onClick = {
                                        selectedStatuses = if (selectedStatuses.contains(status)) {
                                            selectedStatuses - status
                                        } else {
                                            selectedStatuses + status
                                        }
                                    }
                                )
                            }

                            Divider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { selectedStatuses = emptySet() }) {
                                    Text("Clear All")
                                }

                                TextButton(onClick = { showStatusMenu = false }) {
                                    Text("Done")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add invoice FAB and menu (Scan/Add)
                    Box {
                        FloatingActionButton(
                            onClick = { invoiceScreenViewModel.onAddInvoiceButtonClicked() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Invoice"
                            )
                        }

                        DropdownMenu(
                            expanded = showDialog,
                            onDismissRequest = { invoiceScreenViewModel.onDialogDismiss() },
                            offset = DpOffset(x = 0.dp, y = 8.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Scan Invoice") },
                                onClick = {
                                    invoiceScreenViewModel.onDialogDismiss()
                                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Manually") },
                                onClick = {
                                    onAddInvoice(-1L)
                                    invoiceScreenViewModel.onDialogDismiss()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Search Input Field
            Box(modifier = Modifier.fillMaxWidth()) {
                val focusRequester = remember { FocusRequester() }
                OutlinedTextField(
                    value = customerSearchQuery,
                    onValueChange = { query ->
                        customerSearchQuery = query
                        if (selectedCustomer != null) {
                            invoiceScreenViewModel.setCustomerFilter(null)
                        }
                        showCustomerDropdown = query.isNotEmpty()
                    },
                    label = { Text("Search by Customer") },
                    placeholder = { Text("Type customer name") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (selectedCustomer != null || customerSearchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                customerSearchQuery = ""
                                invoiceScreenViewModel.setCustomerFilter(null)
                                showCustomerDropdown = false
                                focusRequester.requestFocus()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                if (showCustomerDropdown && filteredCustomers.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp)
                            .heightIn(max = 200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredCustomers) { customer ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            invoiceScreenViewModel.setCustomerFilter(customer)
                                            customerSearchQuery = customer.name
                                            showCustomerDropdown = false
                                            focusRequester.requestFocus()
                                        },
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                customer.name,
                                                style = MaterialTheme.typography.bodyMedium
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
                                }
                                if (customer != filteredCustomers.last()) {
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedCustomer != null) {
                    FilterChip(
                        selected = true,
                        onClick = {
                            invoiceScreenViewModel.setCustomerFilter(null)
                            customerSearchQuery = ""
                        },
                        label = { Text("Customer: ${selectedCustomer!!.name}") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear filter",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                if (filterByStatus && selectedStatuses.isNotEmpty()) {
                    FilterChip(
                        selected = true,
                        onClick = { showStatusMenu = true },
                        label = { Text("Status (${selectedStatuses.size})") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    filterByStatus = false
                                    selectedStatuses = emptySet()
                                },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear filter",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }

                if (filterByDueDate && selectedDueDateLocal != null) {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    // Format the selectedDueDateLocal to a string similar to DateFormat
                    val labelText = try {
                        val instant = selectedDueDateLocal!!.atStartOfDay(ZoneId.systemDefault()).toInstant()
                        dateFormat.format(java.util.Date.from(instant))
                    } catch (e: Exception) {
                        selectedDueDateLocal.toString()
                    }

                    FilterChip(
                        selected = true,
                        onClick = { showDatePicker = true },
                        label = { Text("Due: $labelText") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear filter",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Invoices list or empty-state
            if (invoiceViewModel.defInvoicesOrSorted.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No invoices found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (selectedCustomer != null || filterByStatus || filterByDueDate) {
                                "Try adjusting your filters"
                            } else {
                                "Add your first invoice to get started"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(invoiceViewModel.defInvoicesOrSorted) { invoice ->

                        var customerName by remember { mutableStateOf("Loading..") }
                        LaunchedEffect(invoice.id) {
                            customerName = kotlinx.coroutines.withContext(Dispatchers.IO) {
                                invoiceViewModel.getCustomerNameByInvoiceId(invoice.id)
                            }
                        }
                        InvoiceCard(
                            invoice = invoice,
                            customerName = customerName,
                            onClick = { onAddInvoice(invoice.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice, customerName: String = "Unknown", onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Invoice Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "Invoice #${invoice.invoiceNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Billed To: $customerName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Amount: $${String.format("%.2f", invoice.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Due: ${dateFormat.format(invoice.dueDate.time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (invoice.status) {
                    InvoiceStatus.Paid -> MaterialTheme.colorScheme.primaryContainer
                    InvoiceStatus.Unpaid -> MaterialTheme.colorScheme.secondaryContainer
                    InvoiceStatus.PastDue -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = when (invoice.status) {
                        InvoiceStatus.PastDue -> "Past Due"
                        else -> invoice.status.name
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (invoice.status) {
                        InvoiceStatus.Paid -> MaterialTheme.colorScheme.onPrimaryContainer
                        InvoiceStatus.Unpaid -> MaterialTheme.colorScheme.onSecondaryContainer
                        InvoiceStatus.PastDue -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
