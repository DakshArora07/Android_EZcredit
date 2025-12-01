package sfu.cmpt362.android_ezcredit.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object CompanyContext {
    private const val PREFS_NAME = "ezcredit_company_prefs"
    private const val KEY_CURRENT_COMPANY_ID_LONG = "current_company_id_long"
    private const val KEY_CURRENT_USER_ID = "current_user_id"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var currentCompanyId: Long?
        get() = prefs.getLong(KEY_CURRENT_COMPANY_ID_LONG, 0L).takeIf { it > 0 }
        set(value) = prefs.edit { putLong(KEY_CURRENT_COMPANY_ID_LONG, value ?: 0L) }

    var currentUserId: String?
        get() = prefs.getString(KEY_CURRENT_USER_ID, null)
        set(value) = prefs.edit { putString(KEY_CURRENT_USER_ID, value) }

    fun clear() {
        prefs.edit { clear() }
    }

    fun isCompanySelected(): Boolean = currentCompanyId != null

    // Convenience getters for Firebase paths
    val currentCompanyIdString: String?
        get() = currentCompanyId?.toString()
}
