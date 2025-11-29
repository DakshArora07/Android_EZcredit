package sfu.cmpt362.android_ezcredit.ui.screens

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
        InvoiceReminderSwitch(context,
            invoiceRemindersEnabled,
            settingsScreenViewModel)
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
private fun InvoiceReminderSwitch (context: Context, invoiceRemindersEnabled: Boolean, settingsScreenViewModel: SettingsScreenViewModel){
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("Invoice Reminders", style = MaterialTheme.typography.bodyLarge)

                Text(
                    if (invoiceRemindersEnabled)
                        "Disable automatic emails"
                    else
                        "Enable automatic emails",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = invoiceRemindersEnabled,
                onCheckedChange = { enabled ->
                    settingsScreenViewModel.toggleInvoiceReminder(context, enabled)
                }
            )
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