package sfu.cmpt362.android_ezcredit.data.gemini
import sfu.cmpt362.android_ezcredit.BuildConfig
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
class GeminiSetup {
    fun sendToGemini(lifecycleScope: LifecycleCoroutineScope){
        val businessName="EZCredit"
        val invoiceNo=1
        val customerName="John"
        val prompt = "generate a polite message to remind the customer to pay the service bill. " +
                "give just one best option and make it crisp. " +
                "use customer name as ${customerName}, invoice number as ${invoiceNo} and our business name as ${businessName}." +
                "Format it properly so it looks professional"
        lifecycleScope.launch {
            try {
                val gemini = GenerativeModel(
                    modelName = "gemini-2.5-flash-lite",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )
                val response = gemini.generateContent(prompt)
                val responseText = response.text ?: "No response generated"

                // Print to Logcat (Android's terminal/console)
                Log.d("Gemini Response", responseText)

            } catch (e: com.google.ai.client.generativeai.type.GoogleGenerativeAIException) {
                Log.e("GEMENI", "Gemini API Error: ${e.message}", e)
                println("Gemini API Error: ${e.message}")
            } catch (e: Exception) {
                Log.e("GEMENI", "Error: ${e.message}", e)
                Log.e("GEMENI", "Error type: ${e::class.java.simpleName}")
                e.printStackTrace()
                println("Error from code: ${e.message}")
            }
        }
    }
}