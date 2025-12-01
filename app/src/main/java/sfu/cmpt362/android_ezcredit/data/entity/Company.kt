package sfu.cmpt362.android_ezcredit.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_list")
data class Company (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "address")
    var address: String = "",

    @ColumnInfo(name = "phone")
    var phone: String = "",

    var lastModified: Long = 0L,

    var isDeleted: Boolean = false
)