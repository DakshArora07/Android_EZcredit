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

    fun loadInvoiceReminderState(context: Context) {
        viewModelScope.launch {
            _invoiceRemindersEnabled.value = PreferenceManager.isInvoiceReminderEnabled(context)
        }
    }

    fun toggleInvoiceReminder(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            _invoiceRemindersEnabled.value = enabled
            PreferenceManager.setInvoiceReminderEnabled(context, enabled)
            handleInvoiceWorkerToggle(context, enabled)
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