package sfu.cmpt362.android_ezcredit.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import sfu.cmpt362.android_ezcredit.utils.CreditScoreCalculator

// Background Worker to update credit scores of customers
class CreditScoreUpdateWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("CreditScoreWorker", "Starting nightly credit score update")

        val database = AppDatabase.getInstance(applicationContext)
        val customerRepository = CustomerRepository(database.customerDao)
        val invoiceRepository = InvoiceRepository(database.invoiceDao)

        var updateCount = 0

        // All customers from database
        val customerList = customerRepository.customers.first()
        customerList.forEach { customer ->
            try {
                // Scan all invoices of a particular customer
                val invoices = invoiceRepository.getInvoicesByCustomerId(customer.id)
                // Calculate credit score
                val newCreditScore = CreditScoreCalculator.calculateCreditScore(invoices)

                // Update credit score
                if (customer.creditScore != newCreditScore) {
                    val updatedCustomer = customer.copy(creditScore = newCreditScore)
                    customerRepository.update(updatedCustomer)
                    updateCount ++
                    Log.d("CreditScoreWorker",
                        "Updated ${customer.name}: ${customer.creditScore} → $newCreditScore")
                }
            } catch (e: Exception) {
                Log.e("CreditScoreWorker", "Error updating customer ${customer.id}: ${e.message}")
            }
        }

        // Save Daily Summary
        saveSummaryData(updateCount)

        Log.d("CreditScoreWorker", "Credit score update completed - $updateCount customers updated")
        Result.success()
    }

    private fun saveSummaryData(updateCount: Int) {
        val prefs = applicationContext.getSharedPreferences(
            DailySummaryWorker.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit()
            .putInt(DailySummaryWorker.KEY_CREDIT_SCORE_UPDATES, updateCount)
            .apply()
        Log.d("CreditScoreWorker", "Credit score summary saved - Updates: $updateCount")
    }
}