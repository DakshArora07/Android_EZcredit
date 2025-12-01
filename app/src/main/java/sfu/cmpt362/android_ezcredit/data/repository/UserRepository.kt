package sfu.cmpt362.android_ezcredit.data.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.FirebaseRefs
import sfu.cmpt362.android_ezcredit.data.dao.UserDao
import sfu.cmpt362.android_ezcredit.data.entity.User
import sfu.cmpt362.android_ezcredit.utils.AccessMode

class UserRepository(private val userDao: UserDao) {
    fun getUsersByCompanyId(companyId: Long): Flow<List<User>> =
        userDao.getUsersByCompanyId(companyId)

    suspend fun insert(user: User): Long {
        val ts = System.currentTimeMillis()
        val toInsert = user.copy(lastModified = ts, isDeleted = false)
        val newId = userDao.insertUser(toInsert)
        val finalUser = toInsert.copy(id = newId)
        pushToFirebase(finalUser)
        return newId
    }

    fun update(user: User) {
        CoroutineScope(IO).launch {
            val updated = user.copy(lastModified = System.currentTimeMillis())
            userDao.update(updated)
            pushToFirebase(updated)
        }
    }

    suspend fun getById(id: Long): User = userDao.getUserById(id)

    suspend fun hasAdminUser(companyId: Long): Boolean =
        userDao.hasAdminUser(companyId)

    fun deleteById(id: Long) {
        CoroutineScope(IO).launch {
            val existing = userDao.getUserById(id)
            val deleted = existing.copy(isDeleted = true, lastModified = System.currentTimeMillis())
            userDao.deleteById(id)
            pushToFirebase(deleted)
        }
    }

    private fun pushToFirebase(user: User) {
        val map = mapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "companyId" to user.companyId,
            "accessLevel" to user.accessLevel.name,
            "lastModified" to user.lastModified,
            "isDeleted" to user.isDeleted
        )
        FirebaseRefs.usersRef(user.companyId).child(user.id.toString()).setValue(map)
    }
}
