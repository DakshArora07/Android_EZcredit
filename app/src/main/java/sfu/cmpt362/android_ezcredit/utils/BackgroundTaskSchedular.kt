package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import sfu.cmpt362.android_ezcredit.workers.CreditScoreUpdateWorker
import sfu.cmpt362.android_ezcredit.workers.DailySummaryWorker
import sfu.cmpt362.android_ezcredit.workers.InvoiceReminderWorker
import sfu.cmpt362.android_ezcredit.workers.OverdueInvoiceStatusWorker
import sfu.cmpt362.android_ezcredit.workers.PaidInvoiceStatusWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Schedules all the workers
object BackgroundTaskSchedular {
    private const val TAG = "BackgroundTaskScheduler"
    private const val DAILY_SUMMARY_WORK_NAME = "daily_summary_work"
    private const val REMINDER_WORK_NAME = "invoice_reminder_work"
    private const val CREDIT_SCORE_UPDATE_WORK_NAME = "credit_score_update_work"
    private const val OVERDUE_INVOICE_STATUS_UPDATE_WORK_NAME = "overdue_invoice_status_update_work"
    private const val PAID_INVOICE_STATUS_UPDATE_WORK_NAME = "paid_invoice_status_update_work"

    // Daily Summary: Notification time set bu user in settings
    fun scheduleDailySummary(context: Context) {
        Log.d(TAG, "Scheduling daily summary")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()

        val hour = PreferenceManager.getSummaryReminderHour(context)
        val minute = PreferenceManager.getSummaryReminderMinute(context)

        val initialDelay = calculateInitialDelay(hour,minute)

        val summaryWork = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("credit_score_update")
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            DAILY_SUMMARY_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            summaryWork
        )

        val summaryWorkInfos = workManager.getWorkInfosForUniqueWork(DAILY_SUMMARY_WORK_NAME)
        summaryWorkInfos.get().forEach { workInfo ->
            val nextRunTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(workInfo.nextScheduleTimeMillis))

            Log.d(TAG, """
                    ====================================
                    Daily Summary Work Status:
                    State: ${workInfo.state}
                    Next run: $nextRunTime
                    Run attempt: ${workInfo.runAttemptCount}
                    Tags: ${workInfo.tags}
                    ====================================
                """.trimIndent())
        }

    }

    fun cancelDailySummary(context: Context) {
        Log.d(TAG, "Cancelling daily summary status updates")

        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(DAILY_SUMMARY_WORK_NAME)
        val summaryWorkInfos = workManager.getWorkInfosForUniqueWork(DAILY_SUMMARY_WORK_NAME)
        summaryWorkInfos.get().forEach { workInfo ->
            val nextRunTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(workInfo.nextScheduleTimeMillis))

            Log.d(TAG, """
                    ====================================
                    Daily Summary Work Status:
                    State: ${workInfo.state}
                    Next run: $nextRunTime
                    Run attempt: ${workInfo.runAttemptCount}
                    Tags: ${workInfo.tags}
                    ====================================
                """.trimIndent())
        }

    }

    // Invoice Reminder Emails: Email sending time selected by user in settings
    fun scheduleInvoiceReminders(context: Context) {
        Log.d(TAG, "Scheduling invoice reminders")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val hour = PreferenceManager.getInvoiceReminderHour(context)
        val minute = PreferenceManager.getInvoiceReminderMinute(context)

        val initialDelay = calculateInitialDelay(hour,minute)

        val reminderWork = PeriodicWorkRequestBuilder<InvoiceReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("invoice_reminders")
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderWork
        )

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
    }

    fun cancelInvoiceReminders(context: Context) {
        Log.d(TAG, "Cancelling invoice reminders")

        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(REMINDER_WORK_NAME)
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
    }

    // Runs at 12:02 am
    fun scheduleOverdueInvoiceWorker(context: Context) {
        Log.d(TAG, "Scheduling overdue invoice status updates")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val initialDelay = calculateInitialDelay(0,2)

        val overdueInvoiceStatusWorker = PeriodicWorkRequestBuilder<OverdueInvoiceStatusWorker>(
            24, TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("overdue_invoice_status_update")
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            OVERDUE_INVOICE_STATUS_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            overdueInvoiceStatusWorker
        )
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
    }

    // Runs at 12:04 am
    fun schedulePaidInvoiceWorker(context: Context) {
        Log.d(TAG, "Scheduling paid invoice status updates")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val initialDelay = calculateInitialDelay(0,4)

        val paidInvoiceStatusWorker = PeriodicWorkRequestBuilder<PaidInvoiceStatusWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("paid_invoice_status_update")
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            PAID_INVOICE_STATUS_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            paidInvoiceStatusWorker
        )
        val paidInvoiceStatusWorkerInfos = workManager.getWorkInfosForUniqueWork(PAID_INVOICE_STATUS_UPDATE_WORK_NAME)
        paidInvoiceStatusWorkerInfos.get().forEach { workInfo ->
            val nextRunTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(workInfo.nextScheduleTimeMillis))
            Log.d(TAG, """
                    ====================================
                    Paid Invoice Status Work Status:
                    State: ${workInfo.state}
                    Next run: $nextRunTime
                    Run attempt: ${workInfo.runAttemptCount}
                    Tags: ${workInfo.tags}
                    ====================================
                    """.trimIndent())
        }

    }

    // Runs at 12:06 am
    fun scheduleCreditScoreUpdate(context: Context) {
        Log.d(TAG, "Scheduling credit score updates")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val initialDelay = calculateInitialDelay(0,6)

        val creditScoreUpdateWork = PeriodicWorkRequestBuilder<CreditScoreUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("credit_score_update")
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            CREDIT_SCORE_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            creditScoreUpdateWork
        )
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
    }

    fun rescheduleAllEnabledTasks(context: Context) {
        Log.d(TAG, "Rescheduling all enabled tasks on app startup")

        // Check preferences and reschedule accordingly
        if (PreferenceManager.isInvoiceReminderEnabled(context)) {
            Log.d(TAG, "Invoice reminders were enabled - rescheduling")
            scheduleInvoiceReminders(context)
        }

        if (PreferenceManager.isDailySummaryEnabled(context)) {
            Log.d(TAG, "Daily summary was enabled - rescheduling")
            scheduleDailySummary(context)
        }
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}