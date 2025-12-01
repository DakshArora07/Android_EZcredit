package sfu.cmpt362.android_ezcredit

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.SyncManager

class EZCreditApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncManager: SyncManager? = null

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        CompanyContext.init(this)

        // ALWAYS sync first - brings down existing companies/users/customers/etc.
        val database = AppDatabase.getInstance(this)
        syncManager = SyncManager(
            database.companyDao,
            database.userDao,
            database.customerDao,
            database.invoiceDao,
            database.receiptDao,
            applicationScope
        )
        syncManager!!.start() // Downloads: Companies→Users→Customers→Invoices→Receipts
    }

    // Call this to restart sync after login (keeps live listeners active)
    fun restartSyncAfterLogin() {
        syncManager?.start() // Re-triggers live listeners
    }
}