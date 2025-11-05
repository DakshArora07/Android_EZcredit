package sfu.cmpt362.android_ezcredit.data

import android.icu.util.Calendar
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCalendar(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun toCalendar(millis: Long?): Calendar? {
        return millis?.let {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it
            calendar
        }
    }
}