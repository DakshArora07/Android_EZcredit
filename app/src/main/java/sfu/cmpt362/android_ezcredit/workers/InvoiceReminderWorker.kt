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
        private const val CHANNEL_ID = "invoice_reminders"
        private const val CHANNEL_NAME = "Invoice Reminders"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("InvoiceReminderWorker", "Worker started")

            createNotificationChannel()

            val database = AppDatabase.getInstance(applicationContext)
            val invoiceRepository = InvoiceRepository(database.invoiceDao)
            val customerRepository = CustomerRepository(database.customerDao)

            val allInvoices = invoiceRepository.invoices.first()
            if (allInvoices.isEmpty()) {
                Log.d("InvoiceReminderWorker", "No invoices to process")
                return@withContext Result.success()
            }
            Log.d("InvoiceReminderWorker", "Total invoices loaded: ${allInvoices.size}")

            val unpaidInvoices = allInvoices.filter { it.status != "Paid" }
            Log.d("InvoiceReminderWorker", "Unpaid invoices count: ${unpaidInvoices.size}")

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

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

                val shouldSendReminder = when (daysDifference) {
                    -3, 0, 3, 5 -> true
                    else -> false
                }

                if (shouldSendReminder) {
                    Log.d("InvoiceReminderWorker", "Preparing email intent for invoice #${invoice.invoiceNumber}")

                    val customer = customerRepository.getById(invoice.customerID)
                    Log.d("InvoiceReminderWorker", "Customer found: ${customer.name}, email: ${customer.email}")

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dueDateStr = dateFormat.format(invoice.dueDate.time)

                    val message = GeminiHelper.generateReminderMessage(
                        customerName = customer.name,
                        invoiceNumber = invoice.invoiceNumber,
                        amount = invoice.amount,
                        dueDate = dueDateStr,
                        daysOffset = daysDifference
                    )

                    sendEmailIntentNotification(
                        applicationContext,
                        customer.email,
                        "Invoice #${invoice.invoiceNumber} Payment Reminder",
                        message,
                        invoice.id.toInt()
                    )
                }
            }

            Log.d("InvoiceReminderWorker", "Worker finished successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("InvoiceReminderWorker", "Error in doWork", e)
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for invoice payment reminders"
            }
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendEmailIntentNotification(
        context: Context,
        email: String,
        subject: String,
        body: String,
        notificationId: Int
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent.createChooser(intent, "Send Email"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Send Email: $subject")
            .setContentText("Tap to send this invoice reminder via email")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE + notificationId, notification)
    }

}
