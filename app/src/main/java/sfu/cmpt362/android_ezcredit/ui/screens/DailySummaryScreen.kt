package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.workers.DailySummaryWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryScreen() {
    val context = LocalContext.current
    val summaryData = remember { loadSummaryData(context) }

    Scaffold{ padding ->
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

            // Header Stats Card
            item {
                HeaderStatsCard(summaryData)
            }

            // Email Summary
            item {
                SummaryCard(
                    title = "Email Reminders",
                    icon = Icons.Default.Email,
                    iconTint = MaterialTheme.colorScheme.primary,
                    stats = listOf(
                        StatItem("Sent Successfully", summaryData.emailsSent.toString(), true),
                        StatItem("Failed", summaryData.emailsFailed.toString(), summaryData.emailsFailed == 0)
                    ),
                    details = if (summaryData.failedEmailList.isNotEmpty()) {
                        "Failed emails:\n${summaryData.failedEmailList}"
                    } else null
                )
            }

            // Credit Score Updates
            item {
                SummaryCard(
                    title = "Credit Score Updates",
                    icon = Icons.Default.TrendingUp,
                    iconTint = MaterialTheme.colorScheme.primary,
                    stats = listOf(
                        StatItem("Customers Updated", summaryData.creditScoreUpdates.toString(), true)
                    ),
                    details = null
                )
            }

            // Invoice Status Updates
            item {
                SummaryCard(
                    title = "Invoice Status Updates",
                    icon = Icons.Default.Receipt,
                    iconTint = MaterialTheme.colorScheme.primary,
                    stats = listOf(
                        StatItem("Marked Overdue", summaryData.invoicesOverdue.toString(), summaryData.invoicesOverdue == 0),
                        StatItem("Marked Paid", summaryData.invoicesPaid.toString(), true),
                        StatItem("Late Payments", summaryData.invoicesLate.toString(), summaryData.invoicesLate == 0)
                    ),
                    details = null
                )
            }
        }
    }
}

@Composable
fun HeaderStatsCard(data: SummaryData) {
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
                text = "Last run: ${data.lastRunDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    value = data.totalActions.toString(),
                    label = "Total Actions",
                    color = MaterialTheme.colorScheme.primary
                )

                StatBox(
                    value = data.totalIssues.toString(),
                    label = "Issues",
                    color = Color(0xFFF44336)
                )

                StatBox(
                    value = data.successRate,
                    label = "Success Rate",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun StatBox(value: String, label: String, color: Color) {
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
fun SummaryCard(
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
                        color = Color(0xFFEEEEEE)
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
fun StatRow(stat: StatItem) {
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
            color = if (stat.isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

// Data classes
data class SummaryData(
    val emailsSent: Int,
    val emailsFailed: Int,
    val failedEmailList: String,
    val creditScoreUpdates: Int,
    val invoicesOverdue: Int,
    val invoicesPaid: Int,
    val invoicesLate: Int,
    val lastRunDate: String,
    val totalActions: Int,
    val totalIssues: Int,
    val successRate: String
)

data class StatItem(
    val label: String,
    val value: String,
    val isPositive: Boolean
)

// Helper function to load data
private fun loadSummaryData(context: android.content.Context): SummaryData {
    val prefs = context.getSharedPreferences(
        DailySummaryWorker.PREFS_NAME,
        android.content.Context.MODE_PRIVATE
    )

    val emailsSent = prefs.getInt(DailySummaryWorker.KEY_EMAILS_SENT, 0)
    val emailsFailed = prefs.getInt(DailySummaryWorker.KEY_EMAILS_FAILED, 0)
    val failedEmailList = prefs.getString(DailySummaryWorker.KEY_FAILED_EMAIL_LIST, "") ?: ""
    val creditScoreUpdates = prefs.getInt(DailySummaryWorker.KEY_CREDIT_SCORE_UPDATES, 0)
    val invoicesOverdue = prefs.getInt(DailySummaryWorker.KEY_INVOICES_MARKED_OVERDUE, 0)
    val invoicesPaid = prefs.getInt(DailySummaryWorker.KEY_INVOICES_MARKED_PAID, 0)
    val invoicesLate = prefs.getInt(DailySummaryWorker.KEY_INVOICES_MARKED_LATE, 0)
    val lastRunDate = prefs.getString(DailySummaryWorker.KEY_LAST_RUN_DATE, "Never") ?: "Never"

    val totalActions = emailsSent + creditScoreUpdates + invoicesOverdue + invoicesPaid + invoicesLate
    val totalIssues = emailsFailed + invoicesOverdue + invoicesLate
    val successRate = if (totalActions > 0) {
        "${((totalActions - totalIssues) * 100 / totalActions)}%"
    } else {
        "N/A"
    }

    return SummaryData(
        emailsSent = emailsSent,
        emailsFailed = emailsFailed,
        failedEmailList = failedEmailList,
        creditScoreUpdates = creditScoreUpdates,
        invoicesOverdue = invoicesOverdue,
        invoicesPaid = invoicesPaid,
        invoicesLate = invoicesLate,
        lastRunDate = lastRunDate,
        totalActions = totalActions,
        totalIssues = totalIssues,
        successRate = successRate
    )
}