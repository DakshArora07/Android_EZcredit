package sfu.cmpt362.android_ezcredit.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import sfu.cmpt362.android_ezcredit.data.entity.Company
import sfu.cmpt362.android_ezcredit.data.repository.CompanyRepository

class CompanyViewModel(private val repository: CompanyRepository) : ViewModel() {

    var company by mutableStateOf(Company())
        private set

    val companiesLiveData: LiveData<List<Company>> = repository.companies.asLiveData()

    fun updateCompany(
        companyId: Long = -1L,
        name: String,
        address: String,
        phone: String
    ) {
        if (companyId == -1L) {
            company = company.copy(
                name = name,
                address = address,
                phone = phone
            )
        } else {
            company = company.copy(
                id = companyId,
                name = name,
                address = address,
                phone = phone
            )
        }
    }

    fun update() {
        repository.update(company)
    }

    suspend fun insert(): Long {
        val id = repository.insert(company)
        company = company.copy(id = id)
        return id
    }

    suspend fun getCompanyById(id: Long): Company {
        return repository.getById(id)
    }

    fun delete(id: Long) {
        val companyList = companiesLiveData.value
        if (companyList != null && companyList.isNotEmpty()) {
            repository.deleteById(id)
        }
    }
}
