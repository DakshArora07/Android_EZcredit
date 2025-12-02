package sfu.cmpt362.android_ezcredit.data

import android.icu.util.Calendar
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sfu.cmpt362.android_ezcredit.data.dao.*
import sfu.cmpt362.android_ezcredit.data.entity.*
import sfu.cmpt362.android_ezcredit.utils.AccessMode
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

    companion object {
        private const val TAG = "SyncManager"
    }

    // Phase 1: Sync companies and users globally on app startup
    fun startInitialSync() {
        companiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    try {
                        processCompanies(snapshot)
                        syncUsersAfterCompanies(snapshot)
                        isInitialSyncDone = true
                        Log.d(TAG, "Initial sync completed successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during initial sync", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Initial sync cancelled: ${error.message}")
            }
        })
    }

    private suspend fun syncUsersAfterCompanies(companiesSnapshot: DataSnapshot) {
        companiesSnapshot.children.forEach { companySnap ->
            val companyId = companySnap.key?.toLongOrNull() ?: return@forEach
            try {
                val usersSnapshot = FirebaseRefs.usersRef(companyId).awaitSingleValue()
                processUsers(usersSnapshot, companyId)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing users for company $companyId", e)
            }
        }
    }

    // Phase 2: Sync detailed company data after company selection/login
    fun startCompanyDataSync(companyId: Long) {
        scope.launch(IO) {
            try {
                Log.d(TAG, "Starting company data sync for company $companyId")

                // Step 1: Sync customers first (no dependencies)
                val customersSnapshot = FirebaseRefs.customersRef(companyId).awaitSingleValue()
                processCustomers(customersSnapshot)
                Log.d(TAG, "Customers synced for company $companyId")

                // Step 2: Sync invoices (depends on customers)
                val invoicesSnapshot = FirebaseRefs.invoicesRef(companyId).awaitSingleValue()
                processInvoices(invoicesSnapshot)
                Log.d(TAG, "Invoices synced for company $companyId")

                // Step 3: Sync receipts (depends on invoices)
                val receiptsSnapshot = FirebaseRefs.receiptsRef(companyId).awaitSingleValue()
                processReceipts(receiptsSnapshot)
                Log.d(TAG, "Receipts synced for company $companyId")

                // Step 4: Attach real-time listeners
                attachCompanySpecificListeners(companyId)
                Log.d(TAG, "Real-time listeners attached for company $companyId")

            } catch (e: Exception) {
                Log.e(TAG, "Error during company data sync for company $companyId", e)
            }
        }
    }

    private fun attachCompanySpecificListeners(companyId: Long) {
        FirebaseRefs.customersRef(companyId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    try {
                        processCustomers(snapshot)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing customers update", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Customers listener cancelled: ${error.message}")
            }
        })

        FirebaseRefs.invoicesRef(companyId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    try {
                        processInvoices(snapshot)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing invoices update", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Invoices listener cancelled: ${error.message}")
            }
        })

        FirebaseRefs.receiptsRef(companyId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(IO) {
                    try {
                        processReceipts(snapshot)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing receipts update", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Receipts listener cancelled: ${error.message}")
            }
        })
    }

    // Processing methods

    private suspend fun processCompanies(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            try {
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
                        Log.d(TAG, "Deleted company $id")
                    } else {
                        companyDao.upsert(
                            Company(
                                id = id,
                                name = name,
                                address = address,
                                phone = phone,
                                lastModified = lastModified,
                                isDeleted = isDeleted
                            )
                        )
                        Log.d(TAG, "Upserted company $id: $name")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing company", e)
            }
        }
    }

    private suspend fun processUsers(snapshot: DataSnapshot, companyId: Long) {
        snapshot.children.forEach { snap ->
            try {
                val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
                val name = snap.child("name").getValue(String::class.java) ?: ""
                val email = snap.child("email").getValue(String::class.java) ?: ""
                val accessLevelStr = snap.child("accessLevel").getValue(String::class.java) ?: "Admin"
                val accessLevel = try {
                    AccessMode.valueOf(accessLevelStr)
                } catch (e: Exception) {
                    AccessMode.Admin
                }
                val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
                val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

                val local = userDao.getUserByIdOrNull(id)
                val localNewer = local != null && local.lastModified > lastModified

                if (!localNewer) {
                    if (isDeleted) {
                        userDao.deleteById(id)
                        Log.d(TAG, "Deleted user $id")
                    } else {
                        userDao.upsert(
                            User(
                                id = id,
                                name = name,
                                email = email,
                                companyId = companyId,
                                accessLevel = accessLevel,
                                lastModified = lastModified,
                                isDeleted = isDeleted
                            )
                        )
                        Log.d(TAG, "Upserted user $id: $name")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing user", e)
            }
        }
    }

    private suspend fun processCustomers(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            try {
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
                        Log.d(TAG, "Deleted customer $id")
                    } else {
                        customerDao.upsert(
                            Customer(
                                id = id,
                                name = name,
                                email = email,
                                phoneNumber = phoneNumber,
                                creditScore = creditScore,
                                credit = credit,
                                lastModified = lastModified,
                                isDeleted = isDeleted
                            )
                        )
                        Log.d(TAG, "Upserted customer $id: $name")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing customer", e)
            }
        }
    }

    private suspend fun processInvoices(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            try {
                val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
                val invoiceNumber = snap.child("invoiceNumber").getValue(String::class.java) ?: ""

                // FIX: Changed from "customerID" to "customerId" to match what InvoiceRepository writes
                val customerId = snap.child("customerId").getValue(Long::class.java) ?: 0L

                val invDateMillis = snap.child("invDate").getValue(Long::class.java) ?: 0L
                val dueDateMillis = snap.child("dueDate").getValue(Long::class.java) ?: 0L
                val amount = snap.child("amount").getValue(Double::class.java) ?: 0.0
                val statusStr = snap.child("status").getValue(String::class.java) ?: "Unpaid"
                val status = try {
                    InvoiceStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    InvoiceStatus.Unpaid
                }
                val url = snap.child("url").getValue(String::class.java) ?: ""
                val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
                val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

                Log.d(TAG, "Processing invoice $id with customerId: $customerId")

                // Verify customer exists before inserting invoice
                val customerExists = customerDao.getCustomerByIdOrNull(customerId) != null
                if (!customerExists && customerId != 0L) {
                    Log.w(TAG, "Skipping invoice $id: customer $customerId not found")
                    return@forEach
                }

                val local = invoiceDao.getInvoiceByIdOrNull(id)
                val localNewer = local != null && local.lastModified > lastModified

                if (!localNewer) {
                    if (isDeleted) {
                        invoiceDao.deleteInvoiceById(id)
                        Log.d(TAG, "Deleted invoice $id")
                    } else {
                        invoiceDao.upsert(
                            Invoice(
                                id = id,
                                invoiceNumber = invoiceNumber,
                                customerId = customerId,
                                invDate = invDateMillis.toCalendar(),
                                dueDate = dueDateMillis.toCalendar(),
                                amount = amount,
                                url = url,
                                status = status,
                                lastModified = lastModified,
                                isDeleted = isDeleted
                            )
                        )
                        Log.d(TAG, "Upserted invoice $id: $invoiceNumber for customer $customerId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing invoice", e)
            }
        }
    }

    private suspend fun processReceipts(snapshot: DataSnapshot) {
        snapshot.children.forEach { snap ->
            try {
                val id = snap.child("id").getValue(Long::class.java) ?: return@forEach
                val receiptNumber = snap.child("receiptNumber").getValue(String::class.java) ?: ""
                val receiptDateMillis = snap.child("receiptDate").getValue(Long::class.java) ?: 0L

                // FIX: Changed from "invoiceID" to "invoiceId" to match what ReceiptRepository writes
                val invoiceId = snap.child("invoiceId").getValue(Long::class.java) ?: 0L

                val lastModified = snap.child("lastModified").getValue(Long::class.java) ?: 0L
                val isDeleted = snap.child("isDeleted").getValue(Boolean::class.java) ?: false

                Log.d(TAG, "Processing receipt $id with invoiceId: $invoiceId")

                // Verify invoice exists before inserting receipt
                val invoiceExists = invoiceDao.getInvoiceByIdOrNull(invoiceId) != null
                if (!invoiceExists && invoiceId != 0L) {
                    Log.w(TAG, "Skipping receipt $id: invoice $invoiceId not found")
                    return@forEach
                }

                val local = receiptDao.getReceiptByIdOrNull(id)
                val localNewer = local != null && local.lastModified > lastModified

                if (!localNewer) {
                    if (isDeleted) {
                        receiptDao.deleteReceiptById(id)
                        Log.d(TAG, "Deleted receipt $id")
                    } else {
                        receiptDao.upsert(
                            Receipt(
                                id = id,
                                receiptNumber = receiptNumber,
                                receiptDate = receiptDateMillis.toCalendar(),
                                invoiceId = invoiceId,
                                lastModified = lastModified,
                                isDeleted = isDeleted
                            )
                        )
                        Log.d(TAG, "Upserted receipt $id: $receiptNumber for invoice $invoiceId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing receipt", e)
            }
        }
    }

    // Helper functions

    private suspend fun com.google.firebase.database.DatabaseReference.awaitSingleValue(): DataSnapshot =
        suspendCancellableCoroutine { cont ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cont.resume(snapshot)
                }
                override fun onCancelled(error: DatabaseError) {
                    cont.resumeWithException(Exception(error.message))
                }
            }
            addListenerForSingleValueEvent(listener)
            cont.invokeOnCancellation { removeEventListener(listener) }
        }

    private fun Long.toCalendar(): Calendar = Calendar.getInstance().apply {
        timeInMillis = this@toCalendar
    }

    fun clearCompanyDataSync() {
        try {
            Log.d(TAG, "Clearing company data in correct FK order")

            // 1. Delete LEAF tables first (no dependencies)
            receiptDao.deleteAll()      // receipts → invoices FK
            Log.d(TAG, "Receipts cleared")

            // 2. Delete middle tables
            invoiceDao.deleteAll()      // invoices → customers FK
            Log.d(TAG, "Invoices cleared")

            // 3. Delete root tables last
            customerDao.deleteAll()
            Log.d(TAG, "Customers cleared")

            Log.d(TAG, "✅ Company data cleared successfully (FK safe)")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing company data", e)
        }
    }
}