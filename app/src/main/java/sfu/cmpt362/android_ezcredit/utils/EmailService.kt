package sfu.cmpt362.android_ezcredit.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import sfu.cmpt362.android_ezcredit.BuildConfig
import java.io.IOException
import java.util.concurrent.TimeUnit

// Mailgun Email Service setup
class MailgunEmailService {
    companion object {
        private const val MAILGUN_API_KEY = BuildConfig.MAILGUN_API_KEY
        private const val MAILGUN_DOMAIN = BuildConfig.MAILGUN_DOMAIN
        private const val FROM_EMAIL = BuildConfig.FROM_EMAIL
        private const val TAG = "MailgunEmailService"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendEmail(
        toEmail: String,
        subject: String,
        body: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== Email Send Attempt ===")
            Log.d(TAG, "To: $toEmail")
            Log.d(TAG, "Subject: $subject")
            Log.d(TAG, "Domain configured: ${MAILGUN_DOMAIN.isNotEmpty()}")
            Log.d(TAG, "API Key configured: ${MAILGUN_API_KEY.isNotEmpty()}")
            Log.d(TAG, "From Email configured: ${FROM_EMAIL.isNotEmpty()}")

            if (!isValidEmail(toEmail)) {
                Log.e(TAG, "Invalid email address: $toEmail")
                return@withContext Result.failure(Exception("Invalid email address format"))
            }

            // Validate configuration
            if (MAILGUN_API_KEY.isEmpty()) {
                Log.e(TAG, "Mailgun API key not configured")
                return@withContext Result.failure(Exception("Mailgun API key not configured in BuildConfig"))
            }

            if (MAILGUN_DOMAIN.isEmpty()) {
                Log.e(TAG, "Mailgun domain not configured")
                return@withContext Result.failure(Exception("Mailgun domain not configured in BuildConfig"))
            }

            if (FROM_EMAIL.isEmpty()) {
                Log.e(TAG, "From email not configured")
                return@withContext Result.failure(Exception("From email not configured in BuildConfig"))
            }

            Log.d(TAG, "Configuration validated, attempting to send...")

            // Send via Mailgun
            val result = sendViaMailgun(toEmail, subject, body)

            if (result.isSuccess) {
                val message = "Email sent successfully to $toEmail"
                Log.d(TAG, message)
                Result.success(message)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.e(TAG, "Failed to send: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in sendEmail", e)
            Result.failure(e)
        }
    }

    // Send email via email
    private fun sendViaMailgun(toEmail: String, subject: String, body: String): Result<String> {
        val url = "https://api.mailgun.net/v3/$MAILGUN_DOMAIN/messages"

        Log.d(TAG, "Mailgun URL: $url")

        return try {
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

            Log.d(TAG, "Executing Mailgun request...")

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val code = response.code

                Log.d(TAG, "Response code: $code")
                Log.d(TAG, "Response body: $responseBody")

                when {
                    response.isSuccessful -> {
                        Result.success("Email queued successfully. Response: $responseBody")
                    }
                    code == 401 -> {
                        Result.failure(Exception("Authentication failed. Check your Mailgun API key."))
                    }
                    code == 400 -> {
                        Result.failure(Exception("Bad request. Error: $responseBody"))
                    }
                    code == 402 -> {
                        Result.failure(Exception("Payment required. Check your Mailgun account status."))
                    }
                    code == 404 -> {
                        Result.failure(Exception("Domain not found. Check your Mailgun domain configuration."))
                    }
                    code >= 500 -> {
                        Result.failure(Exception("Mailgun server error ($code). Try again later."))
                    }
                    else -> {
                        Result.failure(Exception("HTTP $code: $responseBody"))
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            Result.failure(Exception("Network error: ${e.message}. Check your internet connection."))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (!isValid) {
            Log.w(TAG, "Email validation failed for: $email")
        }
        return isValid
    }
}