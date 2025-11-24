package sfu.cmpt362.android_ezcredit.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseRefs {
    private val db = FirebaseDatabase.getInstance()

    val customersRef: DatabaseReference
        get() = db.getReference("ezcredit").child("customers")

    val invoicesRef: DatabaseReference
        get() = db.getReference("ezcredit").child("invoices")
}