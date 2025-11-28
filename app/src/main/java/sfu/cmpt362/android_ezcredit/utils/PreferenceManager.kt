package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import androidx.core.content.edit

object PreferenceManager {

    private const val PREF_NAME = "app_settings"
    private const val KEY_INVOICE_REMINDER = "invoice_reminder_enabled"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setInvoiceReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit {
            putBoolean(KEY_INVOICE_REMINDER, enabled)
        }
    }

    fun isInvoiceReminderEnabled(context: Context) : Boolean {
        return prefs(context).getBoolean(KEY_INVOICE_REMINDER, true)
    }
}