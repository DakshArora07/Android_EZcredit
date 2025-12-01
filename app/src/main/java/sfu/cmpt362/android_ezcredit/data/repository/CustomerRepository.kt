package sfu.cmpt362.android_ezcredit.data.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.FirebaseRefs
import sfu.cmpt362.android_ezcredit.data.dao.CustomerDao
import sfu.cmpt362.android_ezcredit.data.entity.Customer

class CustomerRepository(private val customerDao: CustomerDao) {
    private val companyId: Long get() = CompanyContext.currentCompanyId
        ?: error("No company selected")
    private val customersRef: DatabaseReference
        get() = FirebaseRefs.customersRef(companyId)

    val customers: Flow<List<Customer>> = customerDao.getCustomers()

    fun insert(customer: Customer){
        CoroutineScope(IO).launch{
            val ts = System.currentTimeMillis()
            val toInsert = customer.copy(lastModified = ts, isDeleted = false)
            val newId = customerDao.insertCustomer(toInsert)
            val finalCustomer = toInsert.copy(id = newId)
            pushToFirebase(finalCustomer)
        }
    }

    fun update(customer: Customer) {
        CoroutineScope(IO).launch {
            val updated = customer.copy(lastModified = System.currentTimeMillis())
            customerDao.update(updated)
            pushToFirebase(updated)
        }
    }

    suspend fun getById(id: Long): Customer{
        return customerDao.getCustomerById(id)
    }

    fun deleteById(id: Long){
        CoroutineScope(IO).launch {
            val existing = customerDao.getCustomerById(id)
            val deleted = existing.copy(
                isDeleted = true,
                lastModified = System.currentTimeMillis()
            )
            customerDao.deleteCustomerById(id)
            pushToFirebase(deleted)
        }
    }

    private fun pushToFirebase(customer: Customer) {
        val map = mapOf(
            "id" to customer.id,
            "name" to customer.name,
            "email" to customer.email,
            "phoneNumber" to customer.phoneNumber,
            "creditScore" to customer.creditScore,
            "credit" to customer.credit,
            "lastModified" to customer.lastModified,
            "isDeleted" to customer.isDeleted
        )
        customersRef.child(customer.id.toString()).setValue(map)
    }
}