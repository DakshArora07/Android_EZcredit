package sfu.cmpt362.android_ezcredit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sfu.cmpt362.android_ezcredit.data.entity.User
import sfu.cmpt362.android_ezcredit.utils.AccessMode

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM user_list WHERE company_id = :key AND isDeleted = 0")
    fun getUsersByCompanyId(key: Long): Flow<List<User>>

    @Query("SELECT * FROM user_list WHERE id = :key")
    suspend fun getUserById(key: Long): User

    @Query("SELECT * FROM user_list WHERE id = :key")
    suspend fun getUserByIdOrNull(key: Long): User?

    @Query("SELECT COUNT(*) > 0 FROM user_list WHERE company_id = :key AND access_level = :adminMode")
    suspend fun hasAdminUser(key: Long, adminMode: AccessMode = AccessMode.Admin): Boolean

    @Query("DELETE FROM user_list WHERE id = :key")
    suspend fun deleteById(key: Long)
}