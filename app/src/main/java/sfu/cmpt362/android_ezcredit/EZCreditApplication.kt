package sfu.cmpt362.android_ezcredit

import android.app.Application
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.SyncManager

class EZCreditApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncManager: SyncManager? = null

    companion object {
        private const val TAG = "EZCreditApplication"
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        CompanyContext.init(this)

        val database = AppDatabase.getInstance(this)
        syncManager = SyncManager(
            database.companyDao,
            database.userDao,
            database.customerDao,
            database.invoiceDao,
            database.receiptDao,
            applicationScope
        )

        syncManager?.startInitialSync()
    }

    fun checkAndSyncOnStartup() {
        val currentCompanyId = CompanyContext.currentCompanyId

        Log.d(TAG, "App startup - Current company: $currentCompanyId")

        if (currentCompanyId != null) {
            Log.d(TAG, "User logged in, syncing company data")
            syncManager?.startCompanyDataSync(currentCompanyId)
        }
    }

    fun restartSyncAfterLogin() {
        applicationScope.launch {
            val companyId = CompanyContext.currentCompanyId
            if (companyId != null) {
                Log.d(TAG, "Login completed, clearing old data and syncing company $companyId")

                syncManager?.clearCompanyDataSync()
                syncManager?.startCompanyDataSync(companyId)
            }
        }
    }

    fun clearOnLogout() {
        applicationScope.launch {
            Log.d(TAG, "Logging out, clearing all company data")

            // Clear all company-specific data
            syncManager?.clearCompanyDataSync()
        }
    }
}