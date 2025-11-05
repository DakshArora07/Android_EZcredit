package sfu.cmpt362.android_ezcredit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sfu.cmpt362.android_ezcredit.data.dao.CustomerDao
import sfu.cmpt362.android_ezcredit.data.dao.InvoiceDao
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice

@Database(entities = [Invoice::class, Customer::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val customerDao: CustomerDao
    abstract val invoiceDao: InvoiceDao

    companion object{
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context) : AppDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "app_data").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}