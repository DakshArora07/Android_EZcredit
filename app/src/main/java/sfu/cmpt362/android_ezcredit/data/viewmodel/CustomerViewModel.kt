package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository
import kotlin.collections.isNotEmpty

class CustomerViewModel(private val repository: CustomerRepository) : ViewModel() {
    val customer = Customer()
    val customersLiveData: LiveData<List<Customer>> = repository.customers.asLiveData()

    fun insert(customer: Customer) {
        repository.insert(customer)
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