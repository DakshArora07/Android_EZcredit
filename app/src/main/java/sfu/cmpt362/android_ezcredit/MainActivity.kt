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
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.FirebaseAuthManager
import sfu.cmpt362.android_ezcredit.data.repository.CompanyRepository
import sfu.cmpt362.android_ezcredit.data.repository.UserRepository
import sfu.cmpt362.android_ezcredit.data.viewmodel.CompanyViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CompanyProfileScreenViewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CompanyProfileScreenViewModelFactory
import sfu.cmpt362.android_ezcredit.utils.AccessMode
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular
import sfu.cmpt362.android_ezcredit.ui.screens.UserRole

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
                var showUserProfileFromSettings by rememberSaveable { mutableStateOf(false) }
                var showCompanyProfileFromSettings by rememberSaveable { mutableStateOf(false) }
                var showAddUserFromCompanyProfile by rememberSaveable { mutableStateOf(false) }

                val context = LocalContext.current
                val application = context.applicationContext as EZCreditApplication
                val database = AppDatabase.getInstance(context)

                val companyRepository = CompanyRepository(database.companyDao)
                val userRepository = UserRepository(database.userDao)

                val companyViewModel = CompanyViewModel(companyRepository)
                val userViewModel = UserViewModel(userRepository)

                val companyProfileViewModel: CompanyProfileScreenViewModel = viewModel(
                    factory = CompanyProfileScreenViewModelFactory(
                        companyViewModel = companyViewModel,
                        userViewModel = userViewModel
                    )
                )

                when {
                    showAddUserFromCompanyProfile -> {
                        val state by companyProfileViewModel.state.collectAsState()

                        UserProfileScreen(
                            existingUsers = state.users,
                            isEditMode = false,
                            onCancel = {
                                showAddUserFromCompanyProfile = false
                                showCompanyProfileFromSettings = true
                            },
                            onSave = { newUser ->
                                companyProfileViewModel.addUserToFirebase(newUser)
                                showAddUserFromCompanyProfile = false
                                showCompanyProfileFromSettings = true
                            }
                        )
                    }
                    showUserProfileFromSettings -> {
                        val authManager = FirebaseAuthManager()
                        val currentUserEmail = authManager.currentUser?.email

                        UserProfileScreen(
                            isEditMode = true,
                            currentUserEmail = currentUserEmail,
                            onCancel = {
                                showUserProfileFromSettings = false
                            },
                            onSave = { updatedUser ->
                                // Get current company ID
                                val currentCompanyId = CompanyContext.currentCompanyId

                                if (currentCompanyId != null) {
                                    // Update in local database
                                    val accessMode = when (updatedUser.role) {
                                        UserRole.ADMIN -> AccessMode.Admin
                                        UserRole.SALES -> AccessMode.Sales
                                        UserRole.RECEIPTS -> AccessMode.Receipts
                                    }

                                    // Find user ID from email
                                    val userId = CompanyContext.currentUserId

                                    if (userId != null) {
                                        userViewModel.updateUser(
                                            userId = userId,
                                            name = updatedUser.name,
                                            email = updatedUser.email,
                                            companyId = currentCompanyId,
                                            accessLevel = accessMode
                                        )
                                        userViewModel.update()
                                    }
                                }

                                showUserProfileFromSettings = false
                            }
                        )
                    }
                    showCompanyProfileFromSettings -> {
                        CompanyProfileScreen(
                            viewModel = companyProfileViewModel,
                            isViewMode = true,
                            onCancel = {
                                showCompanyProfileFromSettings = false
                            },
                            onSave = {
                                showCompanyProfileFromSettings = false
                            },
                            onAddUser = {
                                showCompanyProfileFromSettings = false
                                showAddUserFromCompanyProfile = true
                            }
                        )
                    }
                    isLoggedIn -> {
                        NavigationDrawerScreen(
                            onLogout = {
                                val authManager = FirebaseAuthManager()
                                authManager.signOut()
                                isLoggedIn = false
                            },
                            onProfileClick = {
                                showUserProfileFromSettings = true
                            },
                            onCompanyProfileClick = {
                                showCompanyProfileFromSettings = true
                            }
                        )
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
                            onLoginSuccess = {
                                isLoggedIn = true
                                BackgroundTaskSchedular.scheduleOverdueInvoiceWorker(this)
                                BackgroundTaskSchedular.schedulePaidInvoiceWorker(this)
                                BackgroundTaskSchedular.scheduleCreditScoreUpdate(this)
                                BackgroundTaskSchedular.rescheduleAllEnabledTasks(this)
                            },
                            onCreateCompany = { showCompanyProfile = true },
                            application = application,
                        )
                    }
                }
            }
        }
    }
}