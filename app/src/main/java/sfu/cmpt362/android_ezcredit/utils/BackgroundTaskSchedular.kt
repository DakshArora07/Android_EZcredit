package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import sfu.cmpt362.android_ezcredit.workers.CreditScoreUpdateWorker
import sfu.cmpt362.android_ezcredit.workers.InvoiceReminderWorker
import sfu.cmpt362.android_ezcredit.workers.OverdueInvoiceStatusWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object BackgroundTaskSchedular {

    private const val TAG = "BackgroundTaskScheduler"
    private const val REMINDER_WORK_NAME = "invoice_reminder_work"
    private const val CREDIT_SCORE_UPDATE_WORK_NAME = "credit_score_update_work"
    private const val OVERDUE_INVOICE_STATUS_UPDATE_WORK_NAME = "overdue_invoice_status_update_work"

    fun initializeAllTasks(context: Context) {
        if (PreferenceManager.isInvoiceReminderEnabled(context)) {
            scheduleInvoiceReminders(context)
        } else {
            cancelInvoiceReminders(context)
        }
        scheduleCreditScoreUpdate(context)
        scheduleOverdueInvoiceWorker(context)
        checkWorkStatus(context)
    }

    fun scheduleInvoiceReminders(context: Context) {
        Log.d(TAG, "Scheduling invoice reminders")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val initialDelay = calculateInitialDelay(10) // Send updates every day at 10 am

        val reminderWork = PeriodicWorkRequestBuilder<InvoiceReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.SECONDS)
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

        val initialDelay = calculateInitialDelay(1) // Credit score updates every night at 1 am

        val creditScoreUpdateWork = PeriodicWorkRequestBuilder<CreditScoreUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("credit_score_update")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CREDIT_SCORE_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            creditScoreUpdateWork
        )
    }

    fun scheduleOverdueInvoiceWorker(context: Context) {
        Log.d(TAG, "Scheduling invoice status updates")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val initialDelay = calculateInitialDelay(0) // Invoice status updates every day at 12 am


        val overdueInvoiceStatusWorker = PeriodicWorkRequestBuilder<OverdueInvoiceStatusWorker>(
            24, TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .addTag("overdue_invoice_status_update")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            OVERDUE_INVOICE_STATUS_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            overdueInvoiceStatusWorker
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

            val overdueInvoiceStatusWorkerInfos = workManager.getWorkInfosForUniqueWork(OVERDUE_INVOICE_STATUS_UPDATE_WORK_NAME)
            overdueInvoiceStatusWorkerInfos.get().forEach { workInfo ->
                val nextRunTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(workInfo.nextScheduleTimeMillis))
                Log.d(TAG, """
                    ====================================
                    Overdue Invoice Status Work Status:
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