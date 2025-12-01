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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.ui.viewmodel.InvoiceScreenViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    invoiceViewModel: InvoiceViewModel,
    invoiceScreenViewModel: InvoiceScreenViewModel,
    customerViewModel: CustomerViewModel,
    onAddInvoice: (invoiceId:Long) -> Unit,
    onScanCompleted: (InvoiceScreenViewModel.OcrInvoiceResult) -> Unit
) {
    var textFieldFocusState by remember { mutableStateOf(true) }
    val invoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())
    var filterListExpanded by rememberSaveable { mutableStateOf(false) }
    var clearFilters by rememberSaveable { mutableStateOf(false) }

    var sortInvoicesByDueDateToday by rememberSaveable { mutableStateOf(false) }
    var showStatusDropdown by rememberSaveable { mutableStateOf(false) }
    var  selectedStatus by rememberSaveable { mutableStateOf("") }
    val customers = customerViewModel.customersLiveData.value

    // Search functionality states
    var customerSearchQuery by rememberSaveable { mutableStateOf("") }
    val selectedCustomer by invoiceScreenViewModel.customerFilter.collectAsState()

    var showCustomerDropdown by rememberSaveable { mutableStateOf(false) }

    // Filter customers based on search query
    val filteredCustomers = if (customerSearchQuery.isNotEmpty() && selectedCustomer == null) {
        customers.filter {
            it.name.contains(customerSearchQuery, ignoreCase = true) ||
                    it.email.contains(customerSearchQuery, ignoreCase = true)
        }
    } else {
        emptyList()
    }

    // Filter invoices by selected customer
    var filteredInvoices = if (selectedCustomer != null) {
        invoices.filter { it.customerID == selectedCustomer!!.id }
    } else {
        invoices
    }
    filteredInvoices = if (selectedStatus.isNotBlank()) {
        filteredInvoices.filter {
            when (selectedStatus) {
                "Paid" -> it.status == InvoiceStatus.Paid
                "Unpaid" -> it.status == InvoiceStatus.Unpaid
                "Past Due" -> it.status == InvoiceStatus.PastDue
                "Late Payment" -> it.status == InvoiceStatus.LatePayment
                else -> true
            }
        }
    } else {
        filteredInvoices
    }

    // Filter by due date today
    filteredInvoices = if (sortInvoicesByDueDateToday) {
        filteredInvoices.filter { invoice ->
            val today = android.icu.util.Calendar.getInstance().apply {
                set(android.icu.util.Calendar.HOUR_OF_DAY, 0)
                set(android.icu.util.Calendar.MINUTE, 0)
                set(android.icu.util.Calendar.SECOND, 0)
                set(android.icu.util.Calendar.MILLISECOND, 0)
            }

            val dueDate = invoice.dueDate.clone() as android.icu.util.Calendar
            dueDate.set(android.icu.util.Calendar.HOUR_OF_DAY, 0)
            dueDate.set(android.icu.util.Calendar.MINUTE, 0)
            dueDate.set(android.icu.util.Calendar.SECOND, 0)
            dueDate.set(android.icu.util.Calendar.MILLISECOND, 0)

            dueDate.timeInMillis == today.timeInMillis
        }
    } else {
        filteredInvoices
    }

    // Sort Invoices
    invoiceViewModel.defInvoicesOrSorted = filteredInvoices


    val context = LocalContext.current
    val cameraRequest by invoiceScreenViewModel.cameraRequest.collectAsState()
    val showDialog by invoiceScreenViewModel.showDialog.collectAsState()


    val ocrResult by invoiceScreenViewModel.ocrResult.collectAsState()

    LaunchedEffect(ocrResult) {
        ocrResult?.let { result ->
            onScanCompleted(result)
            invoiceScreenViewModel.clearOcrResult()
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

                Box {
                    FloatingActionButton(onClick = { filterListExpanded = true }) {
                        Icon(
                            Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = filterListExpanded,
                        onDismissRequest = { filterListExpanded = false }
                    ) {
                        DropdownMenuItem(

                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Sort By Status")
                                    if (selectedStatus.isNotEmpty()) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                   },
                            onClick = {
                                filterListExpanded = false
                                showStatusDropdown=true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                Text("Sort By Due Date Today")
                                if (sortInvoicesByDueDateToday) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                   }
                                   },
                            onClick = {
                                sortInvoicesByDueDateToday = !sortInvoicesByDueDateToday
                                filterListExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Clear Filters")
                                    if (clearFilters) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                clearFilters = !clearFilters
                                filterListExpanded = false
                                invoiceScreenViewModel.setCustomerFilter(null)
                                sortInvoicesByDueDateToday=false
                                selectedStatus=""
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false },
                    ) {
                        listOf("Paid", "Unpaid", "Past Due", "Late Payment").forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(status)
                                    if (selectedStatus == status) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } },

                                onClick = {
                                    selectedStatus = status
                                    showStatusDropdown = false
                                    filterListExpanded = false
                                }
                            )
                        }
                    }
                }


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

            Spacer(modifier = Modifier.height(16.dp))


