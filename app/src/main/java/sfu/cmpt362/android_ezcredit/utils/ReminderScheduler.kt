package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import androidx.work.*
import sfu.cmpt362.android_ezcredit.workers.InvoiceReminderWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val REMINDER_WORK_NAME = "invoice_reminder_work"

    /**
     * Schedule daily invoice reminder checks
     */
    fun scheduleInvoiceReminders(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Run daily at 9 AM
        val currentTime = java.util.Calendar.getInstance()
        val targetTime = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }

        if (targetTime.before(currentTime)) {
            targetTime.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val reminderWork = PeriodicWorkRequestBuilder<InvoiceReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }

    /**
     * Cancel all scheduled reminders
     */
    fun cancelInvoiceReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    /**
     * Run reminder check immediately (for testing)
     */
    fun runReminderCheckNow(context: Context) {
        val reminderWork = OneTimeWorkRequestBuilder<InvoiceReminderWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(reminderWork)
    }
}