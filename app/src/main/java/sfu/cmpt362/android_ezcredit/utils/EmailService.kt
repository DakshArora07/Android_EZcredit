package sfu.cmpt362.android_ezcredit.utils



import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import sfu.cmpt362.android_ezcredit.BuildConfig
import java.io.IOException
import java.util.concurrent.TimeUnit

class MailgunEmailService {
    companion object {
        private const val MAILGUN_API_KEY =  BuildConfig.MAILGUN_API_KEY
        private const val MAILGUN_DOMAIN = BuildConfig.MAILGUN_DOMAIN

        private const val FROM_EMAIL = BuildConfig.FROM_EMAIL

        private const val TAG = "MailgunEmailService"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Send an email using Mailgun API
     *
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param body Email body (plain text)
     * @return Result with success message or error
     */
    suspend fun sendEmail(
        toEmail: String,
        subject: String,
        body: String
    ): Result<String> {
        return try {
            // Validate email format
            if (!isValidEmail(toEmail)) {
                Log.e(TAG, "Invalid email address: $toEmail")
                return Result.failure(Exception("Invalid email address: $toEmail"))
            }

            Log.d(TAG, "Attempting to send email to: $toEmail")

            val success = sendViaMailgun(toEmail, subject, body)

            if (success) {
                Log.d(TAG, "Email sent successfully to $toEmail")
                Result.success("Email sent successfully to $toEmail")
            } else {
                Log.e(TAG, "Failed to send email to $toEmail")
                Result.failure(Exception("Failed to send email to $toEmail"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while sending email to $toEmail", e)
            Result.failure(e)
        }
    }

    /**
     * Send email via Mailgun's Messages API
     */
    private fun sendViaMailgun(toEmail: String, subject: String, body: String): Boolean {
        val url = "https://api.mailgun.net/v3/$MAILGUN_DOMAIN/messages"

        // Create credentials for Basic Auth
        val credentials = Credentials.basic("api", MAILGUN_API_KEY)

        // Build form data
        val formBody = FormBody.Builder()
            .add("from", FROM_EMAIL)
            .add("to", toEmail)
            .add("subject", subject)
            .add("text", body)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", credentials)
            .post(formBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    Log.d(TAG, "Mailgun response: $responseBody")
                    true
                } else {
                    Log.e(TAG, "Mailgun error (${response.code}): $responseBody")
                    false
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException while calling Mailgun API", e)
            false
        }
    }

    /**
     * Validate email address format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}