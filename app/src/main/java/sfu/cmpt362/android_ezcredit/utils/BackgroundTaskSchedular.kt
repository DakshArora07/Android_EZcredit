package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import sfu.cmpt362.android_ezcredit.workers.CreditScoreUpdateWorker
import sfu.cmpt362.android_ezcredit.workers.InvoiceReminderWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object BackgroundTaskSchedular {

    private const val TAG = "BackgroundTaskScheduler"
    private const val REMINDER_WORK_NAME = "invoice_reminder_work"
    private const val CREDIT_SCORE_UPDATE_WORK_NAME = "credit_score_update_work"

    fun initializeAllTasks(context: Context) {
        scheduleInvoiceReminders(context)
        scheduleCreditScoreUpdate(context)
        checkWorkStatus(context)
    }

    fun scheduleInvoiceReminders(context: Context) {
        Log.d(TAG, "Scheduling invoice reminders")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val reminderWork = PeriodicWorkRequestBuilder<InvoiceReminderWorker>(
            10, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .addTag("invoice_reminders")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderWork
        )
    }

    fun cancelInvoiceReminders(context: Context) {
        Log.d(TAG, "Cancelling invoice reminders")
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun scheduleCreditScoreUpdate(context: Context) {
        Log.d(TAG, "Scheduling credit score updates")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val creditScoreUpdateWork = PeriodicWorkRequestBuilder<CreditScoreUpdateWorker>(
            10, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .addTag("credit_score_update")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CREDIT_SCORE_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            creditScoreUpdateWork
        )
    }

    fun checkWorkStatus(context: Context) {
        val workManager = WorkManager.getInstance(context)

        try {
            val creditScoreWorkInfos =
                workManager.getWorkInfosForUniqueWork(CREDIT_SCORE_UPDATE_WORK_NAME)
            creditScoreWorkInfos.get().forEach { workInfo ->
                val nextRunTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(workInfo.nextScheduleTimeMillis))

                Log.d(TAG, """
                    ====================================
                    Credit Score Work Status:
                    State: ${workInfo.state}
                    Next run: $nextRunTime
                    Run attempt: ${workInfo.runAttemptCount}
                    Tags: ${workInfo.tags}
                    ====================================
                """.trimIndent())
            }

            val reminderWorkInfos = workManager.getWorkInfosForUniqueWork(REMINDER_WORK_NAME)
            reminderWorkInfos.get().forEach { workInfo ->
                val nextRunTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(workInfo.nextScheduleTimeMillis))

                Log.d(TAG, """
                    ====================================
                    Invoice Reminder Work Status:
                    State: ${workInfo.state}
                    Next run: $nextRunTime
                    Run attempt: ${workInfo.runAttemptCount}
                    Tags: ${workInfo.tags}
                    ====================================
                """.trimIndent())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking work status: ${e.message}")
        }
    }
}