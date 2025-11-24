package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {

    var invoice by mutableStateOf(Invoice())
        private set

    var amountText by mutableStateOf("")
        private set

    val invoicesLiveData: LiveData<List<Invoice>> = repository.invoices.asLiveData()

    fun updateInvoice(
        invoiceId:Long,
        invoiceNumber: String,
        customerId: Long,
        issueDate: android.icu.util.Calendar,
        dueDate: android.icu.util.Calendar,
        amount: Double,
        status: String
    ) {
        if(invoiceId==-1L){
            invoice = invoice.copy(
                invoiceNumber = invoiceNumber,
                customerID = customerId,
                invDate = issueDate,
                dueDate = dueDate,
                amount = amount,
                status = status
            )
        }else{
            invoice = invoice.copy(
                invoiceId,
                invoiceNumber = invoiceNumber,
                customerID = customerId,
                invDate = issueDate,
                dueDate = dueDate,
                amount = amount,
                status = status
            )
        }
    }
    fun update(){
        repository.update(invoice)
    }

    fun updateAmountText(text: String) {
        amountText = text
    }

    fun insert() {
        repository.insert(invoice)
        invoice = Invoice()
        amountText = ""
    }

    fun getInvoiceById(id: Long, onResult: (Invoice) -> Unit){
        viewModelScope.launch {
            val invoice = repository.getById(id)
            onResult(invoice)
        }
    }

    fun getCustomerNameByInvoiceId(id: Long): String{
        return repository.getCustomerNameByInvoiceId(id)
    }

    fun getInvoicesByCustomerId(id: Long, onResult: (List<Invoice>) -> Unit) {
        viewModelScope.launch {
            val invoices = repository.getInvoicesByCustomerId(id)
            onResult(invoices)
        }
    }

    fun delete(id: Long) {
        val invoiceList = invoicesLiveData.value
        if (invoiceList != null && invoiceList.isNotEmpty()) {
            repository.deleteById(id)
        }
    }
}