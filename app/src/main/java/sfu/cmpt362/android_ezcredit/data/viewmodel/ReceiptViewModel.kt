package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt
import sfu.cmpt362.android_ezcredit.data.repository.ReceiptRepository
import kotlin.collections.isNotEmpty

class ReceiptViewModel(private val repository: ReceiptRepository) : ViewModel() {

    var receipt by mutableStateOf(Receipt())
        private set

    val receiptsLiveData: LiveData<List<Receipt>> = repository.receipts.asLiveData()

    var defReceiptOrSorted by mutableStateOf(emptyList<Receipt>())

    private val _currentReceipt = MutableStateFlow<Receipt?>(null)


    val currentReceipt: StateFlow<Receipt?> = _currentReceipt


    fun loadReceipt(id: Long) {
        viewModelScope.launch {
            val receipt = repository.getById(id)
            _currentReceipt.value = receipt
        }
    }

    fun updateReceipt(
        receiptId:Long,
        receiptNumber: String,
        receiptDate: android.icu.util.Calendar,
        invoiceId: Long,
    ) {
        if(receiptId==-1L){
            receipt = receipt.copy(
                receiptNumber = receiptNumber,
                receiptDate = receiptDate,
                invoiceId = invoiceId
            )
        }else{
            receipt = receipt.copy(
                id = receiptId,
                receiptNumber = receiptNumber,
                receiptDate = receiptDate,
                invoiceId = invoiceId
            )
        }
    }
    fun update(){
        repository.update(receipt)
    }

    fun insert(onError: (String) -> Unit) {
        repository.insert(receipt, onError = {
            onError(it)
            return@insert
        })

        receipt = Receipt()
    }

    suspend fun getReceiptById(id: Long): Receipt {
        return repository.getById(id)
    }

    fun getInvoiceByReceiptId(id: Long): Invoice {
        return repository.getInvoiceByReceiptId(id)
    }

    fun getAmountByReceiptId(id: Long): Double {
        return repository.getAmountByReceiptId(id)
    }

    fun delete(
        id: Long,
        onError: (String) -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        val receiptList = receiptsLiveData.value
        if (receiptList != null && receiptList.isNotEmpty()) {
            repository.deleteById(id, onError, onSuccess)
        } else {
            onError("No receipts available to delete")
        }
    }

}