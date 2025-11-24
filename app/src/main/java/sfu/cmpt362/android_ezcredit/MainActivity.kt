package sfu.cmpt362.android_ezcredit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import sfu.cmpt362.android_ezcredit.ui.NavigationDrawerScreen
import sfu.cmpt362.android_ezcredit.ui.theme.Android_EZCreditTheme
import sfu.cmpt362.android_ezcredit.utils.ReminderScheduler

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                ReminderScheduler.scheduleInvoiceReminders(this)
            } else {
                // Permission denied: optionally notify user or proceed without reminders
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    ReminderScheduler.scheduleInvoiceReminders(this)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            ReminderScheduler.scheduleInvoiceReminders(this)
        }

        setContent {
            Android_EZCreditTheme {
                NavigationDrawerScreen()
            }
        }
    }
}
