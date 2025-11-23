package sfu.cmpt362.android_ezcredit.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.dao.InvoiceDao
import sfu.cmpt362.android_ezcredit.data.entity.Invoice

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    val invoices: Flow<List<Invoice>> = invoiceDao.getInvoices()

    fun insert(invoice: Invoice){
        CoroutineScope(IO).launch{
            invoiceDao.insertInvoice(invoice)
        }
    }

    fun getById(id: Long): Invoice{
        return invoiceDao.getInvoiceById(id)
    }

    fun getCustomerNameByInvoiceId(id: Long): String{
        return invoiceDao.getCustomerNameByInvoiceId(id)
    }

    fun getInvoicesByCustomerId(id: Long): List<Invoice>{
        return invoiceDao.getInvoicesByCustomerId(id)
    }

    fun deleteById(id: Long){
        CoroutineScope(IO).launch {
            invoiceDao.deleteInvoiceById(id)
        }
    }
}