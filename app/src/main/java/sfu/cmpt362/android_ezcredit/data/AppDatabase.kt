package sfu.cmpt362.android_ezcredit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sfu.cmpt362.android_ezcredit.data.dao.CompanyDao
import sfu.cmpt362.android_ezcredit.data.dao.CustomerDao
import sfu.cmpt362.android_ezcredit.data.dao.InvoiceDao
import sfu.cmpt362.android_ezcredit.data.dao.ReceiptDao
import sfu.cmpt362.android_ezcredit.data.dao.UserDao
import sfu.cmpt362.android_ezcredit.data.entity.Company
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.entity.Receipt
import sfu.cmpt362.android_ezcredit.data.entity.User

@Database(
    entities = [Invoice::class, Customer::class, Receipt::class, Company::class, User::class],
    version = 5
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val customerDao: CustomerDao
    abstract val invoiceDao: InvoiceDao
    abstract val receiptDao: ReceiptDao
    abstract val companyDao: CompanyDao
    abstract val userDao: UserDao

    companion object {
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, "app_data"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}