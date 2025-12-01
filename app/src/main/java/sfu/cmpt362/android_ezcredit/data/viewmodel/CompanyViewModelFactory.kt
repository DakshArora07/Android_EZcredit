package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sfu.cmpt362.android_ezcredit.data.repository.CompanyRepository
import java.lang.IllegalArgumentException

class CompanyViewModelFactory (private val repository: CompanyRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(CompanyViewModel::class.java))
            return CompanyViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}