package sfu.cmpt362.android_ezcredit.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.data.repository.ReceiptRepository
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModelFactory
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModelFactory
import sfu.cmpt362.android_ezcredit.data.viewmodel.ReceiptViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.ReceiptViewModelFactory
import sfu.cmpt362.android_ezcredit.ui.screens.CustomerScreen
import sfu.cmpt362.android_ezcredit.ui.screens.InvoiceScreen
import sfu.cmpt362.android_ezcredit.ui.screens.ReceiptScreen
import sfu.cmpt362.android_ezcredit.ui.screens.CalendarScreen
import sfu.cmpt362.android_ezcredit.ui.screens.AnalyticsScreen
import sfu.cmpt362.android_ezcredit.ui.screens.DailySummaryScreen
import sfu.cmpt362.android_ezcredit.ui.screens.SettingsScreen
import sfu.cmpt362.android_ezcredit.ui.screens.manual_input.CustomerEntryScreen
import sfu.cmpt362.android_ezcredit.ui.screens.manual_input.InvoiceEntryScreen
import sfu.cmpt362.android_ezcredit.ui.screens.manual_input.ReceiptEntryScreen
import sfu.cmpt362.android_ezcredit.ui.viewmodel.InvoiceScreenViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.core.content.ContextCompat
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular

data class Screen(
    val route: String,
    val title: Int,
    val icon: ImageVector
)

