package sfu.cmpt362.android_ezcredit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sfu.cmpt362.android_ezcredit.data.entity.Customer

@Dao
interface CustomerDao {
    @Insert
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(customer: Customer)

    @Update
    suspend fun update(customer: Customer)

    @Query("SELECT * FROM customer_list")
    fun getCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customer_list WHERE id = :key")
    suspend fun getCustomerById(key: Long): Customer

    @Query("SELECT * FROM customer_list WHERE id = :key")
    suspend fun getCustomerByIdOrNull(key: Long): Customer?

    @Query("DELETE FROM customer_list WHERE id = :key")
    suspend fun deleteCustomerById(key: Long)
}