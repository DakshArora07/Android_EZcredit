package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository

class CustomerViewModel(private val repository: CustomerRepository) : ViewModel() {

    var customer by mutableStateOf(Customer())
        private set

    var creditText by mutableStateOf("")
        private set

    val customersLiveData: LiveData<List<Customer>> = repository.customers.asLiveData()

    fun updateCustomer(name: String, email: String, phone: String, credit: Double) {
        customer = customer.copy(
            name = name,
            email = email,
            phoneNumber = phone,
            credit = credit
        )
    }

    fun updateCreditText(text: String) {
        creditText = text
    }

    fun insert() {
        repository.insert(customer)
        customer = Customer()
        creditText = ""
    }

    fun update(customer: Customer) {
        repository.update(customer)
    }

    fun getById(id: Long): Customer {
        return repository.getById(id)
    }

    fun delete(id: Long){
        val customerList = customersLiveData.value
        if (customerList != null && customerList.isNotEmpty()){
            repository.deleteById(id)
        }
    }
}