package sfu.cmpt362.android_ezcredit.utils

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import sfu.cmpt362.android_ezcredit.BuildConfig

object GeminiHelper {
    private const val API_KEY = BuildConfig.GEMINI_API_KEY

    val model = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateReminderMessage(
        customerName: String,
        invoiceNumber: String,
        amount: Double,
        dueDate: String,
        daysOffset: Int
    ): String {
        try {
            val prompt = buildPrompt(customerName, invoiceNumber, amount, dueDate, daysOffset)
            Log.d("GeminiHelper", "Generating message with prompt:\n$prompt")

            val response = model.generateContent(prompt)
            val text = response.text ?: getDefaultMessage(customerName, invoiceNumber, amount, dueDate, daysOffset)
            Log.d("GeminiHelper", "Generated message: $text")

            return if (text.contains(customerName)) text else "Hi $customerName, $text"
        } catch (e: Exception) {
            Log.e("GeminiHelper", "Failed generating message, using default", e)
            return getDefaultMessage(customerName, invoiceNumber, amount, dueDate, daysOffset)
        }
    }

    private fun buildPrompt(
        customerName: String,
        invoiceNumber: String,
        amount: Double,
        dueDate: String,
        daysOffset: Int
    ): String {
        val context = when (daysOffset) {
            -3 -> "3 days before the due date (early reminder)"
            0 -> "on the due date (payment due today)"
            3 -> "3 days after the due date (overdue reminder)"
            5 -> "5 days after the due date (urgent overdue reminder)"
            else -> "regarding payment"
        }

        return """
            Write a professional and polite payment reminder message for a customer.
            
            Context: This is a reminder sent $context.
            Customer Name: $customerName
            Invoice Number: $invoiceNumber
            Amount Due: $$amount
            Due Date: $dueDate
            
            Requirements:
            - Be professional and courteous
            - Keep it concise (2-3 sentences)
            - Must start the message by greeting customer by their name exactly as provided
            - Include the invoice number and amount
            - ${if (daysOffset > 0) "Mention that payment is overdue" else ""}
            - ${if (daysOffset <= 0) "Remind them of the upcoming/current due date" else ""}
            - End with a call to action
            - Do not include subject line or greeting
            - Start directly with the message content
            
            Generate only the message body, no additional formatting or explanations.
        """.trimIndent()
    }

    private fun getDefaultMessage(
        customerName: String,
        invoiceNumber: String,
        amount: Double,
        dueDate: String,
        daysOffset: Int
    ): String {
        return when (daysOffset) {
            -3 ->
                "Hi $customerName, this is a friendly reminder that invoice #$invoiceNumber for $$amount is due on $dueDate (in 3 days). Please ensure timely payment. Thank you!"

            0 ->
                "Hi $customerName, invoice #$invoiceNumber for $$amount is due today ($dueDate). Please process your payment at your earliest convenience. Thank you!"

            3 ->
                "Hi $customerName, invoice #$invoiceNumber for $$amount was due on $dueDate and is now 3 days overdue. Please arrange payment as soon as possible. Thank you!"

            5 ->
                "Hi $customerName, urgent reminder: invoice #$invoiceNumber for $$amount is now 5 days overdue (due date: $dueDate). Please contact us immediately to arrange payment. Thank you!"

            else -> "Hi $customerName, this is a reminder regarding invoice #$invoiceNumber for $$amount (due: $dueDate). Please process your payment. Thank you!"
        }
    }
}
