package sfu.cmpt362.android_ezcredit.data.entity

import android.icu.util.Calendar
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_list",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customer_id"]
    )]
)
data class Invoice (

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "invoice_number")
    var invoiceNumber: String = "",

    @ColumnInfo(name = "customer_id")
    var customerID: Long = 0,

    @ColumnInfo(name = "invoice_date")
    var invDate: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "invoice_due_date")
    var dueDate: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "invoice_amount")
    var amount: Double = 0.0,

    @ColumnInfo(name = "invoice_status")
    var status: String = ""
)