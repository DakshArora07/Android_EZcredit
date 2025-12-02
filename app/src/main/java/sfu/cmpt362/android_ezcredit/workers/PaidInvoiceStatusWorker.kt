package sfu.cmpt362.android_ezcredit.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.data.repository.ReceiptRepository
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import android.icu.util.Calendar
import android.util.Log
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository

class PaidInvoiceStatusWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params){

    companion object {
        private const val TAG = "PaidInvoiceWorker"
    }

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val receiptRepository = ReceiptRepository(database.receiptDao)
        val invoiceRepository = InvoiceRepository(database.invoiceDao)
        val customerRepository = CustomerRepository(database.customerDao)

        val allReceipts = receiptRepository.receipts.first()
        Log.d(TAG, "Processing ${allReceipts.size} receipts")

        var paidCount = 0
        var lateCount = 0

        allReceipts.forEach { receipt ->
            val invoice = invoiceRepository.getById(receipt.invoiceId)
            Log.d(TAG, "invoice status = ${invoice.status}")
            if (invoice.status == InvoiceStatus.Unpaid || invoice.status == InvoiceStatus.PastDue) {
                val today = Calendar.getInstance()
                val dueDate = invoice.dueDate
                Log.d(TAG, "today = ${today.time}, dueDate = ${dueDate.time}")
                if (dueDate.after(today)) {
                    Log.d(TAG, "invoice paid")
                    invoiceRepository.update(invoice.copy(
                        status = InvoiceStatus.Paid
                    ))
                    paidCount++
                } else {
                    Log.d(TAG, "invoice late")
                    invoiceRepository.update(invoice.copy(
                        status = InvoiceStatus.LatePayment
                    ))
                    lateCount ++
                }

                val customer = customerRepository.getById(invoice.customerId)
                val newCredit = customer.credit - invoice.amount
                customerRepository.update(customer.copy(
                    credit = newCredit
                ))
            }
        }

        saveSummaryData(paidCount, lateCount)

        return Result.success()
    }

    private fun saveSummaryData(paidCount: Int, lateCount: Int) {
        val prefs = applicationContext.getSharedPreferences(
            DailySummaryWorker.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit().apply {
            putInt(DailySummaryWorker.KEY_INVOICES_MARKED_PAID, paidCount)
            putInt(DailySummaryWorker.KEY_INVOICES_MARKED_LATE, lateCount)
        }.apply()
        Log.d(TAG, "Payment invoice summary saved - Paid: $paidCount, Late: $lateCount")
    }
}