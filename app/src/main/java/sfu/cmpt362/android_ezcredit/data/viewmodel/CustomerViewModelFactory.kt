package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sfu.cmpt362.android_ezcredit.data.repository.CustomerRepository
import java.lang.IllegalArgumentException

class CustomerViewModelFactory (private val repository: CustomerRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(CustomerViewModel::class.java))
            return CustomerViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}