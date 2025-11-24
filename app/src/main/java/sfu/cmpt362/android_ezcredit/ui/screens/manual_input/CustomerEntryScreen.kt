package sfu.cmpt362.android_ezcredit.ui.screens.manual_input

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel

@Composable
fun CustomerEntryScreen(
    viewModel: CustomerViewModel,
    invoiceViewModel: InvoiceViewModel,
    customerId:Long,
    onBack: () -> Unit
) {
    var ALLOW_TO_EDIT by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val IS_EDIT_MODE = customerId>=0
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    var customerNameFromDB by rememberSaveable { mutableStateOf("") }
    var customerEmailFromDB by rememberSaveable { mutableStateOf("") }
    var customerPhoneFromDB by rememberSaveable { mutableStateOf("") }
    var hasLoadedFromDb by rememberSaveable { mutableStateOf(false) }
    if(IS_EDIT_MODE && !hasLoadedFromDb){
        LaunchedEffect(customerId) {
            viewModel.getCustomerById(customerId) { fetchedCustomer ->
                customerNameFromDB= fetchedCustomer.name
                customerEmailFromDB = fetchedCustomer.email
                customerPhoneFromDB = fetchedCustomer.phoneNumber
            }
            hasLoadedFromDb=true

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if(IS_EDIT_MODE){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Update Customer",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = {ALLOW_TO_EDIT= !ALLOW_TO_EDIT},
                    shape = MaterialTheme.shapes.medium){
                    Icon(Icons.Default.Edit, contentDescription = null,tint = MaterialTheme.colorScheme.onPrimary)

                }
            }
        }else{
            Text(
                text = "Add New Customer",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }


        Text(
            text = if(IS_EDIT_MODE )"Edit the customer details below" else "Fill in the customer details below",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = if(IS_EDIT_MODE)customerNameFromDB else name,
            onValueChange = {
                if(IS_EDIT_MODE){
                    customerNameFromDB=it
                }else{
                    name = it
                }
            },
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            enabled = if (IS_EDIT_MODE) ALLOW_TO_EDIT else true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            )
        )

        OutlinedTextField(
            value = if(IS_EDIT_MODE)customerEmailFromDB else email,
            onValueChange = {
                if(IS_EDIT_MODE){
                    customerEmailFromDB=it
                }else{
                    email = it
                }
            },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            enabled = if (IS_EDIT_MODE) ALLOW_TO_EDIT else true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            )
        )

        OutlinedTextField(
            value = if(IS_EDIT_MODE)customerPhoneFromDB else phone,
            onValueChange = {
                if(IS_EDIT_MODE){
                    customerPhoneFromDB=it
                }else{
                    phone = it
                }
            },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            enabled = if (IS_EDIT_MODE) ALLOW_TO_EDIT else true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {

                val currentName = if (IS_EDIT_MODE) customerNameFromDB else name
                val currentEmail = if (IS_EDIT_MODE) customerEmailFromDB else email
                val currentPhone = if (IS_EDIT_MODE) customerPhoneFromDB else phone

                if (currentName.isBlank() || currentEmail.isBlank() || currentPhone.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val capitalizedName = currentName.split(" ")
                    .joinToString(" ") { word ->
                        word.lowercase().replaceFirstChar { it.uppercase() }
                    }

                if (IS_EDIT_MODE) {
                    val customer = viewModel.updateCustomer(customerId,capitalizedName, currentEmail, currentPhone)
                    viewModel.update(customer)
                    Toast.makeText(context, "Customer updated", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateCustomer(customerId,capitalizedName, currentEmail, currentPhone)
                    viewModel.insert()
                    Toast.makeText(context, "Customer added", Toast.LENGTH_SHORT).show()
                }
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = if (IS_EDIT_MODE) ALLOW_TO_EDIT else true,

        ) {
            Text(
                text = "Save Customer",
                style = MaterialTheme.typography.titleMedium
            )
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.titleMedium
            )
        }
        if(IS_EDIT_MODE && ALLOW_TO_EDIT){
            OutlinedButton(
                onClick = {
                    invoiceViewModel.getInvoicesByCustomerId(customerId){ invoices ->
                        if (invoices.isNotEmpty()) {
                            Toast.makeText(context, "Please delete all the customer invoices before deleting the customer", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.delete(customerId)
                            Toast.makeText(context, "Customer Deleted.", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                    },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Delete", style = MaterialTheme.typography.titleMedium)}
        }
    }
}