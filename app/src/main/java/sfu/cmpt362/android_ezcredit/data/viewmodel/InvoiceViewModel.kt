package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {

    var invoice by mutableStateOf(Invoice())
        private set
    var defInvoicesOrSorted by mutableStateOf<List<Invoice>>(emptyList())
    var amountText by mutableStateOf("")
        private set

    var selectedInvoice by mutableStateOf<Invoice?>(null)

    val invoicesLiveData: LiveData<List<Invoice>> = repository.invoices.asLiveData()
    private val _customerName = MutableStateFlow<String?>(null)
    val customerName = _customerName.asStateFlow()

    fun updateInvoice(
        invoiceId:Long,
        invoiceNumber: String,
        customerId: Long,
        issueDate: android.icu.util.Calendar,
        dueDate: android.icu.util.Calendar,
        amount: Double,
        status: InvoiceStatus
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
                id = invoiceId,
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

    suspend fun getInvoiceById(id: Long): Invoice {
        return repository.getById(id)
    }

    suspend fun getCustomerNameByInvoiceId(id: Long): String{
        return repository.getCustomerNameByInvoiceId(id)
    }
    // Have to add this since getCustomerNameByInvoiceId cannot be be ran in a UI thread
    fun loadCustomerName(invoiceId: Long) {
        viewModelScope.launch {
            val name = repository.getCustomerNameByInvoiceId(invoiceId)
            _customerName.value = name
        }
    }

    fun clearCustomerName() {
        _customerName.value = null
    }


    suspend fun getInvoicesByCustomerId(id: Long): List<Invoice> {
        return repository.getInvoicesByCustomerId(id)
    }

    fun delete(id: Long) {
        val invoiceList = invoicesLiveData.value
        if (invoiceList != null && invoiceList.isNotEmpty()) {
            repository.deleteById(id)
        }
    }
}