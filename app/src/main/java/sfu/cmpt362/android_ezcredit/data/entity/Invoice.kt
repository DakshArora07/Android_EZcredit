package sfu.cmpt362.android_ezcredit.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "invoice_list")
data class Invoice (

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String = "",

    @ColumnInfo(name = "customer_id")
    val customerID: Int = 0,

    @ColumnInfo(name = "invoice_due_date")
    val dueDate: Date = Date(),

    @ColumnInfo(name = "invoice_amount")
    val amount: Double = 0.0,

    @ColumnInfo(name = "invoice_status")
    val status: String = ""

)