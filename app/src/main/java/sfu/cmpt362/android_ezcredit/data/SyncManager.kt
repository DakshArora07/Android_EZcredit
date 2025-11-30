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
import sfu.cmpt362.android_ezcredit.data.dao.ReceiptDao
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus

class SyncManager(
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val receiptDao: ReceiptDao,
    private val scope: CoroutineScope
) {
    private val customersRef = FirebaseRefs.customersRef
    private val invoicesRef = FirebaseRefs.invoicesRef
    private val receiptsRef = FirebaseRefs.receiptsRef

    private var isInitialSyncDone = false

    fun start() {
        // First do initial sync, then start realtime updates
        if (!isInitialSyncDone) {
            performInitialOrderedSync()
        } else {
            attachLiveListeners()
        }
    }

    private fun performInitialOrderedSync() {
        customersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    processCustomers(snapshot)

                    // After customers are synced already, sync invoices
                    syncInvoicesAfterCustomers()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun syncInvoicesAfterCustomers() {
        invoicesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    processInvoices(snapshot)
                    // After invoices are synced already, sync receipts
                    syncReceiptsAfterInvoices()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun syncReceiptsAfterInvoices() {
        receiptsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    processReceipts(snapshot)
                    // When the Initial sync is complete, start realtime updates
                    isInitialSyncDone = true
                    attachLiveListeners()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachLiveListeners() {
        attachCustomersListener()
        attachInvoicesListener()
        attachReceiptsListener()
    }

    private fun attachCustomersListener() {
        customersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) { processCustomers(snapshot) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachInvoicesListener() {
        invoicesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) { processInvoices(snapshot) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachReceiptsListener() {
        receiptsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) { processReceipts(snapshot) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private suspend fun processCustomers(snapshot: DataSnapshot) {
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
                    customerDao.upsert(Customer(id, name, email, phone, creditScore, credit, lastModified, isDeleted))
                }
            }
        }
    }

    private suspend fun processInvoices(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
            val invoiceNumber = snap.child("invoiceNumber").getValue(String::class.java) ?: ""
            val customerID = snap.child("customerID").getValue(Long::class.java) ?: 0L
            val invDateMillis = snap.child("invDate").getValue(Long::class.java) ?: 0L
            val dueDateMillis = snap.child("dueDate").getValue(Long::class.java) ?: 0L
            val amount = snap.child("amount").getValue(Double::class.java) ?: 0.0
            val statusStr = snap.child("status").getValue(String::class.java) ?: "Unpaid"
            val status = try { InvoiceStatus.valueOf(statusStr) } catch (e: Exception) { InvoiceStatus.Unpaid }
            val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
            val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

            val local = invoiceDao.getInvoiceByIdOrNull(id)
            val localNewer = local != null && local.lastModified > lastModified

            if (!localNewer) {
                if (isDeleted) {
                    invoiceDao.deleteInvoiceById(id)
                } else {
                    invoiceDao.upsert(Invoice(
                        id = id,
                        invoiceNumber = invoiceNumber,
                        customerID = customerID,
                        invDate = invDateMillis.toCalendar(),
                        dueDate = dueDateMillis.toCalendar(),
                        amount = amount,
                        status = status,
                        lastModified = lastModified,
                        isDeleted = isDeleted
                    ))
                }
            }
        }
    }

    private suspend fun processReceipts(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
            val receiptNumber = snap.child("receiptNumber").getValue(String::class.java) ?: ""
            val receiptDateMillis = snap.child("receiptDate").getValue(Long::class.java) ?: 0L
            val invoiceID = snap.child("invoiceID").getValue(Long::class.java) ?: 0L
            val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
            val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

            val local = receiptDao.getReceiptByIdOrNull(id)
            val localNewer = local != null && local.lastModified > lastModified

            if (!localNewer) {
                if (isDeleted) {
                    receiptDao.deleteReceiptById(id)
                } else {
                    receiptDao.upsert(Receipt(
                        id = id,
                        receiptNumber = receiptNumber,
                        receiptDate = receiptDateMillis.toCalendar(),
                        invoiceID = invoiceID,
                        lastModified = lastModified,
                        isDeleted = isDeleted
                    ))
                }
            }
        }
    }

    private fun Long.toCalendar(): Calendar =
        Calendar.getInstance().apply { timeInMillis = this@toCalendar }
}