// Customer Search Input Field
            Box(modifier = Modifier.fillMaxWidth()) {
                val focusRequester = remember { FocusRequester() }

                // Auto-focus when the screen loads
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

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
                                // Re-focus after clearing
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

                // CUSTOM DROPDOWN - No more DropdownMenu!
                if (showCustomerDropdown && filteredCustomers.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp) // Position below text field
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
                                            // Re-focus after selection
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
                                // Add divider between items (except last one)
                                if (customer != filteredCustomers.last()) {
                                    Divider(
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

            // Show active filter chip
            if (selectedCustomer != null) {
                FilterChip(
                    selected = true,
                    onClick = {
                        invoiceScreenViewModel.setCustomerFilter(null)
                        customerSearchQuery = ""
                    },
                    label = { Text("Filtering: ${selectedCustomer!!.name}") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear filter",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
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
                            text = "No invoices yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add your first invoice to get started",
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
                        InvoiceCard(
                            invoice = invoice,
                            onClick = {
                                onAddInvoice(invoice.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice, onClick: () -> Unit) {
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

            // Status badge
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
//
//             Customer Search Input Field
//            ExposedDropdownMenuBox(
//                expanded = showCustomerDropdown && filteredCustomers.isNotEmpty(),
//                onExpandedChange = {  showCustomerDropdown = it },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                val focusRequester = remember { FocusRequester() }
//                LaunchedEffect(Unit) {
//                    focusRequester.requestFocus()
//                }
//                OutlinedTextField(
//                    value =  customerSearchQuery,
//                    onValueChange = { query ->
//                        customerSearchQuery = query
//                        if (selectedCustomer != null) {
//                            invoiceScreenViewModel.setCustomerFilter(null)
//                        }
////                        showCustomerDropdown = query.isNotEmpty()
//                        showCustomerDropdown = query.isNotEmpty() && filteredCustomers.isNotEmpty()
//                    },
//                    label = { Text("Search by Customer") },
//                    placeholder = { Text("Type customer name") },
//                    leadingIcon = {
//                        Icon(Icons.Default.Search, contentDescription = "Search")
//                    },
//                    trailingIcon = {
//                        if (selectedCustomer != null || customerSearchQuery.isNotEmpty()) {
//                            IconButton(onClick = {
//                                customerSearchQuery = ""
//                                invoiceScreenViewModel.setCustomerFilter(null)
//                                showCustomerDropdown = false
//                            }) {
//                                Icon(Icons.Default.Clear, contentDescription = "Clear")
//                            }
//                        }
//                    },
//                    singleLine = true,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .menuAnchor()
//                )
//
//                ExposedDropdownMenu(
//                    expanded = showCustomerDropdown && filteredCustomers.isNotEmpty(),
//                    onDismissRequest = { showCustomerDropdown = false }
//                ) {
//                    filteredCustomers.forEach { customer ->
//                        DropdownMenuItem(
//                            text = {
//                                Column {
//                                    Row(
//                                        Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(
//                                            customer.name,
//                                            style = MaterialTheme.typography.bodyLarge
//                                        )
//                                        Text(
//                                            "ID: ${customer.id}",
//                                            style = MaterialTheme.typography.bodySmall,
//                                            color = MaterialTheme.colorScheme.primary
//                                        )
//                                    }
//                                    Text(
//                                        customer.email,
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                }
//                            },
//                            onClick = {
//                                invoiceScreenViewModel.setCustomerFilter(customer)
//                                customerSearchQuery = customer.name
//                                showCustomerDropdown = false
//                            }
//                        )
//                    }
//                }
//            }