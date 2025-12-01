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
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DailySummaryWorker"
        private const val CHANNEL_ID = "daily_summary_channel"
        private const val NOTIFICATION_ID = 1001
        const val PREFS_NAME = "daily_summary_prefs"

        // Keys for storing summary data
        const val KEY_EMAILS_SENT = "emails_sent"
        const val KEY_EMAILS_FAILED = "emails_failed"
        const val KEY_FAILED_EMAIL_LIST = "failed_email_list"
        const val KEY_CREDIT_SCORE_UPDATES = "credit_score_updates"
        const val KEY_INVOICES_MARKED_OVERDUE = "invoices_overdue"
        const val KEY_INVOICES_MARKED_PAID = "invoices_paid"
        const val KEY_INVOICES_MARKED_LATE = "invoices_late"
        const val KEY_LAST_RUN_DATE = "last_run_date"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting daily summary aggregation")

            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Read all summary data
            val emailsSent = prefs.getInt(KEY_EMAILS_SENT, 0)
            val emailsFailed = prefs.getInt(KEY_EMAILS_FAILED, 0)
            // failedEmails list is read by the UI screen, not needed here
            val creditScoreUpdates = prefs.getInt(KEY_CREDIT_SCORE_UPDATES, 0)
            val invoicesOverdue = prefs.getInt(KEY_INVOICES_MARKED_OVERDUE, 0)
            val invoicesPaid = prefs.getInt(KEY_INVOICES_MARKED_PAID, 0)
            val invoicesLate = prefs.getInt(KEY_INVOICES_MARKED_LATE, 0)

            // Update last run date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            prefs.edit().putString(KEY_LAST_RUN_DATE, currentDate).apply()

            Log.d(TAG, "Summary - Emails: $emailsSent sent, $emailsFailed failed")
            Log.d(TAG, "Summary - Credit scores updated: $creditScoreUpdates")
            Log.d(TAG, "Summary - Invoices: $invoicesOverdue overdue, $invoicesPaid paid, $invoicesLate late")

            // Create and show notification
            createNotification(
                emailsSent,
                emailsFailed,
                creditScoreUpdates,
                invoicesOverdue,
                invoicesPaid,
                invoicesLate
            )

            Log.d(TAG, "Daily summary completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating daily summary", e)
            Result.failure()
        }
    }

    private fun createNotification(
        emailsSent: Int,
        emailsFailed: Int,
        creditScoreUpdates: Int,
        invoicesOverdue: Int,
        invoicesPaid: Int,
        invoicesLate: Int
    ) {
        createNotificationChannel()

        // Build concise summary message
        val summaryParts = mutableListOf<String>()

        if (emailsSent > 0 || emailsFailed > 0) {
            summaryParts.add("📧 $emailsSent emails sent" + if (emailsFailed > 0) ", $emailsFailed failed" else "")
        }
        if (creditScoreUpdates > 0) {
            summaryParts.add("📊 $creditScoreUpdates credit scores updated")
        }
        if (invoicesOverdue > 0) {
            summaryParts.add("⚠️ $invoicesOverdue invoices overdue")
        }
        if (invoicesPaid > 0 || invoicesLate > 0) {
            summaryParts.add("✓ $invoicesPaid paid, $invoicesLate late")
        }

        val summaryText = if (summaryParts.isEmpty()) {
            "No updates today"
        } else {
            summaryParts.joinToString(" • ")
        }

        val intent = applicationContext.packageManager.getLaunchIntentForPackage(
            applicationContext.packageName
        )?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "summary")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Daily Summary - ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())}")
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily summary of automated tasks"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}