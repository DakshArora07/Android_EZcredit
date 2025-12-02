package sfu.cmpt362.android_ezcredit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.viewmodel.CompanyViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModel
import sfu.cmpt362.android_ezcredit.ui.screens.User
import sfu.cmpt362.android_ezcredit.ui.screens.UserRole
import sfu.cmpt362.android_ezcredit.utils.AccessMode

data class CompanyProfileScreenState(
    val companyName: String = "",
    val address: String = "",
    val phone: String = "",
    val users: List<User> = emptyList(),
    val isCompanyDetailsSaved: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String = ""
)

class CompanyProfileScreenViewModel(
    private val companyViewModel: CompanyViewModel,
    private val userViewModel: UserViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(CompanyProfileScreenState())
    val state: StateFlow<CompanyProfileScreenState> = _state.asStateFlow()

    private var companyId: Long = 0L

    fun updateCompanyName(name: String) {
        _state.update { it.copy(companyName = name, showError = false) }
    }

    fun updateAddress(address: String) {
        _state.update { it.copy(address = address, showError = false) }
    }

    fun updatePhone(phone: String) {
        // Only allow digits and limit to 10
        val filtered = phone.filter { it.isDigit() }.take(10)
        _state.update { it.copy(phone = filtered, showError = false) }
    }

    fun addUser(user: User) {
        _state.update { currentState ->
            currentState.copy(
                users = currentState.users + user,
                showError = false
            )
        }
    }

    fun removeUser(userId: String) {
        _state.update { currentState ->
            currentState.copy(
                users = currentState.users.filter { it.id != userId },
                showError = false
            )
        }
    }

    fun saveCompanyDetails() {
        val currentState = _state.value

        // Validate company details
        if (currentState.companyName.isBlank()) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Company name is required"
                )
            }
            return
        }

        if (currentState.address.isBlank()) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Address is required"
                )
            }
            return
        }

        if (currentState.phone.isBlank()) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Phone is required"
                )
            }
            return
        }

        if (currentState.phone.length != 10) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Phone number must be 10 digits"
                )
            }
            return
        }

        // Save company to database
        viewModelScope.launch {
            try {
                companyViewModel.updateCompany(
                    companyId = companyId,
                    name = currentState.companyName,
                    address = currentState.address,
                    phone = currentState.phone
                )

                companyId = companyViewModel.insert()

                _state.update {
                    it.copy(
                        isCompanyDetailsSaved = true,
                        showError = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        showError = true,
                        errorMessage = "Failed to save company details: ${e.message}"
                    )
                }
            }
        }
    }

    fun goToDashboard(onSuccess: () -> Unit) {
        val currentState = _state.value

        // Validate that at least one admin user exists
        val hasAdmin = currentState.users.any { it.role == UserRole.ADMIN }

        if (!hasAdmin) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "At least one Admin user is required"
                )
            }
            return
        }

        // Save all users to database
        viewModelScope.launch {
            try {
                // Save each user to the database
                for (user in currentState.users) {
                    val accessMode = when (user.role) {
                        UserRole.ADMIN -> AccessMode.Admin
                        UserRole.SALES -> AccessMode.Sales
                        UserRole.RECEIPTS -> AccessMode.Receipts
                    }

                    userViewModel.updateUser(
                        userId = 0L,
                        name = user.name,
                        email = user.email,
                        companyId = companyId,
                        accessLevel = accessMode
                    )

                    CompanyContext.currentUserId = userViewModel.insert()
                }

                // Set the company context
                CompanyContext.currentCompanyId = companyId

                // Navigate to dashboard
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        showError = true,
                        errorMessage = "Failed to save users: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetCompanyDetails() {
        _state.update {
            it.copy(
                companyName = "",
                address = "",
                phone = "",
                showError = false
            )
        }
    }
}