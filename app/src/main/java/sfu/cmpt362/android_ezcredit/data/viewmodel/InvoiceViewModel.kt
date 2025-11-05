package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import kotlin.collections.isNotEmpty

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {
    val invoice = Invoice()
    val invoicesLiveData: LiveData<List<Invoice>> = repository.invoices.asLiveData()

    fun insert(invoice: Invoice) {
        repository.insert(invoice)
    }

    fun getById(id: Long): Invoice {
        return repository.getById(id)
    }

    fun delete(id: Long) {
        val invoicesList = invoicesLiveData.value
        if (invoicesList != null && invoicesList.isNotEmpty()) {
            repository.deleteById(id)
        }
    }
}