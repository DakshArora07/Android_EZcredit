package sfu.cmpt362.android_ezcredit.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.ui.activities.OCRResultActivity

class InvoiceScreenViewModel : ViewModel() {

    private val _cameraRequest = MutableStateFlow(false)
    val cameraRequest = _cameraRequest.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()

    fun onScanInvoiceOptionClicked() {
        _cameraRequest.value = true
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

    fun onBitmapCaptured(bitmap: Bitmap, context: Context) {
        viewModelScope.launch {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text.ifEmpty { "No text found" }
                    val intent = Intent(context, OCRResultActivity::class.java).apply {
                        putExtra("ocr_text", extractedText)
                    }
                    context.startActivity(intent)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }
}
