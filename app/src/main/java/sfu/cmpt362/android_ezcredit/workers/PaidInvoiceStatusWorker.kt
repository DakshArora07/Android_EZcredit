package sfu.cmpt362.android_ezcredit.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.data.repository.ReceiptRepository
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import android.icu.util.Calendar

class PaidInvoiceStatusWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params){
    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val receiptRepository = ReceiptRepository(database.receiptDao)
        val invoiceRepository = InvoiceRepository(database.invoiceDao)

        val allReceipts = receiptRepository.receipts.first()

        allReceipts.forEach { receipt ->
            val invoice = Invoice() // call getInvoiceByReceiptId(receipt.invoiceID)

            if (invoice != null && invoice.status == InvoiceStatus.Unpaid && invoice.status == InvoiceStatus.PastDue) {
                val today = Calendar.getInstance()
                val dueDate = invoice.dueDate

                if (dueDate.after(today)) {
                    invoiceRepository.update(invoice.copy(
                        status = InvoiceStatus.Paid
                    ))
                } else {
                    invoiceRepository.update(invoice.copy(
                        status = InvoiceStatus.LatePayment
                    ))
                }
            }
        }

        return Result.success()

    }
}