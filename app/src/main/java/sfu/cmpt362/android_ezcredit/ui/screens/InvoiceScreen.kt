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
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import java.util.Locale

@Composable
fun InvoiceScreen(
    invoiceViewModel: InvoiceViewModel,
    invoiceScreenViewModel: InvoiceScreenViewModel = viewModel(),
    onAddInvoice: (invoiceId:Long) -> Unit,
    onScanCompleted: (InvoiceScreenViewModel.OcrInvoiceResult) -> Unit
) {
    val OPEN_IN_EDIT_MODE:Boolean = false
    val context = LocalContext.current
    val cameraRequest by invoiceScreenViewModel.cameraRequest.collectAsState()
    val showDialog by invoiceScreenViewModel.showDialog.collectAsState()
    val invoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())

    val ocrResult by invoiceScreenViewModel.ocrResult.collectAsState()

    LaunchedEffect(ocrResult) {
        ocrResult?.let { result ->
            onScanCompleted(result)

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
            
            if (invoices.isEmpty()) {
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
                    items(invoices) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onClick = {
                                onAddInvoice(invoice.id)
                                Toast.makeText(
                                    context,
                                    "Invoice #${invoice.invoiceNumber} clicked",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    "Paid" -> MaterialTheme.colorScheme.primaryContainer
                    "Unpaid" -> MaterialTheme.colorScheme.secondaryContainer
                    "PastDue" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = when (invoice.status) {
                        "PastDue" -> "Past Due"
                        else -> invoice.status
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (invoice.status) {
                        "Paid" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "Unpaid" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "PastDue" -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}