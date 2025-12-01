package sfu.cmpt362.android_ezcredit.data.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.FirebaseRefs
import sfu.cmpt362.android_ezcredit.data.dao.InvoiceDao
import sfu.cmpt362.android_ezcredit.data.entity.Invoice

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    private val companyId: Long get() = CompanyContext.currentCompanyId!!
    private val invoicesRef: DatabaseReference
        get() = FirebaseRefs.invoicesRef(companyId)

    val invoices: Flow<List<Invoice>> = invoiceDao.getInvoices()

    fun insert(invoice: Invoice){
        CoroutineScope(IO).launch{
            val ts = System.currentTimeMillis()
            val toInsert = invoice.copy(lastModified = ts, isDeleted = false)
            val newId = invoiceDao.insertInvoice(toInsert)
            val finalInv = toInsert.copy(id = newId)
            pushToFirebase(finalInv)
        }
    }

    fun update(invoice: Invoice){
        CoroutineScope(IO).launch{
            val updated = invoice.copy(lastModified = System.currentTimeMillis())
            invoiceDao.updateInvoice(updated)
            pushToFirebase(updated)
        }
    }

    suspend fun getById(id: Long): Invoice{
        return invoiceDao.getInvoiceById(id)
    }

    suspend fun getCustomerNameByInvoiceId(id: Long): String{
        return invoiceDao.getCustomerNameByInvoiceId(id)
    }

    suspend fun getInvoicesByCustomerId(id: Long): List<Invoice>{
        return invoiceDao.getInvoicesByCustomerId(id)
    }

    fun deleteById(id: Long){
        CoroutineScope(IO).launch {
            val existing = invoiceDao.getInvoiceById(id)
            val deleted = existing.copy(
                isDeleted = true,
                lastModified = System.currentTimeMillis()
            )
            invoiceDao.deleteInvoiceById(id)
            pushToFirebase(deleted)
        }
    }

    private fun pushToFirebase(inv: Invoice) {
        val map = mapOf(
            "id" to inv.id,
            "invoiceNumber" to inv.invoiceNumber,
            "customerId" to inv.customerId,
            "invDate" to inv.invDate.timeInMillis,
            "dueDate" to inv.dueDate.timeInMillis,
            "amount" to inv.amount,
            "status" to inv.status,
            "lastModified" to inv.lastModified,
            "isDeleted" to inv.isDeleted
        )
        invoicesRef.child(inv.id.toString()).setValue(map)
    }
}