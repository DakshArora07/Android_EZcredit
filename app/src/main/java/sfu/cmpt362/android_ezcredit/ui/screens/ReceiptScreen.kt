package sfu.cmpt362.android_ezcredit.ui.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.ReceiptViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    invoiceViewModel: InvoiceViewModel,
    invoiceScreenViewModel: InvoiceScreenViewModel,
    customerViewModel: CustomerViewModel,
    receiptViewModel: ReceiptViewModel,
    onViewAddReceipt: (receiptID:Long) -> Unit
) {


    var textFieldFocusState by remember { mutableStateOf(true) }
    val invoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())
    var filterListExpanded by rememberSaveable { mutableStateOf(false) }
    var clearFilters by rememberSaveable { mutableStateOf(false) }

    var sortInvoicesByDueDateToday by rememberSaveable { mutableStateOf(false) }
    var showStatusDropdown by rememberSaveable { mutableStateOf(false) }
    var selectedStatus by rememberSaveable { mutableStateOf("") }
    val customers = customerViewModel.customersLiveData.value  ?: emptyList()

    // Search functionality states
    var receiptSearchQuery by rememberSaveable { mutableStateOf("") } //Refactor this later
    val selectedReceipt by invoiceScreenViewModel.customerFilter.collectAsState()



    val invoiceMap = remember(invoices) { invoices.associateBy { it.id } }




    // Receipts constansts
    val receipts by receiptViewModel.receiptsLiveData.observeAsState(emptyList())



    // Filter receipts based on input

    val filteredReceipts = if (receiptSearchQuery.isNotEmpty()) {
        receipts.filter {
            it.receiptNumber.contains(receiptSearchQuery, ignoreCase = true) ||
                    it.receiptNumber.contains(receiptSearchQuery, ignoreCase = true)
        }
    } else {
        receipts
    }

    // set mutable list to receipts

    receiptViewModel.defReceiptOrSorted = filteredReceipts

    // Filter invoices by selected customer




    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.receipts),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.receiptScreenSubHeading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                FloatingActionButton(onClick = { onViewAddReceipt(-1L) }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Invoice")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Field
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            OutlinedTextField(
                value = receiptSearchQuery,
                onValueChange = { query ->
                    receiptSearchQuery = query
                    if (selectedReceipt != null) {
                        invoiceScreenViewModel.setCustomerFilter(null)
                    }

                },
                label = { Text("Search by Receipt Number") },
                placeholder = { Text("Type receipt number") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (selectedReceipt != null || receiptSearchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            receiptSearchQuery = ""
                            invoiceScreenViewModel.setCustomerFilter(null)

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

            Spacer(modifier = Modifier.height(28.dp))

            // Receipts / Empty state
            // This Box fills remaining space using weight
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
                if (receiptViewModel.defReceiptOrSorted.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No receipts yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add your first receipt to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(receiptViewModel.defReceiptOrSorted) { receipt ->
                            val relatedInvoice = invoiceMap[receipt.invoiceId] ?: return@items
                            ReceiptCard(
                                receipt = receipt,
                                invoice = relatedInvoice,
                                onClick = { onViewAddReceipt(receipt.id) }
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ReceiptCard( receipt: Receipt, invoice: Invoice, onClick: () -> Unit) {
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
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Receipt Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "Receipt #${receipt.receiptNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Invoice #${invoice.invoiceNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Issued: ${dateFormat.format(receipt.receiptDate.time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


        }
    }
}
