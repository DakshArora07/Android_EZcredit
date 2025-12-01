package sfu.cmpt362.android_ezcredit.data.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.FirebaseRefs
import sfu.cmpt362.android_ezcredit.data.dao.ReceiptDao
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt

class ReceiptRepository (
    private val receiptDao: ReceiptDao,
    private val receiptsRef: DatabaseReference = FirebaseRefs.receiptsRef) {
    val receipts: Flow<List<Receipt>> = receiptDao.getReceipts()

    fun insert(receipt: Receipt){
        CoroutineScope(IO).launch{
            val ts = System.currentTimeMillis()
            val toInsert = receipt.copy(lastModified = ts, isDeleted = false)
            val newId = receiptDao.insertReceipt(toInsert)
            val finalReceipt = toInsert.copy(id = newId)
            pushToFirebase(finalReceipt)
        }
    }
    fun update(receipt: Receipt){
        CoroutineScope(IO).launch{
            val updated = receipt.copy(lastModified = System.currentTimeMillis())
            receiptDao.updateReceipt(updated)
            pushToFirebase(updated)
        }
    }

    fun getById(id: Long): Receipt{
        return receiptDao.getReceiptById(id)
    }

    fun getInvoiceByReceiptId(id: Long): Invoice {
        return receiptDao.getInvoiceByReceiptId(id)
    }

    fun getAmountByReceiptId(id: Long): Double{
        return receiptDao.getAmountByReceiptId(id)
    }

    fun deleteById(id: Long){
        CoroutineScope(IO).launch {
            val existing = receiptDao.getReceiptById(id)
            val deleted = existing.copy(
                isDeleted = true,
                lastModified = System.currentTimeMillis()
            )
            receiptDao.deleteReceiptById(id)
            pushToFirebase(deleted)
        }
    }

    private fun pushToFirebase(receipt: Receipt) {
        val map = mapOf(
            "id" to receipt.id,
            "receiptNumber" to receipt.receiptNumber,
            "receiptDate" to receipt.receiptDate.timeInMillis,
            "invoiceID" to receipt.invoiceID,
            "lastModified" to receipt.lastModified,
            "isDeleted" to receipt.isDeleted
        )
        receiptsRef.child(receipt.id.toString()).setValue(map)
    }
}