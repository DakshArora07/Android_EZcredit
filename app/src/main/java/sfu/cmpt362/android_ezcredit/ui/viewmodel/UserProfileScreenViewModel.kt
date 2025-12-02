package sfu.cmpt362.android_ezcredit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.FirebaseRefs
import sfu.cmpt362.android_ezcredit.ui.screens.User
import sfu.cmpt362.android_ezcredit.ui.screens.UserRole

data class UserProfileScreenState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.SALES,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val isLoading: Boolean = false
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

    // Load user data from Firebase by email
    fun loadUserData(email: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                val usersSnapshot = FirebaseRefs.usersRef(currentCompanyId).get().await()

                usersSnapshot.children.forEach { userSnap ->
                    val userEmail = userSnap.child("email").getValue(String::class.java) ?: ""

                    if (userEmail == email) {
                        val name = userSnap.child("name").getValue(String::class.java) ?: ""

                        // Try both "role" and "accessLevel" fields
                        var roleString = userSnap.child("role").getValue(String::class.java)
                        if (roleString == null) {
                            roleString = userSnap.child("accessLevel").getValue(String::class.java)
                        }

                        Log.d("UserProfileVM", "Loading user: $name, Email: $email, Role: $roleString")

                        val role = when (roleString?.uppercase()) {
                            "ADMIN" -> UserRole.ADMIN
                            "SALES" -> UserRole.SALES
                            "RECEIPTS" -> UserRole.RECEIPTS
                            else -> UserRole.SALES
                        }

                        _state.update {
                            it.copy(
                                name = name,
                                email = userEmail,
                                selectedRole = role,
                                isLoading = false,
                                showError = false
                            )
                        }

                        Log.d("UserProfileVM", "User data loaded successfully")
                        return@launch
                    }
                }

                _state.update { it.copy(isLoading = false) }
                Log.w("UserProfileVM", "User with email $email not found")
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Error loading user data", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        showError = true,
                        errorMessage = "Failed to load user data: ${e.message}"
                    )
                }
            }
        }
    }

    // Update user in Firebase
    fun updateUserInFirebase(email: String, newName: String) {
        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                val usersSnapshot = FirebaseRefs.usersRef(currentCompanyId).get().await()

                usersSnapshot.children.forEach { userSnap ->
                    val userEmail = userSnap.child("email").getValue(String::class.java) ?: ""

                    if (userEmail == email) {
                        val firebaseKey = userSnap.key ?: return@forEach

                        val updates = hashMapOf<String, Any>(
                            "name" to newName,
                            "lastModified" to System.currentTimeMillis()
                        )

                        FirebaseRefs.usersRef(currentCompanyId).child(firebaseKey)
                            .updateChildren(updates).await()

                        Log.d("UserProfileVM", "User name updated in Firebase: $newName")
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Error updating user in Firebase", e)
                _state.update {
                    it.copy(
                        showError = true,
                        errorMessage = "Failed to update user: ${e.message}"
                    )
                }
            }
        }
    }

    fun isValidEmail(): Boolean {
        val currentState = _state.value
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return currentState.email.matches(emailPattern.toRegex())
    }

    fun canSave(existingUsers: List<User>): Boolean {
        val currentState = _state.value

        if (currentState.name.isBlank() ||
            currentState.email.isBlank() ||
            currentState.password.isBlank()) {
            return false
        }

        if (!isValidEmail()) {
            return false
        }

        if (currentState.password.length < 6) {
            return false
        }

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

        if (currentState.name.isBlank()) {
            _state.update {
                it.copy(showError = true, errorMessage = "Name is required")
            }
            return
        }

        if (currentState.email.isBlank()) {
            _state.update {
                it.copy(showError = true, errorMessage = "Email is required")
            }
            return
        }

        if (!isValidEmail()) {
            _state.update {
                it.copy(showError = true, errorMessage = "Invalid email address")
            }
            return
        }

        if (existingUsers.any { it.email == currentState.email }) {
            _state.update {
                it.copy(showError = true, errorMessage = "Email already exists")
            }
            return
        }

        if (currentState.password.isBlank()) {
            _state.update {
                it.copy(showError = true, errorMessage = "Password is required")
            }
            return
        }

        if (currentState.password.length < 6) {
            _state.update {
                it.copy(showError = true, errorMessage = "Password must be at least 6 characters")
            }
            return
        }

        val hasAdmin = existingUsers.any { it.role == UserRole.ADMIN }
        if (hasAdmin && currentState.selectedRole == UserRole.ADMIN) {
            _state.update {
                it.copy(showError = true, errorMessage = "Only one Admin user is allowed")
            }
            return
        }

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

    fun setError(message: String) {
        _state.update { it.copy(showError = true, errorMessage = message) }
    }
}