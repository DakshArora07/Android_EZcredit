package sfu.cmpt362.android_ezcredit.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import java.util.Calendar

//Background Worker to mark invoices as overdue
class OverdueInvoiceStatusWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("OverdueInvoiceStatusWorker", "Starting nightly invoice status update")

        val database = AppDatabase.getInstance(applicationContext)
        val invoiceRepository = InvoiceRepository(database.invoiceDao)

        // Scans all invoices from db
        val allInvoices = invoiceRepository.invoices.first()
        if (allInvoices.isEmpty()) {
            Log.d("OverdueInvoiceStatusWorker", "No invoices to process")
            return Result.success()
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Compares current date with invoice.dueDate and marks the unpaid invoices as past due
        val overdueInvoices = allInvoices.filter { invoice ->
            val dueDate = Calendar.getInstance().apply {
                timeInMillis = invoice.dueDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            invoice.status == InvoiceStatus.Unpaid &&
                    dueDate.before(today)
        }

        overdueInvoices.forEach { invoice ->
            invoice.status = InvoiceStatus.PastDue
            invoiceRepository.update(invoice)
        }

        // Update Daily summary
        saveSummaryData(overdueInvoices.size)

        return Result.success()
    }

    private fun saveSummaryData(overdueCount: Int) {
        val prefs = applicationContext.getSharedPreferences(
            DailySummaryWorker.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit()
            .putInt(DailySummaryWorker.KEY_INVOICES_MARKED_OVERDUE, overdueCount)
            .apply()
        Log.d("OverdueInvoiceStatusWorker", "Overdue invoice summary saved - Count: $overdueCount")
    }
}