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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import sfu.cmpt362.android_ezcredit.ui.NavigationDrawerScreen
import sfu.cmpt362.android_ezcredit.ui.screens.CompanyProfileScreen
import sfu.cmpt362.android_ezcredit.ui.screens.LoginScreen
import sfu.cmpt362.android_ezcredit.ui.theme.Android_EZCreditTheme
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Android_EZCreditTheme {
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }
                var showCompanyProfile by rememberSaveable { mutableStateOf(false)}

                when {
                    isLoggedIn -> {
                        NavigationDrawerScreen()
                    }
                    showCompanyProfile -> {
                        CompanyProfileScreen(
                            onCancel = { showCompanyProfile = false },
                            onSave = { showCompanyProfile = false
                                isLoggedIn = true },
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
