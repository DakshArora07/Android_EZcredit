package sfu.cmpt362.android_ezcredit.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseRefs {
    private val db = FirebaseDatabase.getInstance()

    // Global collections (scan all companies)
    fun companiesRef(): DatabaseReference = db.getReference("companies")

    // Company-specific
    fun companyRef(companyId: Long): DatabaseReference = companiesRef().child(companyId.toString())
    fun usersRef(companyId: Long): DatabaseReference = companyRef(companyId).child("users")
    fun dataRef(companyId: Long): DatabaseReference = companyRef(companyId).child("data")
    fun customersRef(companyId: Long): DatabaseReference = dataRef(companyId).child("customers")
    fun invoicesRef(companyId: Long): DatabaseReference = dataRef(companyId).child("invoices")
    fun receiptsRef(companyId: Long): DatabaseReference = dataRef(companyId).child("receipts")
}