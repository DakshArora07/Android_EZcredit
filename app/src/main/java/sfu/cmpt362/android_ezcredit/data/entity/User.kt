package sfu.cmpt362.android_ezcredit.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import sfu.cmpt362.android_ezcredit.utils.AccessMode

@Entity(tableName = "user_list",
        foreignKeys = [ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["company_id"]
        )]
)
data class User (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "email")
    var email: String = "",

    @ColumnInfo(name = "company_id")
    var companyId: Long = 0,

    @ColumnInfo(name = "access_level")
    var accessLevel: AccessMode = AccessMode.Admin,

    var lastModified: Long = 0L,

    var isDeleted: Boolean = false
)