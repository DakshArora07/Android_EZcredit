package sfu.cmpt362.android_ezcredit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import sfu.cmpt362.android_ezcredit.ui.NavigationDrawerScreen
import sfu.cmpt362.android_ezcredit.ui.theme.Android_EZCreditTheme
import sfu.cmpt362.android_ezcredit.utils.ReminderScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderScheduler.scheduleInvoiceReminders(this)
            Android_EZCreditTheme {
                NavigationDrawerScreen()
            }
        }
    }
}
