package sfu.cmpt362.android_ezcredit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt

@Dao
interface ReceiptDao {
    @Insert
    suspend fun insertReceipt(receipt: Receipt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(receipt: Receipt)

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Query("SELECT * FROM receipt_list")
    fun getReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipt_list WHERE id = :key")
    fun getReceiptById(key: Long): Receipt

    @Query("SELECT * FROM receipt_list WHERE id = :key")
    fun getReceiptByIdOrNull(key: Long): Receipt?

    @Query("SELECT i.* FROM receipt_list AS r JOIN invoice_list AS i ON r.invoice_id = i.id WHERE r.id = :key")
    fun getInvoiceByReceiptId(key: Long): Invoice

    @Query("SELECT invoice_amount FROM receipt_list AS r JOIN invoice_list AS i ON r.invoice_id = i.id WHERE r.id = :key")
    fun getAmountByReceiptId(key: Long): Double

    @Query("DELETE FROM receipt_list WHERE id = :key")
    suspend fun deleteReceiptById(key: Long)
}