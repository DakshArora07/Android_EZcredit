package sfu.cmpt362.android_ezcredit.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.SettingsScreenViewModel

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    settingsScreenViewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val dailySummaryEnabled by settingsScreenViewModel.dailySummaryEnabled.collectAsState()
    val invoiceRemindersEnabled by settingsScreenViewModel.invoiceRemindersEnabled.collectAsState()

    // Permission launcher for notifications (Android 13+)
    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Optionally show a snackbar or dialog explaining that notifications won't work
        }
    }

    // Load settings on first composition
    LaunchedEffect(Unit) {
        settingsScreenViewModel.loadInvoiceReminderState(context)
        settingsScreenViewModel.loadSummaryReminderState(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        ScreenTitle()

        SectionTitles("Account")
        UserProfile(onProfileClick)

        SectionTitles("Background Tasks")
        ReminderSettingsCard(
            context = context,
            enabled = invoiceRemindersEnabled,
            hour = settingsScreenViewModel.reminderHour.collectAsState().value,
            minute = settingsScreenViewModel.reminderMinute.collectAsState().value,
            settingsScreenViewModel = settingsScreenViewModel
        )

        SummarySettingsCard(
            context = context,
            enabled = dailySummaryEnabled,
            hour = settingsScreenViewModel.summaryHour.collectAsState().value,
            minute = settingsScreenViewModel.summaryMinute.collectAsState().value,
            settingsScreenViewModel = settingsScreenViewModel,
            requestNotificationPermissionLauncher = requestNotificationPermissionLauncher
        )

        Spacer(modifier = Modifier.weight(1f))
        Logout(onLogout)
    }
}

@Composable
private fun ScreenTitle() {
    Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SectionTitles(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun UserProfile(onProfileClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onProfileClick() }
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("User Profile", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Manage your personal information",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReminderSettingsCard(
    context: Context,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    settingsScreenViewModel: SettingsScreenViewModel
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                settingsScreenViewModel.updateReminderTime(context, selectedHour, selectedMinute)
                showPicker = false
            },
            hour,
            minute,
            false
        ).apply {
            setOnDismissListener { showPicker = false }
            show()
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Invoice Reminders", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (enabled)
                            "Automatic reminder emails are ON"
                        else
                            "Enable automatic reminder emails",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = { on ->
                        settingsScreenViewModel.toggleInvoiceReminder(context, on)
                    }
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reminder Time", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            String.format("%02d:%02d", hour, minute),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
fun SummarySettingsCard(
    context: Context,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    settingsScreenViewModel: SettingsScreenViewModel,
    requestNotificationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                // FIXED: Now calls updateSummaryTime instead of updateReminderTime
                settingsScreenViewModel.updateSummaryTime(context, selectedHour, selectedMinute)
                showPicker = false
            },
            hour,
            minute,
            false
        ).apply {
            setOnDismissListener { showPicker = false }
            show()
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Summary", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (enabled)
                            "Daily Summary Notification is ON"
                        else
                            "Enable Daily Summary Notification",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = { on ->
                        // FIXED: Request permission only when turning ON and on Android 13+
                        if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }

                        // Toggle happens in ViewModel - scheduling happens only once
                        settingsScreenViewModel.toggleDailySummary(context, on)
                    }
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notification Time", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            String.format("%02d:%02d", hour, minute),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun Logout(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Logout")
    }
}