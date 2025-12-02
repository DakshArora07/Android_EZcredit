package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.repository.UserRepository
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModelFactory
import sfu.cmpt362.android_ezcredit.ui.theme.Green
import sfu.cmpt362.android_ezcredit.ui.theme.Red
import sfu.cmpt362.android_ezcredit.ui.theme.VeryLightGray
import sfu.cmpt362.android_ezcredit.ui.viewmodel.DailySummaryScreenViewModel
import sfu.cmpt362.android_ezcredit.utils.AccessMode
import sfu.cmpt362.android_ezcredit.utils.DailySummaryUiState


@Composable
fun DailySummaryScreen(

) {
    val context = LocalContext.current
    val viewModel: DailySummaryScreenViewModel = viewModel()
    val userRepository = remember {
        val database = AppDatabase.getInstance(context)
        UserRepository(database.userDao)
    }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to track if user is admin
    var isAdmin by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Check user access level on composition
    LaunchedEffect(Unit) {
        val currentUserId = CompanyContext.currentUserId
        if (currentUserId != null) {
            try {
                val user = userViewModel.getUserById(currentUserId)
                isAdmin = user.accessLevel == AccessMode.Admin
            } catch (e: Exception) {
                isAdmin = false
            }
        } else {
            isAdmin = false
        }
        isLoading = false
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isAdmin == true) {
                viewModel.refresh(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Show loading state while checking access
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show access denied message for non-admin users
    if (isAdmin == false) {
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

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.dailySummary),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.dialySummaryScreenSubHeading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item { HeaderStatsCard(uiState) }

            item {
                SummaryCard(
                    title = "Email Reminders",
                    icon = Icons.Default.Email,
                    iconTint = MaterialTheme.colorScheme.primary,
                    stats = listOf(
                        StatItem("Sent Successfully", uiState.emailsSent.toString(), true),
                        StatItem("Failed", uiState.emailsFailed.toString(), uiState.emailsFailed == 0)
                    ),
                    details = if (uiState.failedEmailList.isNotEmpty()) {
                        "Failed emails:\n${uiState.failedEmailList}"
                    } else null
                )
            }

            item {
                SummaryCard(
                    title = "Credit Score Updates",
                    icon = Icons.Default.TrendingUp,
                    iconTint = MaterialTheme.colorScheme.primary,
                    stats = listOf(
                        StatItem("Customers Updated", uiState.creditScoreUpdates.toString(), true)
                    ),
                    details = null
                )
            }

            item {
                SummaryCard(
                    title = "Invoice Status Updates",
                    icon = Icons.Default.Receipt,
                    iconTint = MaterialTheme.colorScheme.primary,
                    stats = listOf(
                        StatItem("Marked Overdue", uiState.invoicesOverdue.toString(), uiState.invoicesOverdue == 0),
                        StatItem("Marked Paid", uiState.invoicesPaid.toString(), true),
                        StatItem("Late Payments", uiState.invoicesLate.toString(), uiState.invoicesLate == 0)
                    ),
                    details = null
                )
            }
        }
    }
}

@Composable
private fun HeaderStatsCard(state: DailySummaryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Last run: ${state.lastRunDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    value = state.totalActions.toString(),
                    label = "Total Actions",
                    color = MaterialTheme.colorScheme.primary
                )

                StatBox(
                    value = state.totalIssues.toString(),
                    label = "Issues",
                    color = Red
                )

                StatBox(
                    value = state.successRate,
                    label = "Success Rate",
                    color = Green
                )
            }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    stats: List<StatItem>,
    details: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Icon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stats
            stats.forEach { stat ->
                StatRow(stat)
            }

            // Details (if any)
            if (details != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (expanded) "Hide Details" else "Show Details")
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                if (expanded) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = VeryLightGray
                    ) {
                        Text(
                            text = details,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(stat: StatItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stat.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stat.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (stat.isPositive) Green else Red
        )
    }
}

// Data class for individual stats
data class StatItem(
    val label: String,
    val value: String,
    val isPositive: Boolean
)