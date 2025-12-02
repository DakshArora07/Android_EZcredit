package sfu.cmpt362.android_ezcredit.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.repository.CompanyRepository
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.utils.GeminiHelper
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import sfu.cmpt362.android_ezcredit.utils.MailgunEmailService
import java.text.SimpleDateFormat
import java.util.*

class InvoiceReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "InvoiceReminderWorker"
    }
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("InvoiceReminderWorker", "Worker started")
            resetDailySummary()

            Log.d("InvoiceReminderWorker", "initialining the MailgunEmailService()")
            val emailService = MailgunEmailService()
            Log.d("InvoiceReminderWorker", "DONE initialining the MailgunEmailService()")

            val database = AppDatabase.getInstance(applicationContext)
            val invoiceRepository = InvoiceRepository(database.invoiceDao)
            val customerRepository = CustomerRepository(database.customerDao)
            val companyRepository = CompanyRepository(database.companyDao)

            val allInvoices = invoiceRepository.invoices.first()
            if (allInvoices.isEmpty()) {
                Log.d("InvoiceReminderWorker", "No invoices to process")
                return@withContext Result.success()
            }
            Log.d("InvoiceReminderWorker", "Total invoices loaded: ${allInvoices.size}")

            val unpaidInvoices = allInvoices.filter { it.status == InvoiceStatus.Unpaid || it.status == InvoiceStatus.PastDue }
            Log.d("InvoiceReminderWorker", "Unpaid invoices count: ${unpaidInvoices.size}")

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var emailsSent = 0
            var emailsFailed = 0
            val failedEmails = mutableListOf<String>()

            unpaidInvoices.forEach { invoice ->
                val dueDate = Calendar.getInstance().apply {
                    time = invoice.dueDate.time
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val daysDifference = ((today.timeInMillis - dueDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                Log.d("InvoiceReminderWorker", "Invoice #${invoice.invoiceNumber}: dueDate=$dueDate, daysDifference=$daysDifference")

                val shouldSendReminder = daysDifference in -2..30

                if (shouldSendReminder) {
                    Log.d("InvoiceReminderWorker", "Preparing email intent for invoice #${invoice.invoiceNumber}")

                    val customer = customerRepository.getById(invoice.customerId)
                    Log.d("InvoiceReminderWorker", "Customer found: ${customer.name}, email: ${customer.email}")

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dueDateStr = dateFormat.format(invoice.dueDate.time)

                    val companyName = companyRepository.getById(CompanyContext.currentCompanyId!!).name

                    val message = GeminiHelper.generateReminderMessage(
                        customerName = customer.name,
                        invoiceNumber = invoice.invoiceNumber,
                        amount = invoice.amount,
                        dueDate = dueDateStr,
                        status = invoice.status,
                        daysOffset = daysDifference,
                        companyName = companyName,
                        invoiceURL =  invoice.url
                    )

                    val result = emailService.sendEmail(
                        toEmail = customer.email,
                        subject = "Invoice #${invoice.invoiceNumber} Payment Reminder",
                        body = message
                    )
                    if(result.isSuccess){
                        emailsSent+=1
                        Log.d(TAG, "✓ Email sent successfully to ${customer.email}")
                    }else{
                        emailsFailed+=1
                        failedEmails.add("${customer.name} : ${customer.email}")
                        Log.e(TAG, "✗ Failed to send email to ${customer.email}: ${result.exceptionOrNull()?.message}")
                    }
                }
            }

            saveSummaryData(emailsSent, emailsFailed, failedEmails)
            Log.d("InvoiceReminderWorker", "Worker finished successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("InvoiceReminderWorker", "Error in doWork", e)
            Result.retry()
        }
    }

    private fun resetDailySummary() {
        val prefs = applicationContext.getSharedPreferences(
            DailySummaryWorker.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit().apply {
            putInt(DailySummaryWorker.KEY_EMAILS_SENT, 0)
            putInt(DailySummaryWorker.KEY_EMAILS_FAILED, 0)
            putString(DailySummaryWorker.KEY_FAILED_EMAIL_LIST, "")
            putInt(DailySummaryWorker.KEY_CREDIT_SCORE_UPDATES, 0)
            putInt(DailySummaryWorker.KEY_INVOICES_MARKED_OVERDUE, 0)
            putInt(DailySummaryWorker.KEY_INVOICES_MARKED_PAID, 0)
            putInt(DailySummaryWorker.KEY_INVOICES_MARKED_LATE, 0)
        }.apply()
        Log.d(TAG, "Daily summary counters reset")
    }

    private fun saveSummaryData(sent: Int, failed: Int, failedList: List<String>) {
        val prefs = applicationContext.getSharedPreferences(
            DailySummaryWorker.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit().apply {
            putInt(DailySummaryWorker.KEY_EMAILS_SENT, sent)
            putInt(DailySummaryWorker.KEY_EMAILS_FAILED, failed)
            putString(DailySummaryWorker.KEY_FAILED_EMAIL_LIST, failedList.joinToString("\n"))
        }.apply()
        Log.d(TAG, "Email summary saved - Sent: $sent, Failed: $failed")
    }
}
