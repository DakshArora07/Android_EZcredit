package sfu.cmpt362.android_ezcredit.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import java.util.Calendar

class OverdueInvoiceStatusWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("OverdueInvoiceStatusWorker", "Starting nightly invoice status update")

        val database = AppDatabase.getInstance(applicationContext)
        val invoiceRepository = InvoiceRepository(database.invoiceDao)
        
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

        val overdueInvoices = allInvoices.filter { invoice ->
            val dueDate = Calendar.getInstance().apply {
                timeInMillis = invoice.dueDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            invoice.status == "Unpaid" &&
                    dueDate.before(today)
        }

        overdueInvoices.forEach { invoice ->
            invoice.status = "PastDue"
            invoiceRepository.update(invoice)
        }

        return Result.success()
    }
}