package sfu.cmpt362.android_ezcredit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.ui.NavigationDrawerScreen
import sfu.cmpt362.android_ezcredit.ui.screens.CompanyProfileScreen
import sfu.cmpt362.android_ezcredit.ui.screens.LoginScreen
import sfu.cmpt362.android_ezcredit.ui.screens.UserProfileScreen
import sfu.cmpt362.android_ezcredit.ui.theme.Android_EZCreditTheme
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CompanyProfileScreenViewModel
import androidx.compose.runtime.collectAsState
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Android_EZCreditTheme {
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }
                var showCompanyProfile by rememberSaveable { mutableStateOf(false) }
                var showUserProfile by rememberSaveable { mutableStateOf(false) }
                val companyViewModel: CompanyProfileScreenViewModel = viewModel()
                when {
                    isLoggedIn -> {
                        NavigationDrawerScreen()
                    }
                    showUserProfile -> {
                        val existingUsers = companyViewModel.state.collectAsState().value.users
                        UserProfileScreen(
                            existingUsers = existingUsers,
                            onCancel = { showUserProfile = false },
                            onSave = { newUser ->
                                companyViewModel.addUser(newUser)
                                showUserProfile = false
                            }
                        )
                    }
                    showCompanyProfile -> {
                        CompanyProfileScreen(
                            onCancel = { showCompanyProfile = false },
                            onSave = { showCompanyProfile = false
                                isLoggedIn = true
                            },
                            onAddUser = { showUserProfile = true }
                        )
                    }
                    else -> {
                        LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true

                                BackgroundTaskSchedular.scheduleOverdueInvoiceWorker(this)
                                BackgroundTaskSchedular.schedulePaidInvoiceWorker(this)
                                BackgroundTaskSchedular.scheduleCreditScoreUpdate(this)
                                BackgroundTaskSchedular.rescheduleAllEnabledTasks(this)
                            },
                            onCreateCompany = { showCompanyProfile = true }
                        )
                    }
                }
            }
        }
    }
}