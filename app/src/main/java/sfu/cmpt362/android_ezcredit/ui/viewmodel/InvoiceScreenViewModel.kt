package sfu.cmpt362.android_ezcredit.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.entity.Customer

class InvoiceScreenViewModel : ViewModel() {

    private val _cameraRequest = MutableStateFlow(false)
    val cameraRequest = _cameraRequest.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()

    private val _ocrResult = MutableStateFlow<OcrInvoiceResult?>(null)
    val ocrResult = _ocrResult.asStateFlow()

    private val _customerFilter = MutableStateFlow<Customer?>(null)
    val customerFilter = _customerFilter.asStateFlow()

    fun setCustomerFilter(customer: Customer?) {
        _customerFilter.value = customer
    }

    data class OcrInvoiceResult(
        val invoiceNumber: String? = null,
        val amount: String? = null,
        val issueDate: String? = null,
        val dueDate: String? = null,
        val customerName: String? = null
    )

    fun onScanInvoiceOptionClicked() {
        _cameraRequest.value = true
    }

    fun clearOcrResult() {
        _ocrResult.value = null
    }

    fun onCameraHandled() {
        _cameraRequest.value = false
    }

    fun onAddInvoiceButtonClicked() {
        _showDialog.value = true
    }

    fun onDialogDismiss() {
        _showDialog.value = false
    }

    fun onBitmapCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val raw = visionText.text
                    Log.d("OCR_DEBUG", "Raw OCR text:\n$raw")

                    val result = OcrInvoiceResult(
                        invoiceNumber = Regex("Invoice Number:\\s*(\\w+)").find(raw)?.groupValues?.get(1),
                        amount = Regex("Amount:\\s*\\$?(\\d+\\.\\d{2})").find(raw)?.groupValues?.get(1),
                        issueDate = Regex("Issue Date:\\s*(\\d{4}-\\d{2}-\\d{2})").find(raw)?.groupValues?.get(1),
                        dueDate = Regex("Due Date:\\s*(\\d{4}-\\d{2}-\\d{2})").find(raw)?.groupValues?.get(1),
                        customerName = Regex("Customer Name:\\s*(.*)").find(raw)?.groupValues?.get(1),
                        status = Regex("Status:\\s*(.*)").find(raw)?.groupValues?.get(1)
                    )


                    _ocrResult.value = result


                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    companion object


}
