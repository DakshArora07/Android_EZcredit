package sfu.cmpt362.android_ezcredit.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sfu.cmpt362.android_ezcredit.ui.screens.User
import sfu.cmpt362.android_ezcredit.ui.screens.UserRole

data class CompanyProfileState(
    val companyName: String = "",
    val address: String = "",
    val phone: String = "",
    val users: List<User> = emptyList(),
    val showError: Boolean = false,
    val errorMessage: String = "",
    val isCompanyDetailsSaved: Boolean = false
)

class CompanyProfileScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(CompanyProfileState())
    val state: StateFlow<CompanyProfileState> = _state.asStateFlow()

    fun updateCompanyName(name: String) {
        if (!_state.value.isCompanyDetailsSaved) {
            _state.value = _state.value.copy(companyName = name)
        }
    }

    fun updateAddress(address: String) {
        if (!_state.value.isCompanyDetailsSaved) {
            _state.value = _state.value.copy(address = address)
        }
    }

    fun updatePhone(phone: String) {
        if (!_state.value.isCompanyDetailsSaved) {
            // Only allow digits and limit to 10 characters
            val filtered = phone.filter { it.isDigit() }.take(10)
            _state.value = _state.value.copy(phone = filtered)
        }
    }

    fun removeUser(userId: String) {
        val updatedUsers = _state.value.users.filter { it.id != userId }
        _state.value = _state.value.copy(users = updatedUsers)
    }

    fun isValidPhone(): Boolean {
        val phone = _state.value.phone
        return phone.isEmpty() || (phone.length == 10 && phone.all { it.isDigit() })
    }

    fun canSaveCompanyDetails(): Boolean {
        return _state.value.companyName.isNotBlank() &&
                _state.value.address.isNotBlank() &&
                _state.value.phone.isNotBlank() &&
                isValidPhone()
    }

    fun saveCompanyDetails(): Boolean {
        if (canSaveCompanyDetails()) {
            _state.value = _state.value.copy(
                isCompanyDetailsSaved = true,
                showError = false,
                errorMessage = ""
            )
            return true
        } else {
            val errorMsg = when {
                !isValidPhone() -> "Phone number must be exactly 10 digits"
                else -> "Please fill all required fields"
            }
            _state.value = _state.value.copy(
                showError = true,
                errorMessage = errorMsg
            )
            return false
        }
    }

    fun canGoToDashboard(): Boolean {
        val hasAdmin = _state.value.users.any { it.role == UserRole.ADMIN }
        return _state.value.isCompanyDetailsSaved && _state.value.users.isNotEmpty() && hasAdmin
    }

    fun goToDashboard(onSuccess: () -> Unit) {
        val hasAdmin = _state.value.users.any { it.role == UserRole.ADMIN }

        if (canGoToDashboard()) {
            // TODO: Save company and users to database
            _state.value = _state.value.copy(showError = false)
            onSuccess()
        } else {
            val errorMsg = when {
                !_state.value.isCompanyDetailsSaved -> "Please save company details first"
                _state.value.users.isEmpty() -> "At least one user is required"
                !hasAdmin -> "At least one Admin user is required"
                else -> "Cannot proceed to dashboard"
            }
            _state.value = _state.value.copy(
                showError = true,
                errorMessage = errorMsg
            )
        }
    }
    fun resetCompanyDetails() {
        _state.value = CompanyProfileState()
    }

    fun addUser(user: User) {
        val currentUsers = _state.value.users.toMutableList()
        currentUsers.add(user)
        _state.value = _state.value.copy(users = currentUsers)
    }
}