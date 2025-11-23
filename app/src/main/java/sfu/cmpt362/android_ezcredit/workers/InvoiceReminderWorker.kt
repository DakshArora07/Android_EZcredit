package sfu.cmpt362.android_ezcredit.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.utils.GeminiHelper
import java.text.SimpleDateFormat
import java.util.*

class InvoiceReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "invoice_reminders"
        const val CHANNEL_NAME = "Invoice Reminders"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {

            val database = AppDatabase.getInstance(applicationContext)
            val invoiceRepository = InvoiceRepository(database.invoiceDao)
            val customerRepository = CustomerRepository(database.customerDao)

            // Get all unpaid and past due invoices
            val allInvoices = invoiceRepository.invoices.first()

            val unpaidInvoices = allInvoices.filter {
                it.status != "Paid"
            }

            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            unpaidInvoices.forEach { invoice ->
                val dueDate = invoice.dueDate.clone() as Calendar
                dueDate.set(Calendar.HOUR_OF_DAY, 0)
                dueDate.set(Calendar.MINUTE, 0)
                dueDate.set(Calendar.SECOND, 0)
                dueDate.set(Calendar.MILLISECOND, 0)

                val daysDifference = ((today.timeInMillis - dueDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                // Check if today matches any reminder day
                val shouldSendReminder = when (daysDifference) {
                    -3 -> true  // 3 days before
                    0 -> true   // On due date
                    3 -> true   // 3 days after
                    5 -> true   // 5 days after
                    else -> false
                }

                if (shouldSendReminder) {
                    // Get customer details
                    val customer = customerRepository.getById(invoice.customerID)

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dueDateStr = dateFormat.format(invoice.dueDate.time)

                    val message = GeminiHelper.generateReminderMessage(
                        customerName = customer.name,
                        invoiceNumber = invoice.invoiceNumber,
                        amount = invoice.amount,
                        dueDate = dueDateStr,
                        daysOffset = daysDifference
                    )

                    // Send notification (Just notification right now, will be replaced by
                    // messaging in next phase)
                    sendReminderNotification(
                        customer.name,
                        invoice.invoiceNumber,
                        message,
                        invoice.id.toInt()
                    )

                    // TODO: Implement actual SMS/Email sending here
                    // sendSMS(customer.phoneNumber, message)
                    // sendEmail(customer.email, "Payment Reminder", message)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendReminderNotification(
        customerName: String,
        invoiceNumber: String,
        message: String,
        notificationId: Int
    ) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Reminder: Invoice #$invoiceNumber")
            .setContentText("Reminder sent to $customerName")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE + notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for invoice payment reminders"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}