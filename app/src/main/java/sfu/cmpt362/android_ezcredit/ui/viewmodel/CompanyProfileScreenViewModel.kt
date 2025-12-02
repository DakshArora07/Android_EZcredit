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

        // Remove from Firebase
        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId
                if (currentCompanyId != null) {
                    FirebaseRefs.usersRef(currentCompanyId).child(userId).removeValue().await()
                    Log.d("CompanyProfileVM", "User removed from Firebase: $userId")
                }
            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error removing user", e)
            }
        }
    }

    // Add user to Firebase and create auth account
    fun addUserToFirebase(user: User) {
        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                val usersRef = FirebaseRefs.usersRef(currentCompanyId)
                val newUserRef = usersRef.push()
                val firebaseKey = newUserRef.key ?: return@launch

                val roleString = when (user.role) {
                    UserRole.ADMIN -> "ADMIN"
                    UserRole.SALES -> "SALES"
                    UserRole.RECEIPTS -> "RECEIPTS"
                }

                // Create user data
                val userData = hashMapOf<String, Any>(
                    "name" to user.name,
                    "email" to user.email,
                    "role" to roleString,
                    "accessLevel" to roleString,
                    "lastModified" to System.currentTimeMillis(),
                    "isDeleted" to false
                )

                newUserRef.setValue(userData).await()
                Log.d("CompanyProfileVM", "User added to Firebase: $firebaseKey")

                // Reload to refresh UI
                loadCompanyData()
            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error adding user", e)
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to add user: ${e.message}")
                }
            }
        }
    }

    // Load company data from Firebase
    fun loadCompanyData() {
        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                // Load company details
                val companySnapshot = FirebaseRefs.companiesRef()
                    .child(currentCompanyId.toString()).get().await()

                val companyName = companySnapshot.child("name").getValue(String::class.java) ?: ""
                val address = companySnapshot.child("address").getValue(String::class.java) ?: ""
                val phone = companySnapshot.child("phone").getValue(String::class.java) ?: ""

                // Load users
                val usersSnapshot = FirebaseRefs.usersRef(currentCompanyId).get().await()

                val uiUsers = mutableListOf<User>()
                usersSnapshot.children.forEach { userSnap ->
                    val firebaseKey = userSnap.key ?: return@forEach
                    val name = userSnap.child("name").getValue(String::class.java) ?: ""
                    val email = userSnap.child("email").getValue(String::class.java) ?: ""

                    // Try both fields
                    var roleString = userSnap.child("role").getValue(String::class.java)
                    if (roleString == null) {
                        roleString = userSnap.child("accessLevel").getValue(String::class.java)
                    }

                    Log.d("CompanyProfileVM", "User: $name, Role: $roleString")

                    val role = when (roleString?.uppercase()) {
                        "ADMIN" -> UserRole.ADMIN
                        "SALES" -> UserRole.SALES
                        "RECEIPTS" -> UserRole.RECEIPTS
                        else -> UserRole.SALES
                    }

                    uiUsers.add(User(id = firebaseKey, name = name, email = email, role = role))
                }

                companyId = currentCompanyId

                _state.update {
                    it.copy(
                        companyName = companyName,
                        address = address,
                        phone = phone,
                        users = uiUsers,
                        isCompanyDetailsSaved = true,
                        showError = false
                    )
                }
            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error loading company data", e)
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to load: ${e.message}")
                }
            }
        }
    }

    // Change user role
    fun changeUserRole(userId: String, newRole: UserRole) {
        // Update UI immediately
        _state.update { currentState ->
            currentState.copy(
                users = currentState.users.map { user ->
                    if (user.id == userId) user.copy(role = newRole) else user
                }
            )
        }

        // Update Firebase
        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                val roleString = when (newRole) {
                    UserRole.ADMIN -> "ADMIN"
                    UserRole.SALES -> "SALES"
                    UserRole.RECEIPTS -> "RECEIPTS"
                }

                val updates = hashMapOf<String, Any>(
                    "role" to roleString,
                    "accessLevel" to roleString,
                    "lastModified" to System.currentTimeMillis()
                )

                FirebaseRefs.usersRef(currentCompanyId).child(userId)
                    .updateChildren(updates).await()

                Log.d("CompanyProfileVM", "Role updated in Firebase")
            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error updating role", e)
            }
        }
    }

    // Save company details in view mode
    fun saveCompanyDetailsInViewMode() {
        val currentState = _state.value

        if (currentState.address.isBlank() || currentState.phone.isBlank() ||
            currentState.phone.length != 10) {
            _state.update {
                it.copy(showError = true, errorMessage = "Invalid address or phone")
            }
            return
        }

        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                val updates = hashMapOf<String, Any>(
                    "address" to currentState.address,
                    "phone" to currentState.phone,
                    "lastModified" to System.currentTimeMillis()
                )

                FirebaseRefs.companiesRef().child(currentCompanyId.toString())
                    .updateChildren(updates).await()

                // Update local DB
                companyViewModel.updateCompany(
                    companyId = currentCompanyId,
                    name = currentState.companyName,
                    address = currentState.address,
                    phone = currentState.phone
                )
                companyViewModel.update()

                _state.update { it.copy(showError = false) }
                Log.d("CompanyProfileVM", "Company updated")
            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error updating company", e)
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to update: ${e.message}")
                }
            }
        }
    }

    fun saveCompanyDetails() {
        val currentState = _state.value

        if (currentState.companyName.isBlank() || currentState.address.isBlank() ||
            currentState.phone.isBlank() || currentState.phone.length != 10) {
            _state.update {
                it.copy(showError = true, errorMessage = "All fields are required")
            }
            return
        }

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
                    it.copy(isCompanyDetailsSaved = true, showError = false)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to save: ${e.message}")
                }
            }
        }
    }

    fun goToDashboard(onSuccess: () -> Unit) {
        val currentState = _state.value

        if (!currentState.users.any { it.role == UserRole.ADMIN }) {
            _state.update {
                it.copy(showError = true, errorMessage = "At least one Admin user required")
            }
            return
        }

        viewModelScope.launch {
            try {
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

                CompanyContext.currentCompanyId = companyId
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to save users: ${e.message}")
                }
            }
        }
    }

    fun resetCompanyDetails() {
        _state.update {
            it.copy(companyName = "", address = "", phone = "", showError = false)
        }
    }
}