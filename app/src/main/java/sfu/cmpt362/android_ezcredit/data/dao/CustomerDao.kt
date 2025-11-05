package sfu.cmpt362.android_ezcredit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sfu.cmpt362.android_ezcredit.data.entity.Customer

@Dao
interface CustomerDao {
    @Insert
    suspend fun insertCustomer(customer: Customer)

    @Query("SELECT * FROM customer_list")
    fun getCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customer_list WHERE id = :key")
    fun getCustomerById(key: Long): Customer

    @Query("DELETE FROM customer_list WHERE id = :key")
    suspend fun deleteCustomerById(key: Long)
}