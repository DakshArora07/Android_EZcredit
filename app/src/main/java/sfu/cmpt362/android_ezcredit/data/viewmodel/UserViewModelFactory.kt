package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sfu.cmpt362.android_ezcredit.data.repository.UserRepository
import java.lang.IllegalArgumentException

class UserViewModelFactory (private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(UserViewModel::class.java))
            return UserViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}