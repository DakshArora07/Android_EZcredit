package sfu.cmpt362.android_ezcredit.data

import android.icu.util.Calendar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.dao.CompanyDao
import sfu.cmpt362.android_ezcredit.data.dao.CustomerDao
import sfu.cmpt362.android_ezcredit.data.dao.InvoiceDao
import sfu.cmpt362.android_ezcredit.data.dao.ReceiptDao
import sfu.cmpt362.android_ezcredit.data.dao.UserDao
import sfu.cmpt362.android_ezcredit.data.entity.Company
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt
import sfu.cmpt362.android_ezcredit.data.entity.User
import sfu.cmpt362.android_ezcredit.utils.AccessMode
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus

class SyncManager(
    private val companyDao: CompanyDao,
    private val userDao: UserDao,
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val receiptDao: ReceiptDao,
    private val scope: CoroutineScope
) {
    private val companiesRef = FirebaseRefs.companiesRef()
    private var isInitialSyncDone = false

    fun start() {
        if (!isInitialSyncDone) {
            performInitialOrderedSync()
        } else {
            attachLiveListeners()
        }
    }

    private fun performInitialOrderedSync() {
        // 1. COMPANIES FIRST (parents for everything)
        companiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    processCompanies(snapshot)
                    syncUsersAfterCompanies()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun syncUsersAfterCompanies() {
        // 2. USERS (FK to Company)
        companiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.usersRef(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                scope.launch(IO) {
                                    processUsers(userSnapshot, companyId)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    syncCustomersAfterUsers()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun syncCustomersAfterUsers() {
        // 3. CUSTOMERS
        companiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.customersRef(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(customerSnapshot: DataSnapshot) {
                                scope.launch(IO) {
                                    processCustomers(customerSnapshot)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    syncInvoicesAfterCustomers()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun syncInvoicesAfterCustomers() {
        // 4. INVOICES
        companiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.invoicesRef(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(invoiceSnapshot: DataSnapshot) {
                                scope.launch(IO) {
                                    processInvoices(invoiceSnapshot)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    syncReceiptsAfterInvoices()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun syncReceiptsAfterInvoices() {
        // 5. RECEIPTS (FK to Invoice)
        companiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.receiptsRef(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(receiptSnapshot: DataSnapshot) {
                                scope.launch(IO) {
                                    processReceipts(receiptSnapshot)
                                    // Initial sync complete!
                                    isInitialSyncDone = true
                                    attachLiveListeners()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachLiveListeners() {
        attachCompaniesListener()
        attachUsersListener()
        attachCustomersListener()
        attachInvoicesListener()
        attachReceiptsListener()
    }

    private fun attachCompaniesListener() {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) { processCompanies(snapshot) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachUsersListener() {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.usersRef(companyId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                scope.launch(IO) { processUsers(userSnapshot, companyId) }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachCustomersListener() {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.customersRef(companyId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(customerSnapshot: DataSnapshot) {
                                scope.launch(IO) { processCustomers(customerSnapshot) }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachInvoicesListener() {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.invoicesRef(companyId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(invoiceSnapshot: DataSnapshot) {
                                scope.launch(IO) { processInvoices(invoiceSnapshot) }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun attachReceiptsListener() {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    snapshot.children.forEach { companySnap ->
                        val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
                        FirebaseRefs.receiptsRef(companyId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(receiptSnapshot: DataSnapshot) {
                                scope.launch(IO) { processReceipts(receiptSnapshot) }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // PROCESSING METHODS
    private suspend fun processCompanies(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
            val name = snap.child("name").getValue(String::class.java) ?: ""
            val address = snap.child("address").getValue(String::class.java) ?: ""
            val phone = snap.child("phone").getValue(String::class.java) ?: ""
            val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
            val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

            val local = companyDao.getCompanyByIdOrNull(id)
            val localNewer = local != null && local.lastModified > lastModified
            if (!localNewer) {
                if (isDeleted) {
                    companyDao.deleteById(id)
                } else {
                    companyDao.upsert(Company(
                        id = id, name = name, address = address, phone = phone,
                        lastModified = lastModified, isDeleted = isDeleted
                    ))
                }
            }
        }
    }

    private suspend fun processUsers(snapshot: DataSnapshot, companyId: Long) {
        snapshot.children.forEach { snap ->
            val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
            val name = snap.child("name").getValue(String::class.java) ?: ""
            val email = snap.child("email").getValue(String::class.java) ?: ""
            val accessLevelStr = snap.child("accessLevel").getValue(String::class.java) ?: "Admin"
            val accessLevel = try { AccessMode.valueOf(accessLevelStr) } catch (e: Exception) { AccessMode.Admin }
            val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
            val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

            val local = userDao.getUserByIdOrNull(id)
            val localNewer = local != null && local.lastModified > lastModified
            if (!localNewer) {
                if (isDeleted) {
                    userDao.deleteById(id)
                } else {
                    userDao.upsert(User(
                        id = id, name = name, email = email, companyId = companyId,
                        accessLevel = accessLevel, lastModified = lastModified, isDeleted = isDeleted
                    ))
                }
            }
        }
    }

    private suspend fun processCustomers(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
            val name = snap.child("name").getValue(String::class.java) ?: ""
            val email = snap.child("email").getValue(String::class.java) ?: ""
            val phoneNumber = snap.child("phoneNumber").getValue(String::class.java) ?: ""
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
                    customerDao.upsert(Customer(
                        id = id, name = name, email = email, phoneNumber = phoneNumber,
                        creditScore = creditScore, credit = credit,
                        lastModified = lastModified, isDeleted = isDeleted
                    ))
                }
            }
        }
    }

    private suspend fun processInvoices(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
            val invoiceNumber = snap.child("invoiceNumber").getValue(String::class.java) ?: ""
            val customerId = snap.child("customerID").getValue(Long::class.java) ?: 0L
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
                        id = id, invoiceNumber = invoiceNumber, customerId = customerId,
                        invDate = invDateMillis.toCalendar(), dueDate = dueDateMillis.toCalendar(),
                        amount = amount, status = status,
                        lastModified = lastModified, isDeleted = isDeleted
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
            val invoiceId = snap.child("invoiceID").getValue(Long::class.java) ?: 0L
            val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
            val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

            val local = receiptDao.getReceiptByIdOrNull(id)
            val localNewer = local != null && local.lastModified > lastModified
            if (!localNewer) {
                if (isDeleted) {
                    receiptDao.deleteReceiptById(id)
                } else {
                    receiptDao.upsert(Receipt(
                        id = id, receiptNumber = receiptNumber,
                        receiptDate = receiptDateMillis.toCalendar(), invoiceId = invoiceId,
                        lastModified = lastModified, isDeleted = isDeleted
                    ))
                }
            }
        }
    }

    private fun Long.toCalendar(): Calendar = Calendar.getInstance().apply { timeInMillis = this@toCalendar }
}
