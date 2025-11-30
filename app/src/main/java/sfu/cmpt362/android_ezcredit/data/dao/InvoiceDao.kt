package sfu.cmpt362.android_ezcredit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sfu.cmpt362.android_ezcredit.data.entity.Invoice

@Dao
interface InvoiceDao {
    @Insert
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(invoice: Invoice)

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Query("SELECT * FROM invoice_list")
    fun getInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoice_list WHERE id = :key")
    suspend fun getInvoiceById(key: Long): Invoice

    @Query("SELECT * FROM invoice_list WHERE id = :key")
    suspend fun getInvoiceByIdOrNull(key: Long): Invoice?

    @Query("SELECT customer_name FROM invoice_list AS i JOIN customer_list AS c ON i.customer_id = c.id WHERE i.id = :key")
    fun getCustomerNameByInvoiceId(key: Long): String

    @Query("SELECT * FROM invoice_list WHERE customer_id = :key")
    suspend fun getInvoicesByCustomerId(key: Long): List<Invoice>

    @Query("DELETE FROM invoice_list WHERE id = :key")
    suspend fun deleteInvoiceById(key: Long)
}