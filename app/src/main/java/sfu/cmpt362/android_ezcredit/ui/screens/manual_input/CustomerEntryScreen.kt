package sfu.cmpt362.android_ezcredit.ui.screens.manual_input

import android.widget.Toast
import androidx.compose.foundation.layout.*
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

@Composable
fun CustomerEntryScreen(
    viewModel: CustomerViewModel,
    customerId:Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val IS_EDIT_MODE = customerId>=0
    val name = viewModel.customer.name
    val email = viewModel.customer.email
    val phone = viewModel.customer.phoneNumber
    var customerNameFromDB by rememberSaveable { mutableStateOf("") }
    var customerEmailFromDB by rememberSaveable { mutableStateOf("") }
    var customerPhoneFromDB by rememberSaveable { mutableStateOf("") }
    var customerCreditScoreFromDB by rememberSaveable { mutableStateOf("") }
    var hasLoadedFromDb by rememberSaveable { mutableStateOf(false) }
    if(IS_EDIT_MODE && !hasLoadedFromDb){
        LaunchedEffect(customerId) {
            viewModel.getCustomerById(customerId) { fetchedCustomer ->
                customerNameFromDB= fetchedCustomer.name
                customerEmailFromDB = fetchedCustomer.email
                customerPhoneFromDB = fetchedCustomer.phoneNumber
                customerCreditScoreFromDB  = fetchedCustomer.creditScore.toString()
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
        Text(
            text = if(IS_EDIT_MODE)"Update Customer" else "Add New Customer",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

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

                }
//                viewModel.updateCustomer(
//                    it,
//                    email,
//                    phone,
//                    viewModel.creditText.toDoubleOrNull() ?: 0.0
//                )
            },
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
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

                }
            },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
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

                }
//                viewModel.updateCustomer(
//                    name,
//                    email,
//                    it,
//                    viewModel.creditText.toDoubleOrNull() ?: 0.0
//                )
            },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            )
        )

        OutlinedTextField(
            value = if(IS_EDIT_MODE) customerCreditScoreFromDB else  viewModel.creditText,
            onValueChange = {
                if(IS_EDIT_MODE){
                    customerCreditScoreFromDB=it
                }else{

                }
//                viewModel.updateCreditText(it)
//                viewModel.updateCustomer(name, email, phone, it.toDoubleOrNull() ?: 0.0)
            },
            label = { Text("Credit Amount") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                val currentCreditText = if (IS_EDIT_MODE) customerCreditScoreFromDB else viewModel.creditText

                if (currentName.isBlank() || currentEmail.isBlank() || currentPhone.isBlank() || currentCreditText.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

//                val creditValue = viewModel.creditText.toDoubleOrNull()
//                if (creditValue == null) {
//                    Toast.makeText(context, "Credit must be a number", Toast.LENGTH_SHORT).show()
//                    return@Button
//                }

                val capitalizedName = currentName.split(" ")
                    .joinToString(" ") { word ->
                        word.lowercase().replaceFirstChar { it.uppercase() }
                    }

                if (IS_EDIT_MODE) {
                    viewModel.updateCustomer(capitalizedName, currentEmail, currentPhone, currentCreditText.toDouble())
                    Toast.makeText(context, "Customer updated", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateCustomer(capitalizedName, currentEmail, currentPhone, currentCreditText.toDouble())
                    viewModel.insert()
                    Toast.makeText(context, "Customer added", Toast.LENGTH_SHORT).show()
                }
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
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
    }
}