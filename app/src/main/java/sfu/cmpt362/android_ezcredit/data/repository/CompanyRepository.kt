package sfu.cmpt362.android_ezcredit.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.FirebaseRefs
import sfu.cmpt362.android_ezcredit.data.dao.CompanyDao
import sfu.cmpt362.android_ezcredit.data.entity.Company

class CompanyRepository(private val companyDao: CompanyDao) {
    val companies: Flow<List<Company>> = companyDao.getCompanies()

    suspend fun insert(company: Company): Long {
        val ts = System.currentTimeMillis()
        val toInsert = company.copy(lastModified = ts, isDeleted = false)
        val newId = companyDao.insertCompany(toInsert)
        val finalCompany = toInsert.copy(id = newId)
        pushToFirebase(finalCompany)
        return newId
    }

    fun update(company: Company) {
        CoroutineScope(IO).launch {
            val updated = company.copy(lastModified = System.currentTimeMillis())
            companyDao.update(updated)
            pushToFirebase(updated)
        }
    }

    suspend fun getById(id: Long): Company {
        return companyDao.getCompanyById(id)
    }

    fun deleteById(id: Long) {
        CoroutineScope(IO).launch {
            val existing = companyDao.getCompanyById(id)
            val deleted = existing.copy(isDeleted = true, lastModified = System.currentTimeMillis())
            companyDao.deleteById(id)
            pushToFirebase(deleted)
        }
    }

    private fun pushToFirebase(company: Company) {
        val map = mapOf(
            "id" to company.id,
            "name" to company.name,
            "address" to company.address,
            "phone" to company.phone,
            "lastModified" to company.lastModified,
            "isDeleted" to company.isDeleted
        )
        FirebaseRefs.companyRef(company.id).updateChildren(map)
    }
}
