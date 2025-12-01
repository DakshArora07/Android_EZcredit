package sfu.cmpt362.android_ezcredit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sfu.cmpt362.android_ezcredit.data.entity.Company

@Dao
interface CompanyDao {
    @Insert
    suspend fun insertCompany(company: Company): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(company: Company)

    @Update
    suspend fun update(company: Company)

    @Query("SELECT * FROM company_list")
    fun getCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM company_list WHERE id = :key")
    suspend fun getCompanyById(key: Long): Company

    @Query("SELECT * FROM company_list WHERE id = :key")
    suspend fun getCompanyByIdOrNull(key: Long): Company?

    @Query("DELETE FROM company_list WHERE id = :key")
    suspend fun deleteById(key: Long)
}