package sfu.cmpt362.android_ezcredit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.ui.screens.*

data class Screen(
    val route: String,
    val title: Int,
    val icon: ImageVector
)

val screens = listOf(
    Screen("customers", R.string.customers, Icons.Default.People),
    Screen("invoices", R.string.invoices, Icons.Default.Receipt),
    Screen("calendar", R.string.calendar, Icons.Default.CalendarMonth),
    Screen("analytics", R.string.analytics, Icons.Default.Analytics)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                        Spacer(Modifier.height(8.dp))
                        screens.forEach { screen ->
                            NavigationDrawerItem(
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(stringResource(screen.title)) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    NavigationDrawerItem(
                        icon = {Icon(Icons.Default.Settings, contentDescription = null)},
                        label = {Text(stringResource(R.string.settings))},
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings")
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
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
            NavigationHost(navController, Modifier.padding(padding))
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = "customers", modifier = modifier) {
        composable("customers") { CustomerScreen() }
        composable("invoices") { InvoiceScreen() }
        composable("calendar") { CalendarScreen() }
        composable("analytics") { AnalyticsScreen() }
        composable("settings") { SettingsScreen()}
    }
}
