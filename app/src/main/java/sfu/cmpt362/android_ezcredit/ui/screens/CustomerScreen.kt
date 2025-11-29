package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.ui.theme.Red
import sfu.cmpt362.android_ezcredit.utils.CreditScoreCalculator
import sfu.cmpt362.android_ezcredit.workers.InvoiceReminderWorker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import sfu.cmpt362.android_ezcredit.data.entity.Customer

@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel,
    onAddCustomer: (id:Long) -> Unit
) {
    var sortCustomersByName by rememberSaveable { mutableStateOf(false) }
    val customers by viewModel.customersLiveData.observeAsState(emptyList())
    val context = LocalContext.current
    var filterListExpanded by rememberSaveable { mutableStateOf(false) }

    viewModel.defCustomersOrSorted = if (sortCustomersByName) {
        customers.sortedBy { it.name } // apply sort
    } else {
        customers // original list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar: Title + FAB
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.customers),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.customerScreenSubHeading),
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
                        text = { Text("Sort by Name") },
                        onClick = {
                            sortCustomersByName = !sortCustomersByName
//
                            filterListExpanded = false
                        }
                    )
                }
            }

            FloatingActionButton(onClick = {onAddCustomer(-1)}) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if ( viewModel.defCustomersOrSorted.isEmpty()){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No customers yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add your first customer to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.defCustomersOrSorted) { customer ->
                    CustomerCard(
                        name = customer.name,
                        email = customer.email,
                        creditScore = customer.creditScore,
                        credit = customer.credit,
                        onClick = {
                            onAddCustomer(customer.id)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun CustomerCard( name: String, email: String, credit: Double, creditScore: Int, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Credit Score: $creditScore", style = MaterialTheme.typography.bodyMedium, color = CreditScoreCalculator.getCreditScoreColor(creditScore))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = email, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Credit: $$credit", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}