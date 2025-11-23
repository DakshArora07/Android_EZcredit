package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {

    var invoice by mutableStateOf(Invoice())
        private set

    var amountText by mutableStateOf("")
        private set

    val invoicesLiveData: LiveData<List<Invoice>> = repository.invoices.asLiveData()

    fun updateInvoice(
        invoiceNumber: String,
        customerId: Long,
        issueDate: android.icu.util.Calendar,
        dueDate: android.icu.util.Calendar,
        amount: Double,
        status: String
    ) {
        invoice = invoice.copy(
            invoiceNumber = invoiceNumber,
            customerID = customerId,
            invDate = issueDate,
            dueDate = dueDate,
            amount = amount,
            status = status
        )
    }

    fun updateAmountText(text: String) {
        amountText = text
    }

    fun insert() {
        repository.insert(invoice)
        invoice = Invoice()
        amountText = ""
    }

    fun getById(id: Long): Invoice {
        return repository.getById(id)
    }

    fun getCustomerNameByInvoiceId(id: Long): String{
        return repository.getCustomerNameByInvoiceId(id)
    }

    fun getInvoicesByCustomerId(id: Long): List<Invoice> {
        return repository.getInvoicesByCustomerId(id)
    }

    fun delete(id: Long) {
        val invoiceList = invoicesLiveData.value
        if (invoiceList != null && invoiceList.isNotEmpty()) {
            repository.deleteById(id)
        }
    }
}