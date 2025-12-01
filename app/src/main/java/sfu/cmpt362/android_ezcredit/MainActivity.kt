package sfu.cmpt362.android_ezcredit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import sfu.cmpt362.android_ezcredit.ui.NavigationDrawerScreen
import sfu.cmpt362.android_ezcredit.ui.screens.CompanyProfileScreen
import sfu.cmpt362.android_ezcredit.ui.screens.LoginScreen
import sfu.cmpt362.android_ezcredit.ui.theme.Android_EZCreditTheme
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                BackgroundTaskSchedular.initializeAllTasks(this)
            } else {
                // Permission denied: optionally notify user or proceed without reminders
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    BackgroundTaskSchedular.initializeAllTasks(this)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            BackgroundTaskSchedular.initializeAllTasks(this)
        }

        setContent {
            Android_EZCreditTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var showCompanyProfile by remember { mutableStateOf(false)}

                when {
                    isLoggedIn -> {
                        NavigationDrawerScreen()
                    }
                    showCompanyProfile -> {
                        CompanyProfileScreen(
                            onCancel = { showCompanyProfile = false },
                            onSave = { showCompanyProfile = false
                            isLoggedIn = true},
                        )
                    }
                    else -> {
                        LoginScreen(
                            onLoginSuccess = { isLoggedIn = true },
                            onCreateCompany = { showCompanyProfile = true }
                        )
                    }
                }
            }
        }
    }
}
