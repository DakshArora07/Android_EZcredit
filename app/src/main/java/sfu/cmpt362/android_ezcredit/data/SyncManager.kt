package sfu.cmpt362.android_ezcredit.data

import android.icu.util.Calendar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.dao.CustomerDao
import sfu.cmpt362.android_ezcredit.data.dao.InvoiceDao
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus

class SyncManager(
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val scope: CoroutineScope
) {

    private val customersRef = FirebaseRefs.customersRef
    private val invoicesRef = FirebaseRefs.invoicesRef

    fun start() {
        customersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { snap ->
                        val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
                        val name = snap.child("name").getValue(String::class.java) ?: ""
                        val email = snap.child("email").getValue(String::class.java) ?: ""
                        val phone = snap.child("phoneNumber").getValue(String::class.java) ?: ""
                        val creditScore = snap.child("creditScore").getValue(Int::class.java) ?: 0
                        val credit = snap.child("credit").getValue(Double::class.java) ?: 0.0
                        val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
                        val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

                        val local = customerDao.getCustomerByIdOrNull(id)
                        val localNewer = local != null && local.lastModified > lastModified

                        if (!localNewer) {
                            if (isDeleted) {
                                customerDao.deleteCustomerById(id)
                            } else {
                                customerDao.upsert(
                                    Customer(
                                        id, name, email, phone,
                                        creditScore, credit, lastModified, isDeleted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        invoicesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { snap ->
                        val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
                        val invoiceNumber = snap.child("invoiceNumber").getValue(String::class.java) ?: ""
                        val customerID = snap.child("customerID").getValue(Long::class.java) ?: 0L
                        val invDateMillis = snap.child("invDate").getValue(Long::class.java) ?: 0L
                        val dueDateMillis = snap.child("dueDate").getValue(Long::class.java) ?: 0L
                        val amount = snap.child("amount").getValue(Double::class.java) ?: 0.0
                        val status = snap.child("status").getValue(InvoiceStatus::class.java) ?: InvoiceStatus.Unpaid
                        val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
                        val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

                        val local = invoiceDao.getInvoiceByIdOrNull(id)
                        val localNewer = local != null && local.lastModified > lastModified

                        if (!localNewer) {
                            if (isDeleted) {
                                invoiceDao.deleteInvoiceById(id)
                            } else {
                                invoiceDao.upsert(
                                    Invoice(
                                        id = id,
                                        invoiceNumber = invoiceNumber,
                                        customerID = customerID,
                                        invDate = invDateMillis.toCalendar(),
                                        dueDate = dueDateMillis.toCalendar(),
                                        amount = amount,
                                        status = status,
                                        lastModified = lastModified,
                                        isDeleted = isDeleted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun Long.toCalendar(): Calendar =
        Calendar.getInstance().apply { timeInMillis = this@toCalendar }
}