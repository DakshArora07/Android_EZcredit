package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import androidx.core.content.edit

object PreferenceManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_INVOICE_REMINDER = "invoice_reminder_enabled"
    private const val KEY_REMINDER_TIME_HOUR = "invoice_reminder_hour"
    private const val KEY_SUMMARY_TIME_HOUR = "summary_time_hour"
    private const val KEY_REMINDER_TIME_MIN = "invoice_reminder_min"
    private const val KEY_SUMMARY_TIME_MIN = "summary_time_min"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setInvoiceReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit {
            putBoolean(KEY_INVOICE_REMINDER, enabled)
        }
    }

    fun setInvoiceReminderHour(context: Context, hour: Int) {
        prefs(context).edit {
            putInt(KEY_REMINDER_TIME_HOUR, hour)
        }
    }

    fun getInvoiceReminderHour(context: Context): Int {
        return prefs(context).getInt(KEY_REMINDER_TIME_HOUR, 9) // default 9 AM
    }

    fun isInvoiceReminderEnabled(context: Context) : Boolean {
        return prefs(context).getBoolean(KEY_INVOICE_REMINDER, true)
    }

    fun getSummaryReminderHour(context: Context): Int {
        return prefs(context).getInt(KEY_SUMMARY_TIME_HOUR, 10) // default 10 AM
    }

    fun setSummaryReminderHour(context: Context, hour: Int) {
        prefs(context).edit {
            putInt(KEY_SUMMARY_TIME_HOUR, hour)
        }
    }

    fun setInvoiceReminderMinute(context: Context, minute: Int) {
        prefs(context).edit { putInt(KEY_REMINDER_TIME_MIN, minute) }
    }

    fun getInvoiceReminderMinute(context: Context): Int {
        return prefs(context).getInt(KEY_REMINDER_TIME_MIN, 0)
    }

    fun setSummaryReminderMinute(context: Context, minute: Int) {
        prefs(context).edit { putInt(KEY_SUMMARY_TIME_MIN, minute) }
    }

    fun getSummaryReminderMinute(context: Context): Int {
        return prefs(context).getInt(KEY_SUMMARY_TIME_MIN, 0)
    }


}