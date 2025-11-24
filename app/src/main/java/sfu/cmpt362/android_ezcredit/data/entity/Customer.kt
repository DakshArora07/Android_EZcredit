package sfu.cmpt362.android_ezcredit.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import sfu.cmpt362.android_ezcredit.utils.CreditScoreCalculator

@Entity(tableName = "customer_list")
data class Customer (

    @PrimaryKey (autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "customer_name")
    val name : String = "",

    @ColumnInfo(name = "customer_email")
    val email: String = "",

    @ColumnInfo(name = "customer_phone_number")
    val phoneNumber: String = "",

    @ColumnInfo(name = "customer_credit_score")
    val creditScore: Int = CreditScoreCalculator.BASE_SCORE,

    @ColumnInfo(name = "customer_credit")
    val credit: Double = 0.0,

    val lastModified: Long = 0L,

    val isDeleted: Boolean = false
)