package sfu.cmpt362.android_ezcredit.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import sfu.cmpt362.android_ezcredit.ui.screens.User
import sfu.cmpt362.android_ezcredit.ui.screens.UserRole

data class UserProfileScreenState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.SALES,
    val showError: Boolean = false,
    val errorMessage: String = ""
)

class UserProfileScreenViewModel : ViewModel() {

    private val _state = MutableStateFlow(UserProfileScreenState())
    val state: StateFlow<UserProfileScreenState> = _state.asStateFlow()

    fun updateName(name: String) {
        _state.update { it.copy(name = name, showError = false) }
    }

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email, showError = false) }
    }

    fun updatePassword(password: String) {
        _state.update { it.copy(password = password, showError = false) }
    }

    fun updateRole(role: UserRole) {
        _state.update { it.copy(selectedRole = role, showError = false) }
    }

    fun isValidEmail(): Boolean {
        val currentState = _state.value
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return currentState.email.matches(emailPattern.toRegex())
    }

    fun canSave(existingUsers: List<User>): Boolean {
        val currentState = _state.value

        // Check if all fields are filled
        if (currentState.name.isBlank() ||
            currentState.email.isBlank() ||
            currentState.password.isBlank()) {
            return false
        }

        // Check email format
        if (!isValidEmail()) {
            return false
        }

        // Check password length
        if (currentState.password.length < 6) {
            return false
        }

        // Check if admin already exists when trying to add admin
        val hasAdmin = existingUsers.any { it.role == UserRole.ADMIN }
        if (hasAdmin && currentState.selectedRole == UserRole.ADMIN) {
            return false
        }

        return true
    }

    fun validateAndSave(
        existingUsers: List<User>,
        onSuccess: (name: String, email: String, password: String, role: UserRole) -> Unit
    ) {
        val currentState = _state.value

        // Validate name
        if (currentState.name.isBlank()) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Name is required"
                )
            }
            return
        }

        // Validate email
        if (currentState.email.isBlank()) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Email is required"
                )
            }
            return
        }

        if (!isValidEmail()) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Invalid email address"
                )
            }
            return
        }

        // Check for duplicate email
        if (existingUsers.any { it.email == currentState.email }) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Email already exists"
                )
            }
            return
        }

        // Validate password
        if (currentState.password.isBlank()) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Password is required"
                )
            }
            return
        }

        if (currentState.password.length < 6) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Password must be at least 6 characters"
                )
            }
            return
        }

        // Check if admin already exists
        val hasAdmin = existingUsers.any { it.role == UserRole.ADMIN }
        if (hasAdmin && currentState.selectedRole == UserRole.ADMIN) {
            _state.update {
                it.copy(
                    showError = true,
                    errorMessage = "Only one Admin user is allowed"
                )
            }
            return
        }

        // All validations passed, call success callback
        onSuccess(
            currentState.name,
            currentState.email,
            currentState.password,
            currentState.selectedRole
        )
    }

    fun clearState() {
        _state.value = UserProfileScreenState()
    }
}