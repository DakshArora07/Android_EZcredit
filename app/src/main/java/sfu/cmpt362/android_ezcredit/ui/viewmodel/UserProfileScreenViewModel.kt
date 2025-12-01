package sfu.cmpt362.android_ezcredit.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sfu.cmpt362.android_ezcredit.ui.screens.UserRole

data class UserProfileState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.SALES,
    val showError: Boolean = false,
    val errorMessage: String = ""
)

class UserProfileScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun updateRole(role: UserRole) {
        _state.value = _state.value.copy(selectedRole = role)
    }

    fun isValidEmail(): Boolean {
        val email = _state.value.email
        return email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun canSave(existingUsers: List<sfu.cmpt362.android_ezcredit.ui.screens.User>): Boolean {
        val state = _state.value

        // Basic validation
        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            return false
        }

        if (!isValidEmail()) {
            return false
        }

        // Check if trying to add Admin when one already exists
        if (state.selectedRole == UserRole.ADMIN) {
            val hasAdmin = existingUsers.any { it.role == UserRole.ADMIN }
            if (hasAdmin) {
                return false
            }
        }

        return true
    }

    fun validateAndSave(
        existingUsers: List<sfu.cmpt362.android_ezcredit.ui.screens.User>,
        onSuccess: (name: String, email: String, password: String, role: UserRole) -> Unit
    ) {
        val state = _state.value

        when {
            state.name.isBlank() -> {
                _state.value = _state.value.copy(
                    showError = true,
                    errorMessage = "Name is required"
                )
            }
            state.email.isBlank() -> {
                _state.value = _state.value.copy(
                    showError = true,
                    errorMessage = "Email is required"
                )
            }
            !isValidEmail() -> {
                _state.value = _state.value.copy(
                    showError = true,
                    errorMessage = "Invalid email address"
                )
            }
            state.password.isBlank() -> {
                _state.value = _state.value.copy(
                    showError = true,
                    errorMessage = "Password is required"
                )
            }
            state.password.length < 6 -> {
                _state.value = _state.value.copy(
                    showError = true,
                    errorMessage = "Password must be at least 6 characters"
                )
            }
            state.selectedRole == UserRole.ADMIN && existingUsers.any { it.role == UserRole.ADMIN } -> {
                _state.value = _state.value.copy(
                    showError = true,
                    errorMessage = "Only one Admin user is allowed"
                )
            }
            else -> {
                _state.value = _state.value.copy(showError = false, errorMessage = "")
                onSuccess(state.name, state.email, state.password, state.selectedRole)
            }
        }
    }

    fun clearState() {
        _state.value = UserProfileState()
    }
}