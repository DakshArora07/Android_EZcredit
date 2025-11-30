package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sfu.cmpt362.android_ezcredit.data.entity.Receipt
import sfu.cmpt362.android_ezcredit.data.repository.ReceiptRepository
import kotlin.collections.isNotEmpty

class ReceiptViewModel(private val repository: ReceiptRepository) : ViewModel() {

    var receipt by mutableStateOf(Receipt())
        private set

    val receiptsLiveData: LiveData<List<Receipt>> = repository.receipts.asLiveData()

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
                invoiceID = invoiceId
            )
        }else{
            receipt = receipt.copy(
                id = receiptId,
                receiptNumber = receiptNumber,
                receiptDate = receiptDate,
                invoiceID = invoiceId
            )
        }
    }
    fun update(){
        repository.update(receipt)
    }

    fun insert() {
        repository.insert(receipt)
        receipt = Receipt()
    }

    fun getReceiptById(id: Long): Receipt {
        return repository.getById(id)
    }

    fun getAmountByReceiptId(id: Long): Double {
        return repository.getAmountByReceiptId(id)
    }

    fun delete(id: Long) {
        val receiptList = receiptsLiveData.value
        if (receiptList != null && receiptList.isNotEmpty()) {
            repository.deleteById(id)
        }
    }
}