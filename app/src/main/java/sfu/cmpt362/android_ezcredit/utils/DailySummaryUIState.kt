package sfu.cmpt362.android_ezcredit.utils

data class DailySummaryUiState(
    val emailsSent: Int = 0,
    val emailsFailed: Int = 0,
    val failedEmailList: String = "",
    val creditScoreUpdates: Int = 0,
    val invoicesOverdue: Int = 0,
    val invoicesPaid: Int = 0,
    val invoicesLate: Int = 0,
    val lastRunDate: String = "Never",
    val totalActions: Int = 0,
    val totalIssues: Int = 0,
    val successRate: String = "N/A"
)