package sfu.cmpt362.android_ezcredit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.utils.DailySummaryUiState
import sfu.cmpt362.android_ezcredit.workers.DailySummaryWorker


class DailySummaryScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DailySummaryUiState())
    val uiState: StateFlow<DailySummaryUiState> = _uiState.asStateFlow()

    fun loadSummaryData(context: Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences(
                DailySummaryWorker.PREFS_NAME,
                Context.MODE_PRIVATE
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

            _uiState.value = DailySummaryUiState(
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
    }

    fun refresh(context: Context) {
        loadSummaryData(context)
    }
}