val screens = listOf(
    Screen("analytics", R.string.analytics, Icons.Default.Analytics),
    Screen("customers", R.string.customers, Icons.Default.People),
    Screen("invoices", R.string.invoices, Icons.Default.Receipt),
    Screen("receipts", R.string.receipts, Icons.AutoMirrored.Filled.ReceiptLong),
    Screen("calendar", R.string.calendar, Icons.Default.CalendarMonth)
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val context = LocalContext.current

    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            BackgroundTaskSchedular.initializeAllTasks(context)
        } else {
            // Permission denied: optionally notify user or proceed without reminders
        }
    }

    // Check and request permission when screen is first composed
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    BackgroundTaskSchedular.initializeAllTasks(context)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            BackgroundTaskSchedular.initializeAllTasks(context)
        }
    }

    // --- Database and ViewModel setup --- //
    val customerRepository = remember {
        val database = AppDatabase.getInstance(context)
        CustomerRepository(database.customerDao)
    }
    val customerViewModel: CustomerViewModel = viewModel(
        factory = CustomerViewModelFactory(customerRepository)
    )

    val invoiceRepository = remember {
        val database = AppDatabase.getInstance(context)
        InvoiceRepository(database.invoiceDao)
    }
    val invoiceViewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModelFactory(invoiceRepository)
    )

    val receiptRepository = remember {
        val database = AppDatabase.getInstance(context)
        ReceiptRepository(database.receiptDao)
    }

    val receiptViewModel: ReceiptViewModel = viewModel(
        factory = ReceiptViewModelFactory(receiptRepository)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 24.dp)
                ) {

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Summarize, contentDescription = null) },
                        label = { Text("Daily Summary") },
                        selected = currentRoute == "summary",
                        onClick = {
                            if (currentRoute != "summary") {
                                navController.navigate("summary") {
                                    popUpTo("customers") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()

                    screens.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.title)) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo("customers") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()


                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.settings)) },
                        selected = currentRoute == "settings",
                        onClick = {
                            if (currentRoute != "settings") {
                                navController.navigate("settings") {
                                    popUpTo("customers") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            NavigationHost(
                navController = navController,
                customerViewModel = customerViewModel,
                invoiceViewModel = invoiceViewModel,
                receiptViewModel = receiptViewModel,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationHost(
    navController: NavHostController,
    customerViewModel: CustomerViewModel,
    invoiceViewModel: InvoiceViewModel,
    receiptViewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "analytics",
        modifier = modifier,
    ) {
        composable("customers") {
            CustomerScreen(
                viewModel = customerViewModel,
                onAddCustomer = { customerId:Long ->
                    if(customerId>=0L){
                        navController.navigate("addCustomer?customerId=$customerId")
                    }else{
                        navController.navigate("addCustomer")
                    }
                }
            )
        }
        composable("addCustomer?customerId={customerId}",
            arguments=listOf(
                navArgument("customerId"){
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )) {
            val customerId = it.arguments?.getLong("customerId") ?: -1L
            CustomerEntryScreen(
                customerViewModel = customerViewModel,
                invoiceViewModel = invoiceViewModel,
                customerId = customerId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("invoices") {
            val invoiceScreenViewModel: InvoiceScreenViewModel = viewModel()
            InvoiceScreen(
                invoiceViewModel = invoiceViewModel,
                invoiceScreenViewModel = invoiceScreenViewModel,
                customerViewModel,
                onAddInvoice = { invoiceId: Long ->
                    if (invoiceId >= 0L) {
                        navController.navigate("addInvoice?invoiceId=$invoiceId")
                    } else {
                        navController.navigate("addInvoice")
                    }
                },
                onScanCompleted = { ocrResult ->
                    navController.navigate(
                        "addInvoice?invoiceId=-1&ocrInvoiceNumber=${ocrResult.invoiceNumber.orEmpty()}" +
                                "&ocrAmount=${ocrResult.amount.orEmpty()}&ocrCustomer=${ocrResult.customerName.orEmpty()}"
                    )
                }

            )
        }
        composable(
            "addInvoice?invoiceId={invoiceId}&ocrInvoiceNumber={ocrInvoiceNumber}&ocrAmount={ocrAmount}&ocrCustomer={ocrCustomer}",
            arguments = listOf(
                navArgument("invoiceId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("ocrInvoiceNumber") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("ocrAmount") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("ocrCustomer") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: -1L
            val ocrInvoiceNumber = backStackEntry.arguments?.getString("ocrInvoiceNumber")?.takeIf { it.isNotBlank() }
            val ocrAmount = backStackEntry.arguments?.getString("ocrAmount")?.takeIf { it.isNotBlank() }
            val ocrCustomer = backStackEntry.arguments?.getString("ocrCustomer")?.takeIf { it.isNotBlank() }

            val ocrResult =
                if (ocrInvoiceNumber != null || ocrAmount != null || ocrCustomer != null) {
                    InvoiceScreenViewModel.OcrInvoiceResult(
                        invoiceNumber = ocrInvoiceNumber,
                        amount = ocrAmount,
                        customerName = ocrCustomer,
                        issueDate = null,
                        dueDate = null
                    )
                } else null

            InvoiceEntryScreen(
                invoiceViewModel = invoiceViewModel,
                customerViewModel = customerViewModel,
                invoiceId = invoiceId,
                ocrResult = ocrResult,
                onBack = { navController.popBackStack() }
            )
        }

        composable("receipts") {
            val invoiceScreenViewModel: InvoiceScreenViewModel = viewModel()
            ReceiptScreen(
                invoiceViewModel = invoiceViewModel,
                invoiceScreenViewModel = invoiceScreenViewModel,
                customerViewModel,
                receiptViewModel = receiptViewModel,
                onViewAddReceipt = { receiptId: Long ->
                    if (receiptId >= 0L) {
                        navController.navigate("viewReceipt?receiptId=$receiptId")
                    } else {
                        navController.navigate("addReceipt")
                    }
                }
            )
        }


        composable("addReceipt") {
            ReceiptEntryScreen(
                invoiceViewModel = invoiceViewModel,
                customerViewModel = customerViewModel,
                receiptViewModel = receiptViewModel,
                receiptId = null,
                isViewing = false,
                onBack = {navController.popBackStack()}
            )
        }

        composable("viewReceipt?receiptId={receiptId}") {
            val receiptId = it.arguments?.getString("receiptId")?.toLongOrNull()
            ReceiptEntryScreen(
                invoiceViewModel = invoiceViewModel,
                customerViewModel = customerViewModel,
                receiptViewModel = receiptViewModel,
                receiptId = receiptId,
                isViewing = true,
                onBack = {navController.popBackStack()}
            )
        }



        composable("calendar") { CalendarScreen(invoiceViewModel = invoiceViewModel,
            onNavigateToInvoice = { invoiceId ->
                navController.navigate("addInvoice?invoiceId=$invoiceId")
            }) }
        composable("analytics") { AnalyticsScreen(invoiceViewModel = invoiceViewModel,
            customerViewModel = customerViewModel) }
        composable("summary") {
            DailySummaryScreen()
        }
        composable("settings") { SettingsScreen() }
    }
}
