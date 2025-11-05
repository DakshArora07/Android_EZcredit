package sfu.cmpt362.android_ezcredit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice

@Dao
interface InvoiceDao {
    @Insert
    suspend fun insertInvoice(invoice: Invoice)

    @Query("SELECT * FROM invoice_list")
    fun getInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoice_list WHERE id = :key")
    fun getInvoiceById(key: Long): Invoice

    @Query("DELETE FROM invoice_list WHERE id = :key")
    suspend fun deleteInvoiceById(key: Long)
}