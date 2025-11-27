package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository

class CustomerViewModel(private val repository: CustomerRepository) : ViewModel() {

    var customer by mutableStateOf(Customer())
        private set
    var customerFromDB by mutableStateOf<Customer?>(null)
        public set

    var customerFromUserInputOnAddMode by mutableStateOf<Customer?>(null)
        public set
    var creditText by mutableStateOf("")
        private set

    val customersLiveData: LiveData<List<Customer>> = repository.customers.asLiveData()

    fun updateCustomer(customerId:Long, name: String, email: String, phone: String):Customer {
        customer = if(customerId==-1L){
            customer.copy(
                name = name,
                email = email,
                phoneNumber = phone
            )
        }else{
            customer.copy(
                id = customerId,
                name = name,
                email = email,
                phoneNumber = phone
            )
        }
        return customer
    }

    fun updateCreditText(text: String) {
        creditText = text
    }

    fun insert() {
        repository.insert(customer)
        customer = Customer()
        creditText = ""
    }

    fun update(customer:Customer) {
        repository.update(customer)
    }

    suspend fun getCustomerById(id: Long): Customer {
        return repository.getById(id)
    }

    fun delete(id: Long){
        val customerList = customersLiveData.value
        if (customerList != null && customerList.isNotEmpty()){
            repository.deleteById(id)
        }
    }
}