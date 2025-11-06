package sfu.cmpt362.android_ezcredit.ui.viewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.Calendar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import sfu.cmpt362.android_ezcredit.ui.screens.DueDate


class CalendarScreenViewModel : ViewModel() {
    val today = Calendar.getInstance()
    var currentDate by mutableStateOf(Calendar.getInstance())
        private set

    var selectedDate by mutableStateOf<DueDate?>(null)
        private set

    fun updateCurrentDate(newDate: Calendar) {
        currentDate = newDate
    }

    fun updateSelectedDate(newDate: DueDate?) {
        selectedDate = newDate
    }

}