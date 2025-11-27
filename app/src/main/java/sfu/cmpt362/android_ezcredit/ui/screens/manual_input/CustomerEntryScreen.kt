package sfu.cmpt362.android_ezcredit.ui.screens.manual_input

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel

@Composable
fun CustomerEntryScreen(
    customerViewModel: CustomerViewModel,
    invoiceViewModel: InvoiceViewModel,
    customerId: Long,
    onBack: () -> Unit) {

    // Mode check:
    // Add mode: id == -1
    // View/Edit mode: id >= 0
    if (customerId >= 0L) {
        ViewEditCustomerScreen(customerViewModel, invoiceViewModel, customerId, onBack)
    } else {
        AddCustomerScreen(customerViewModel, onBack)
    }
}

// Add mode
// Calls the SetupUIViews with empty views
@Composable
fun AddCustomerScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    SetupUIViews(
        title = "Add New Customer",
        subtitle = "Fill in the customer details below",
        name = name,
        onNameChange = { name = it },
        email = email,
        onEmailChange = { email = it },
        phone = phone,
        onPhoneChange = { phone = it },
        isEditable = true,
        showEditButton = false,
        showDeleteButton = false,
        onSave = {
            if (name.isBlank() || email.isBlank() || phone.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@SetupUIViews
            }

            val capitalizedName = name.split(" ")
                .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }

            viewModel.updateCustomer(-1, capitalizedName, email, phone)
            viewModel.insert()
            Toast.makeText(context, "Customer added", Toast.LENGTH_SHORT).show()
            onBack()
        },
        onCancel = onBack,
        onDelete = {}
    )
}

// View/Edit mode
// Loads customer entry from database
// Calls SetupViews with the customer details displayed
@Composable
fun ViewEditCustomerScreen(
    viewModel: CustomerViewModel,
    invoiceViewModel: InvoiceViewModel,
    customerId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var allowToEdit by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var hasLoadedFromDb by rememberSaveable { mutableStateOf(false) }

    if (!hasLoadedFromDb) {
        LaunchedEffect(customerId) {
            val selectedCustomer = viewModel.getCustomerById(customerId)
            name = selectedCustomer.name
            email = selectedCustomer.email
            phone = selectedCustomer.phoneNumber
            hasLoadedFromDb = true
        }
    }

    SetupUIViews(
        title = "Update Customer",
        subtitle = "Edit the customer details below",
        name = name,
        onNameChange = { name = it },
        email = email,
        onEmailChange = { email = it },
        phone = phone,
        onPhoneChange = { phone = it },
        isEditable = allowToEdit,
        showEditButton = true,
        onEditToggle = { allowToEdit = !allowToEdit },
        showDeleteButton = allowToEdit,
        onSave = {
            if (name.isBlank() || email.isBlank() || phone.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@SetupUIViews
            }

            val capitalizedName = name.split(" ")
                .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }

            val customer = viewModel.updateCustomer(customerId, capitalizedName, email, phone)
            viewModel.update(customer)
            Toast.makeText(context, "Customer updated", Toast.LENGTH_SHORT).show()
            onBack()
        },
        onCancel = onBack,
        onDelete = {
            CoroutineScope(Dispatchers.IO).launch {
                val invoices = invoiceViewModel.getInvoicesByCustomerId(customerId)
                if (invoices.isNotEmpty()) {
                    withContext(Dispatchers.Main){Toast.makeText(
                        context,
                        "Please delete all the customer invoices before deleting the customer",
                        Toast.LENGTH_SHORT
                    ).show()
                    }

                } else {
                    viewModel.delete(customerId)
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Customer deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                }
            }
        }
    )
}

@Composable
private fun SetupUIViews(
    title: String,
    subtitle: String,
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    isEditable: Boolean,
    showEditButton: Boolean,
    onEditToggle: () -> Unit = {},
    showDeleteButton: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        if (showEditButton) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = onEditToggle,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth()
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Phone
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            enabled = isEditable,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Buttons
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Save Customer", style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Cancel", style = MaterialTheme.typography.titleMedium)
        }

        if (showDeleteButton) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Delete", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}