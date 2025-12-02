package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.utils.CreditScoreCalculator
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.repository.UserRepository
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModelFactory
import sfu.cmpt362.android_ezcredit.utils.AccessMode

@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel,
    onAddCustomer: (id:Long) -> Unit
) {

    val context = LocalContext.current
    val userRepository = remember {
        val database = AppDatabase.getInstance(context)
        UserRepository(database.userDao)
    }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepository)
    )
    var isReceipts by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val currentUserId = CompanyContext.currentUserId
        if (currentUserId != null) {
            try {
                val user = userViewModel.getUserById(currentUserId)
                isReceipts = user.accessLevel == AccessMode.Receipts
            } catch (e: Exception) {
                isReceipts = false
            }
        } else {
            isReceipts = false
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (isReceipts == true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Access Denied",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "This screen is only accessible to administrators.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        return
    }

    var sortCustomersByName by rememberSaveable { mutableStateOf(false) }
    var sortCustomersByCreditScore by rememberSaveable { mutableStateOf(false) }
    var sortCustomersByTimeCreated by rememberSaveable { mutableStateOf(true) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val customers by viewModel.customersLiveData.observeAsState(emptyList())
    var filterListExpanded by rememberSaveable { mutableStateOf(false) }

    // Apply search filter
    val searchedCustomers = if (searchQuery.isNotEmpty()) {
        customers.filter { customer ->
            customer.name.contains(searchQuery, ignoreCase = true) ||
                    customer.email.contains(searchQuery, ignoreCase = true)
        }
    } else {
        customers
    }

    // Apply sort
    viewModel.defCustomersOrSorted = when {
        sortCustomersByName -> searchedCustomers.sortedBy { it.name }
        sortCustomersByCreditScore -> searchedCustomers.sortedByDescending { it.creditScore }
        sortCustomersByTimeCreated -> searchedCustomers.sortedBy { it.id }
        else -> searchedCustomers
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            text = { Text("Sort by Time Created") },
                            onClick = {
                                sortCustomersByTimeCreated = true
                                sortCustomersByName = false
                                sortCustomersByCreditScore = false
                                filterListExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Name") },
                            onClick = {
                                sortCustomersByName = true
                                sortCustomersByTimeCreated = false
                                sortCustomersByCreditScore = false
                                filterListExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Credit Score") },
                            onClick = {
                                sortCustomersByCreditScore = true
                                sortCustomersByTimeCreated = false
                                sortCustomersByName = false
                                filterListExpanded = false
                            }
                        )
                    }
                }

                FloatingActionButton(onClick = {onAddCustomer(-1)}) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        val focusRequester = remember { FocusRequester() }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Customers") },
            placeholder = { Text("Search by name or email") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
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
                        text = if (searchQuery.isNotEmpty()) "No customers found" else "No customers yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search" else "Add your first customer to get started",
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