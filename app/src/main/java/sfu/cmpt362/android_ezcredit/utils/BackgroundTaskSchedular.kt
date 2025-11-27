package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import sfu.cmpt362.android_ezcredit.workers.CreditScoreUpdateWorker
import sfu.cmpt362.android_ezcredit.workers.InvoiceReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object BackgroundTaskSchedular {

    private const val TAG = "BackgroundTaskScheduler"
    private const val REMINDER_WORK_NAME = "invoice_reminder_work"
    private const val CREDIT_SCORE_UPDATE_WORK_NAME = "credit_score_update_work"

    fun initializeAllTasks(context: Context) {
        scheduleInvoiceReminders(context)
        scheduleCreditScoreUpdate(context)
    }

    fun scheduleInvoiceReminders(context: Context) {
        Log.d(TAG, "Scheduling invoice reminders")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val initialDelay = calculateInitialDelay(9)
        Log.d(TAG, "Initial delay for reminders: $initialDelay ms")

        val reminderWork = PeriodicWorkRequestBuilder<InvoiceReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("invoice_reminders")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }

    fun scheduleCreditScoreUpdate(context: Context) {
        Log.d(TAG, "Scheduling credit score updates")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val initialDelay = calculateInitialDelay(9)
        Log.d(TAG, "Initial delay for credit score: $initialDelay ms")

        val creditScoreUpdateWork = PeriodicWorkRequestBuilder<CreditScoreUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("credit_score_update")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CREDIT_SCORE_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            creditScoreUpdateWork
        )
    }

    private fun calculateInitialDelay(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
