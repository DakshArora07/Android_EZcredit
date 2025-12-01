package sfu.cmpt362.android_ezcredit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.utils.BackgroundTaskSchedular
import sfu.cmpt362.android_ezcredit.utils.PreferenceManager

class SettingsScreenViewModel : ViewModel() {
    private val _invoiceRemindersEnabled = MutableStateFlow(false)
    val invoiceRemindersEnabled: StateFlow<Boolean> = _invoiceRemindersEnabled.asStateFlow()
    private val _reminderHour = MutableStateFlow(9)
    val reminderHour = _reminderHour.asStateFlow()
    private val _summaryHout = MutableStateFlow(10)
    val summaryHour = _summaryHout.asStateFlow()
    private val _reminderMinute = MutableStateFlow(0)
    val reminderMinute = _reminderMinute.asStateFlow()
    private val _summaryMinute = MutableStateFlow(0)
    val summaryMinute = _summaryMinute.asStateFlow()

    fun loadInvoiceReminderState(context: Context) {
        viewModelScope.launch {
            _invoiceRemindersEnabled.value = PreferenceManager.isInvoiceReminderEnabled(context)
            _reminderHour.value = PreferenceManager.getInvoiceReminderHour(context)
            _reminderMinute.value = PreferenceManager.getInvoiceReminderMinute(context)
        }
    }

    fun loadSummaryReminderState(context: Context){
        viewModelScope.launch {
            _summaryHout.value = PreferenceManager.getSummaryReminderHour(context)
            _summaryMinute.value = PreferenceManager.getSummaryReminderMinute(context)
        }
    }

    fun toggleInvoiceReminder(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            _invoiceRemindersEnabled.value = enabled
            PreferenceManager.setInvoiceReminderEnabled(context, enabled)
            handleInvoiceWorkerToggle(context, enabled)
        }
    }

    fun updateReminderTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            _reminderHour.value = hour
            _reminderMinute.value = minute
            PreferenceManager.setInvoiceReminderHour(context, hour)
            PreferenceManager.setInvoiceReminderMinute(context, minute)

            if (invoiceRemindersEnabled.value) {
                BackgroundTaskSchedular.scheduleInvoiceReminders(context)
            }
        }
    }

    fun updateSummaryTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            _summaryHout.value = hour
            _summaryMinute.value = minute
            PreferenceManager.setSummaryReminderHour(context, hour)
            PreferenceManager.setSummaryReminderMinute(context, minute)
        }
    }

    private fun handleInvoiceWorkerToggle(context: Context, enabled: Boolean) {
        if (enabled) {
            BackgroundTaskSchedular.scheduleInvoiceReminders(context)
        } else {
            BackgroundTaskSchedular.cancelInvoiceReminders(context)
        }
    }

}