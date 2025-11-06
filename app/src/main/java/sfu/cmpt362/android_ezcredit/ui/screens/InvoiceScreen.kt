package sfu.cmpt362.android_ezcredit.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sfu.cmpt362.android_ezcredit.R
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.InvoiceScreenViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Preview
@Composable
fun InvoiceScreen(viewModel: InvoiceScreenViewModel = viewModel()) {

    val context = LocalContext.current
    val cameraRequest by viewModel.cameraRequest.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onAddInvoiceClicked()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.onBitmapCaptured(it, context) }
        viewModel.onCameraHandled()
    }

    if (cameraRequest) {
        cameraLauncher.launch(null)
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(10.dp)
    ) {
        Column {
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
                FloatingActionButton(
                    onClick = {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Invoice"
                    )
                }

            }

            DemoInvoices()
        }
    }
}

@Composable
fun DemoInvoices() {

    val demoInvoices = listOf(
        Triple("ABC Company", "$120", "Nov 05, 2024"),
        Triple("XYZ Enterprise", "$150.49", "Nov 07, 2024")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 20.dp)
    ) {
        items(demoInvoices) { invoice ->
            InvoiceCard(
                title = invoice.first,
                amount = invoice.second,
                date = invoice.third,
                onClick = {
                }
            )
        }
    }
}

@Composable
fun InvoiceCard(title: String, amount: String, date: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium
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
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = "Amount: $amount", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Date: $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
