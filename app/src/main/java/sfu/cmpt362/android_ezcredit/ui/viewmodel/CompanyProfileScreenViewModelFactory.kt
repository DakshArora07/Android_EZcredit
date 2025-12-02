package sfu.cmpt362.android_ezcredit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sfu.cmpt362.android_ezcredit.data.viewmodel.CompanyViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModel

class CompanyProfileScreenViewModelFactory(
    private val companyViewModel: CompanyViewModel,
    private val userViewModel: UserViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanyProfileScreenViewModel::class.java)) {
            return CompanyProfileScreenViewModel(companyViewModel, userViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}