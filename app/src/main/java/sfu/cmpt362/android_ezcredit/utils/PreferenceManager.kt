package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import androidx.core.content.edit

object PreferenceManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_INVOICE_REMINDER = "invoice_reminder_enabled"
    private const val KEY_REMINDER_TIME_HOUR = "invoice_reminder_hour"

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
}