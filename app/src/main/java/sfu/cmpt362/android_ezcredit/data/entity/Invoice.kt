package sfu.cmpt362.android_ezcredit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_list")
data class Invoice (

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)