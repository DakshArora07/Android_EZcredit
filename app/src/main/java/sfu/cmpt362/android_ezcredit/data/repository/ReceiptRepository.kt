package sfu.cmpt362.android_ezcredit.data.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.FirebaseRefs
import sfu.cmpt362.android_ezcredit.data.dao.ReceiptDao
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt

class ReceiptRepository (private val receiptDao: ReceiptDao) {
    private val companyId: Long get() = CompanyContext.currentCompanyId!!
    private val receiptsRef: DatabaseReference
        get() = FirebaseRefs.receiptsRef(companyId)

    val receipts: Flow<List<Receipt>> = receiptDao.getReceipts()

    fun insert(
        receipt: Receipt,
        onError: (String) -> Unit = {}
    ){
        CoroutineScope(IO).launch {
            val ts = System.currentTimeMillis()
            val toInsert = receipt.copy(lastModified = ts, isDeleted = false)

            try {
                val newId = receiptDao.insertReceipt(toInsert)

                if (newId == -1L) {
                    withContext(Dispatchers.Main) {
                        onError("Another receipt with the same invoice already exists")
                    }
                    return@launch
                }

                val finalReceipt = toInsert.copy(id = newId)
                pushToFirebase(finalReceipt)
                withContext(Dispatchers.Main) { onError("") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to insert receipt: ${e.message}")
                }
            }
        }
    }

    fun update(receipt: Receipt){
        CoroutineScope(IO).launch{
            val updated = receipt.copy(lastModified = System.currentTimeMillis())
            receiptDao.updateReceipt(updated)
            pushToFirebase(updated)
        }
    }

    suspend fun getById(id: Long): Receipt{
        return receiptDao.getReceiptById(id)
    }

    fun getInvoiceByReceiptId(id: Long): Invoice {
        return receiptDao.getInvoiceByReceiptId(id)
    }

    fun getAmountByReceiptId(id: Long): Double{
        return receiptDao.getAmountByReceiptId(id)
    }

    fun deleteById(
        id: Long,
        onError: (String) -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        CoroutineScope(IO).launch {
            try {
                val existing = receiptDao.getReceiptById(id)
                if (existing == null) {
                    withContext(Dispatchers.Main) {
                        onError("Receipt not found")
                    }
                    return@launch
                }

                val deleted = existing.copy(
                    isDeleted = true,
                    lastModified = System.currentTimeMillis()
                )

                receiptDao.deleteReceiptById(id)
                pushToFirebase(deleted)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to delete receipt: ${e.message}")
                }
            }
        }
    }


    private fun pushToFirebase(receipt: Receipt) {
        val map = mapOf(
            "id" to receipt.id,
            "receiptNumber" to receipt.receiptNumber,
            "receiptDate" to receipt.receiptDate.timeInMillis,
            "invoiceId" to receipt.invoiceId,
            "lastModified" to receipt.lastModified,
            "isDeleted" to receipt.isDeleted
        )
        receiptsRef.push().setValue(map)
    }
}