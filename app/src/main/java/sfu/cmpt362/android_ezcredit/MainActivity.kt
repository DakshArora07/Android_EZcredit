package sfu.cmpt362.android_ezcredit

import android.annotation.SuppressLint
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.repository.CompanyRepository
import sfu.cmpt362.android_ezcredit.data.repository.UserRepository
import sfu.cmpt362.android_ezcredit.data.viewmodel.CompanyViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CompanyProfileScreenViewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CompanyProfileScreenViewModelFactory
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular

class MainActivity : ComponentActivity() {

    @SuppressLint("ViewModelConstructorInComposable")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Android_EZCreditTheme {
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }
                var showCompanyProfile by rememberSaveable { mutableStateOf(false) }
                var showUserProfile by rememberSaveable { mutableStateOf(false) }

                val context = LocalContext.current

                // Get EZCreditApplication instance
                val application = context.applicationContext as EZCreditApplication

                // Get database instance
                val database = AppDatabase.getInstance(context)

                // Create repositories from database DAOs
                val companyRepository = CompanyRepository(database.companyDao)
                val userRepository = UserRepository(database.userDao)

                // Create database ViewModels
                val companyViewModel = CompanyViewModel(companyRepository)
                val userViewModel = UserViewModel(userRepository)

                // Create UI ViewModel with factory
                val companyProfileViewModel: CompanyProfileScreenViewModel = viewModel(
                    factory = CompanyProfileScreenViewModelFactory(
                        companyViewModel = companyViewModel,
                        userViewModel = userViewModel
                    )
                )
                when {
                    isLoggedIn -> {
                        NavigationDrawerScreen()
                    }
                    showUserProfile -> {
                        val state by companyProfileViewModel.state.collectAsState()
                        val existingUsers = state.users

                        UserProfileScreen(
                            existingUsers = existingUsers,
                            onCancel = { showUserProfile = false },
                            onSave = { newUser ->
                                companyProfileViewModel.addUser(newUser)
                                showUserProfile = false
                            }
                        )
                    }
                    showCompanyProfile -> {
                        CompanyProfileScreen(
                            viewModel = companyProfileViewModel,
                            onCancel = { showCompanyProfile = false },
                            onSave = { showCompanyProfile = false },
                            onAddUser = { showUserProfile = true }
                        )
                    }
                    else -> {
                        LoginScreen(
                            onLoginSuccess = { isLoggedIn = true
                                BackgroundTaskSchedular.scheduleOverdueInvoiceWorker(this)
                                BackgroundTaskSchedular.schedulePaidInvoiceWorker(this)
                                BackgroundTaskSchedular.scheduleCreditScoreUpdate(this)
                                BackgroundTaskSchedular.rescheduleAllEnabledTasks(this)},
                            onCreateCompany = { showCompanyProfile = true },
                            application = application,
                        )
                    }
                }
            }
        }
    }
}