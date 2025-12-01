package sfu.cmpt362.android_ezcredit.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular
import sfu.cmpt362.android_ezcredit.utils.PreferenceManager

class SettingsScreenViewModel : ViewModel() {

    // Invoice Reminder States
    private val _invoiceRemindersEnabled = MutableStateFlow(false)
    val invoiceRemindersEnabled: StateFlow<Boolean> = _invoiceRemindersEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(9)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(0)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    // Daily Summary States
    private val _dailySummaryEnabled = MutableStateFlow(false)
    val dailySummaryEnabled: StateFlow<Boolean> = _dailySummaryEnabled.asStateFlow()

    private val _summaryHour = MutableStateFlow(10)
    val summaryHour: StateFlow<Int> = _summaryHour.asStateFlow()

    private val _summaryMinute = MutableStateFlow(0)
    val summaryMinute: StateFlow<Int> = _summaryMinute.asStateFlow()

    companion object {
        private const val TAG = "SettingsScreenViewModel"
    }

    fun loadInvoiceReminderState(context: Context) {
        viewModelScope.launch {
            _invoiceRemindersEnabled.value = PreferenceManager.isInvoiceReminderEnabled(context)
            _reminderHour.value = PreferenceManager.getInvoiceReminderHour(context)
            _reminderMinute.value = PreferenceManager.getInvoiceReminderMinute(context)

            Log.d(TAG, "Loaded Invoice Reminder: enabled=${_invoiceRemindersEnabled.value}, time=${_reminderHour.value}:${_reminderMinute.value}")
        }
    }

    fun loadSummaryReminderState(context: Context) {
        viewModelScope.launch {
            _dailySummaryEnabled.value = PreferenceManager.isDailySummaryEnabled(context)
            _summaryHour.value = PreferenceManager.getSummaryReminderHour(context)
            _summaryMinute.value = PreferenceManager.getSummaryReminderMinute(context)

            Log.d(TAG, "Loaded Daily Summary: enabled=${_dailySummaryEnabled.value}, time=${_summaryHour.value}:${_summaryMinute.value}")
        }
    }

    fun toggleInvoiceReminder(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            _invoiceRemindersEnabled.value = enabled
            PreferenceManager.setInvoiceReminderEnabled(context, enabled)

            if (enabled) {
                Log.d(TAG, "Enabling Invoice Reminders at ${_reminderHour.value}:${_reminderMinute.value}")
                BackgroundTaskSchedular.scheduleInvoiceReminders(context)
            } else {
                Log.d(TAG, "Disabling Invoice Reminders")
                BackgroundTaskSchedular.cancelInvoiceReminders(context)
            }
        }
    }

    fun toggleDailySummary(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            _dailySummaryEnabled.value = enabled
            PreferenceManager.setDailySummaryEnabled(context, enabled)

            if (enabled) {
                Log.d(TAG, "Enabling Daily Summary at ${_summaryHour.value}:${_summaryMinute.value}")
                BackgroundTaskSchedular.scheduleDailySummary(context)
            } else {
                Log.d(TAG, "Disabling Daily Summary")
                BackgroundTaskSchedular.cancelDailySummary(context)
            }
        }
    }
    fun updateReminderTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            _reminderHour.value = hour
            _reminderMinute.value = minute
            PreferenceManager.setInvoiceReminderHour(context, hour)
            PreferenceManager.setInvoiceReminderMinute(context, minute)
            if (_invoiceRemindersEnabled.value) {
                Log.d(TAG, "Rescheduling Invoice Reminders with new time")
                BackgroundTaskSchedular.scheduleInvoiceReminders(context)
            }
        }
    }

    fun updateSummaryTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            _summaryHour.value = hour
            _summaryMinute.value = minute
            PreferenceManager.setSummaryReminderHour(context, hour)
            PreferenceManager.setSummaryReminderMinute(context, minute)
            if (_dailySummaryEnabled.value) {
                Log.d(TAG, "Rescheduling Daily Summary with new time")
                BackgroundTaskSchedular.scheduleDailySummary(context)
            }
        }
    }
}