package sfu.cmpt362.android_ezcredit.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sfu.cmpt362.android_ezcredit.ui.screens.User

data class CompanyProfileState(
    val companyName: String = "",
    val address: String = "",
    val phone: String = "",
    val users: List<User> = emptyList(),
    val showError: Boolean = false,
    val errorMessage: String = ""
)

class CompanyProfileScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(CompanyProfileState())
    val state: StateFlow<CompanyProfileState> = _state.asStateFlow()

    fun updateCompanyName(name: String) {
        _state.value = _state.value.copy(companyName = name)
    }

    fun updateAddress(address: String) {
        _state.value = _state.value.copy(address = address)
    }

    fun updatePhone(phone: String) {
        val filtered = phone.filter { it.isDigit() }.take(10)
        _state.value = _state.value.copy(phone = filtered)
    }

    fun removeUser(userId: String) {
        val updatedUsers = _state.value.users.filter { it.id != userId }
        _state.value = _state.value.copy(users = updatedUsers)
    }

    fun isValidPhone(): Boolean {
        val phone = _state.value.phone
        return phone.isEmpty() || (phone.length == 10 && phone.all { it.isDigit() })
    }


    fun canSave(): Boolean {
        return _state.value.companyName.isNotBlank() &&
                _state.value.address.isNotBlank() &&
                _state.value.phone.isNotBlank() &&
                isValidPhone() &&
                _state.value.users.isNotEmpty()
    }

    fun validateAndSave(onSuccess: () -> Unit) {
        if (canSave()) {
            // TODO: Implement actual save logic
            _state.value = _state.value.copy(showError = false)
            onSuccess()
        } else {
            val errorMsg = when {
                _state.value.users.isEmpty() -> "At least one user is required"
                !isValidPhone() -> "Phone number must be exactly 10 digits"
                else -> "Please fill all required fields"
            }
            _state.value = _state.value.copy(
                showError = true,
                errorMessage = errorMsg
            )
        }
    }

}