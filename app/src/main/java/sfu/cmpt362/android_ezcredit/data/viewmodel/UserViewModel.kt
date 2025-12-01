package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sfu.cmpt362.android_ezcredit.data.repository.UserRepository
import sfu.cmpt362.android_ezcredit.data.entity.User
import sfu.cmpt362.android_ezcredit.utils.AccessMode
import kotlin.collections.isNotEmpty

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    var user by mutableStateOf(User())
        private set

    fun getUsersByCompanyId(companyId: Long): LiveData<List<User>> {
        return repository.getUsersByCompanyId(companyId).asLiveData()
    }

    fun updateUser(
        userId: Long = -1L,
        name: String,
        email: String,
        companyId: Long,
        accessLevel: AccessMode
    ) {
        if (userId == -1L) {
            user = user.copy(
                name = name,
                email = email,
                companyId = companyId,
                accessLevel = accessLevel
            )
        } else {
            user = user.copy(
                id = userId,
                name = name,
                email = email,
                companyId = companyId,
                accessLevel = accessLevel
            )
        }
    }

    fun update() {
        repository.update(user)
    }

    suspend fun insert(): Long {
        val id = repository.insert(user)
        user = User()
        return id
    }

    suspend fun getUserById(id: Long): User {
        return repository.getById(id)
    }

    suspend fun hasAdminUser(companyId: Long): Boolean {
        return repository.hasAdminUser(companyId)
    }

    fun delete(id: Long) {
        val usersLiveData: LiveData<List<User>> = repository.getUsersByCompanyId(user.companyId).asLiveData()
        val userList = usersLiveData.value
        if (userList != null && userList.isNotEmpty()) {
            repository.deleteById(id)
        }
    }
}
