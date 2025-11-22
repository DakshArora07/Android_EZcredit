package sfu.cmpt362.android_ezcredit.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.dao.CustomerDao
import sfu.cmpt362.android_ezcredit.data.entity.Customer

class CustomerRepository(private val customerDao: CustomerDao) {
    val customers: Flow<List<Customer>> = customerDao.getCustomers()

    fun insert(customer: Customer){
        CoroutineScope(IO).launch{
            customerDao.insertCustomer(customer)
        }
    }

    fun update(customer: Customer) {
        CoroutineScope(IO).launch {
            customerDao.update(customer)
        }
    }

    fun getById(id: Long): Customer{
        return customerDao.getCustomerById(id)
    }

    fun deleteById(id: Long){
        CoroutineScope(IO).launch {
            customerDao.deleteCustomerById(id)
        }
    }
}