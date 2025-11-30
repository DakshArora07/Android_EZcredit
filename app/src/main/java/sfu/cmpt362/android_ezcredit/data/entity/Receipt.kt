package sfu.cmpt362.android_ezcredit.data.entity

import android.icu.util.Calendar
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_list",
    foreignKeys = [ForeignKey(
        entity = Invoice::class,
        parentColumns = ["id"],
        childColumns = ["invoice_id"]
    )]
)
data class Receipt (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "receipt_number")
    var receiptNumber: String = "",

    @ColumnInfo(name = "receipt_date")
    var receiptDate: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "invoice_id")
    var invoiceID: Long = 0,

    var lastModified: Long = 0L,

    var isDeleted: Boolean = false
)