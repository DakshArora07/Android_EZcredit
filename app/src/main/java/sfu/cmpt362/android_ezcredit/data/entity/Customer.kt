package sfu.cmpt362.android_ezcredit.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_list")
data class Customer (

    @PrimaryKey (autoGenerate = true)
    val id: Long = 0L

)