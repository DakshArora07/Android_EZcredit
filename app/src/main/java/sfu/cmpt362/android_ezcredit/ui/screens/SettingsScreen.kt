package sfu.cmpt362.android_ezcredit.ui.screens

import android.app.TimePickerDialog
import android.content.Context
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
import sfu.cmpt362.android_ezcredit.utils.PreferenceManager

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    settingsScreenViewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val invoiceRemindersEnabled by settingsScreenViewModel.invoiceRemindersEnabled.collectAsState()
    LaunchedEffect(Unit) {
        settingsScreenViewModel.loadInvoiceReminderState(context)
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
            hour = PreferenceManager.getInvoiceReminderHour(context),
            settingsScreenViewModel = settingsScreenViewModel
        )
        Spacer(modifier = Modifier.weight(1f))
        Logout(onLogout)
    }
}
@Composable
private fun ScreenTitle(){
    Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )
}
@Composable
private fun SectionTitles (title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
@Composable
private fun UserProfile(onProfileClick: ()-> Unit){
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
    settingsScreenViewModel: SettingsScreenViewModel
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        TimePickerDialog(
            context,
            { _, selectedHour, _ ->
                settingsScreenViewModel.updateReminderHour(context, selectedHour)
                showPicker = false
            },
            hour,
            0,
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
                            "${hour}:00",
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
private fun Logout(onLogout: () -> Unit){
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