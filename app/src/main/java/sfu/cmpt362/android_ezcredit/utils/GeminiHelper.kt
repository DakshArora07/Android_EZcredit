package sfu.cmpt362.android_ezcredit.utils

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import sfu.cmpt362.android_ezcredit.BuildConfig

// Generate email by Gemini setup
object GeminiHelper {
    private const val API_KEY = BuildConfig.GEMINI_API_KEY
    val model = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = API_KEY
    )
    suspend fun generateReminderMessage(
        customerName: String,
        invoiceNumber: String,
        invoiceURL: String,
        companyName: String,
        amount: Double,
        dueDate: String,
        status: InvoiceStatus,
        daysOffset: Int
    ): String {
        try {
            val prompt = buildPrompt(customerName, invoiceNumber, amount, dueDate, status, daysOffset, companyName, invoiceURL)
            Log.d("GeminiHelper", "Generating message with prompt:\n$prompt")

            val response = model.generateContent(prompt)
            val text = response.text ?: ""
            Log.d("GeminiHelper", "Generated message: $text")

            return text
        } catch (e: Exception) {
            Log.e("GeminiHelper", "Failed generating message", e)
            return ""
        }
    }

    private fun buildPrompt(
        customerName: String,
        invoiceNumber: String,
        amount: Double,
        dueDate: String,
        status: InvoiceStatus,
        daysOffset: Int,
        companyName: String,
        invoiceURL: String
    ): String {

        return """
            Write a professional and polite payment reminder message for a customer.
            
            Context: Look at the due date and amount of the invoice.
            Customer Name: $customerName
            Invoice Number: $invoiceNumber
            Company Name: $companyName
            Amount Due: $$amount
            Due Date: $dueDate
            Invoice Status: ${status.name}
            Invoice Payment Link: $invoiceURL
            
            Requirements:
            - Begin with a salutation greeting the customer by name on its own line.
            - Follow with a concise body paragraph including invoice number, amount, and due date details.
            - End with a polite thank you and call to action on its own line.
            - Separate greeting, body, and closing with a blank line.
            - The entire message should be clear and well formatted with line breaks.
            - Be professional and courteous, concise (2-3 sentences).
            - Look at the invoice status to see whether it is unpaid or overdue, and mention it accordingly
            - End the email by thanking formally and including the Company Name
            - Include: Pay here: $invoiceURL (in a separate paragraph)
        
            Generate only the message content with greeting, body, and closing formatted properly.
        """.trimIndent()
    }
}
