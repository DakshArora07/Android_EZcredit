package sfu.cmpt362.android_ezcredit.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.FirebaseAuthManager

data class LoginState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginScreenViewModel() : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val authManager =  FirebaseAuthManager()
    var loginResult by mutableStateOf<LoginResult>(LoginResult.Idle)
        private set

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(passwordVisible = !_state.value.passwordVisible)
    }

    fun resetLoginState() {
        _state.value = state.value.copy(
            isLoading = false,
            errorMessage = null
        )
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = state.value.copy(isLoading = true, errorMessage = null)
            val companyId = authManager.loginAndSetCompanyContext(email, password)
            _state.value = state.value.copy(isLoading = false)

            if (companyId != null) {
                onSuccess()
            } else {
                _state.value = state.value.copy(errorMessage = "Invalid credentials")
            }
        }
    }
}

sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val companyId: Long) : LoginResult()
    data class Error(val message: String) : LoginResult()
}