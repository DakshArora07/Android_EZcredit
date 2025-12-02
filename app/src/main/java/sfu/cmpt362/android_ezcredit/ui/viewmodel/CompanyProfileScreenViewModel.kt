package sfu.cmpt362.android_ezcredit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
        viewModelScope.launch {
            try {
                val numericUserId = userId.toLongOrNull() ?: return@launch

                // Delete from database
                withContext(Dispatchers.IO) {
                    userViewModel.delete(numericUserId)
                }

                Log.d("CompanyProfileVM", "User $userId deleted from database")

                // Update UI state
                _state.update { currentState ->
                    currentState.copy(
                        users = currentState.users.filter { it.id != userId },
                        showError = false
                    )
                }
            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error deleting user", e)
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to delete user: ${e.message}")
                }
            }
        }
    }

    fun addUserToDatabase(user: User) {
        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                Log.d("CompanyProfileVM", "Adding user: ${user.name}, Email: ${user.email}, Role: ${user.role}")

                val accessMode = when (user.role) {
                    UserRole.ADMIN -> AccessMode.Admin
                    UserRole.SALES -> AccessMode.Sales
                    UserRole.RECEIPTS -> AccessMode.Receipts
                }

                userViewModel.updateUser(
                    userId = 0L, // Let Room auto-generate
                    name = user.name,
                    email = user.email,
                    companyId = currentCompanyId,
                    accessLevel = accessMode
                )

                val localUserId = userViewModel.insert()
                Log.d("CompanyProfileVM", "User added to local DB with ID: $localUserId")

                loadCompanyData()

            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error adding user", e)
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to add user: ${e.message}")
                }
            }
        }
    }

    fun loadCompanyData() {
        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch

                Log.d("CompanyProfileVM", "Loading company data for ID: $currentCompanyId")

                // Load company details
                val company = withContext(Dispatchers.IO) {
                    companyViewModel.getCompanyById(currentCompanyId)
                }

                Log.d("CompanyProfileVM", "Company loaded - Name: ${company.name}, Address: ${company.address}, Phone: ${company.phone}")

                // Load users and observe LiveData
                val usersLiveData = userViewModel.getUsersByCompanyId(currentCompanyId)

                // Observe the LiveData to get users
                usersLiveData.observeForever { dbUsers ->
                    val uiUsers = dbUsers?.mapNotNull { user ->
                        try {
                            val role = when (user.accessLevel) {
                                AccessMode.Admin -> UserRole.ADMIN
                                AccessMode.Sales -> UserRole.SALES
                                AccessMode.Receipts -> UserRole.RECEIPTS
                            }

                            Log.d("CompanyProfileVM", "User: ID: ${user.id}, Name: ${user.name}, Email: ${user.email}, Role: $role")

                            User(
                                id = user.id.toString(),
                                name = user.name,
                                email = user.email,
                                role = role
                            )
                        } catch (e: Exception) {
                            Log.e("CompanyProfileVM", "Error mapping user ${user.id}", e)
                            null
                        }
                    } ?: emptyList()

                    companyId = currentCompanyId

                    _state.update {
                        it.copy(
                            companyName = company.name,
                            address = company.address,
                            phone = company.phone,
                            users = uiUsers,
                            isCompanyDetailsSaved = true,
                            showError = false
                        )
                    }

                    Log.d("CompanyProfileVM", "State updated with ${uiUsers.size} users")
                }

            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error loading company data", e)
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to load: ${e.message}")
                }
            }
        }
    }

    fun changeUserRole(userId: String, newRole: UserRole) {
        Log.d("CompanyProfileVM", "Changing role for user $userId to $newRole")

        // Prevent changing Admin users' role
        val currentUser = _state.value.users.find { it.id == userId }
        if (currentUser?.role == UserRole.ADMIN) {
            Log.d("CompanyProfileVM", "Cannot change role of Admin user")
            _state.update {
                it.copy(showError = true, errorMessage = "Admin user's role cannot be changed")
            }
            return
        }

        // Update UI immediately
        _state.update { currentState ->
            currentState.copy(
                users = currentState.users.map { user ->
                    if (user.id == userId) user.copy(role = newRole) else user
                },
                showError = false
            )
        }

        viewModelScope.launch {
            try {
                val currentCompanyId = CompanyContext.currentCompanyId ?: return@launch
                val numericUserId = userId.toLongOrNull() ?: return@launch

                // Update in local database
                val accessMode = when (newRole) {
                    UserRole.ADMIN -> AccessMode.Admin
                    UserRole.SALES -> AccessMode.Sales
                    UserRole.RECEIPTS -> AccessMode.Receipts
                }

                val localUser = withContext(Dispatchers.IO) {
                    userViewModel.getUserById(numericUserId)
                }

                userViewModel.updateUser(
                    userId = numericUserId,
                    name = localUser.name,
                    email = localUser.email,
                    companyId = currentCompanyId,
                    accessLevel = accessMode
                )
                userViewModel.update()

                Log.d("CompanyProfileVM", "Role updated in local DB")
            } catch (e: Exception) {
                Log.e("CompanyProfileVM", "Error updating role", e)
                _state.update {
                    it.copy(showError = true, errorMessage = "Failed to update role: ${e.message}")
                }
            }
        }
    }

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

                Log.d("CompanyProfileVM", "Saving company details - Current users in state: ${currentState.users.size}")

                // Update local DB company info only (not touching users)
                withContext(Dispatchers.IO) {
                    companyViewModel.updateCompany(
                        companyId = currentCompanyId,
                        name = currentState.companyName,
                        address = currentState.address,
                        phone = currentState.phone
                    )
                    companyViewModel.update()
                }

                Log.d("CompanyProfileVM", "Company updated in local DB")

                // Clear error and keep the current state (don't reload to preserve users)
                _state.update {
                    it.copy(showError = false)
                }

                Log.d("CompanyProfileVM", "Company updated successfully. Users remain: ${currentState.users.size}")
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