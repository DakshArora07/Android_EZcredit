package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sfu.cmpt362.android_ezcredit.data.repository.InvoiceRepository
import java.lang.IllegalArgumentException

class InvoiceViewModelFactory (private val repository: InvoiceRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(InvoiceViewModel::class.java))
            return InvoiceViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}