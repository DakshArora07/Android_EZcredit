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

class CreditScoreUpdateWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("CreditScoreWorker", "Starting nightly credit score update")

        val database = AppDatabase.getInstance(applicationContext)
        val customerRepository = CustomerRepository(database.customerDao)
        val invoiceRepository = InvoiceRepository(database.invoiceDao)


        val customers = customerRepository.customers.first()
        if (customers.isEmpty()) {
            Log.d("CreditScoreWorker", "No customers in DB, skipping update")
            return@withContext Result.success()
        }

        val customerList = customerRepository.customers.first()

        customerList.forEach { customer ->
            try {
                val invoices = invoiceRepository.getInvoicesByCustomerId(customer.id)

                val newCreditScore = CreditScoreCalculator.calculateCreditScore(invoices)

                if (customer.creditScore != newCreditScore) {
                    val updatedCustomer = customer.copy(creditScore = newCreditScore)
                    customerRepository.update(updatedCustomer)
                    Log.d("CreditScoreWorker",
                        "Updated ${customer.name}: ${customer.creditScore} → $newCreditScore")
                }
            } catch (e: Exception) {
                Log.e("CreditScoreWorker", "Error updating customer ${customer.id}: ${e.message}")
            }
        }

        Log.d("CreditScoreWorker", "Credit score update completed")
        Result.success()
    }

}