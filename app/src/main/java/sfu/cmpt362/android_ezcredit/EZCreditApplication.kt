package sfu.cmpt362.android_ezcredit

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.SyncManager

class EZCreditApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val database = AppDatabase.getInstance(this)
        val syncManager = SyncManager(database.customerDao, database.invoiceDao, applicationScope)
        syncManager.start()
    }
}