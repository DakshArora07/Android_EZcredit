package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sfu.cmpt362.android_ezcredit.ui.theme.*
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CompanyProfileScreenViewModel

data class User(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val role: UserRole
)

enum class UserRole(val displayName: String) {
    ADMIN("Admin"),
    SALES("Sales"),
    RECEIPTS("Receipts")
}

@Composable
fun CompanyProfileScreen(
    viewModel: CompanyProfileScreenViewModel,
    onCancel: () -> Unit = {},
    onSave: () -> Unit = {},
    onAddUser: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val isValidPhone = state.phone.isEmpty() || state.phone.length == 10

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isVertical = maxWidth < 800.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteSmoke)
        ) {
            // Header
            CompanyProfileHeader()

            if (isVertical) {
                CompanyProfileContentVertical(
                    companyName = state.companyName,
                    address = state.address,
                    phone = state.phone,
                    users = state.users,
                    isValidPhone = isValidPhone,
                    showError = state.showError,
                    errorMessage = state.errorMessage,
                    isCompanyDetailsSaved = state.isCompanyDetailsSaved,
                    onCompanyNameChange = { viewModel.updateCompanyName(it) },
                    onAddressChange = { viewModel.updateAddress(it) },
                    onPhoneChange = { viewModel.updatePhone(it) },
                    onAddUser = onAddUser,
                    onRemoveUser = { user -> viewModel.removeUser(user.id) },
                    onCancelCompanyDetails = {
                        viewModel.resetCompanyDetails()
                        onCancel()
                    },
                    onSaveCompanyDetails = { viewModel.saveCompanyDetails() },
                    onGoToDashboard = { viewModel.goToDashboard(onSave) },
                    focusManager = focusManager
                )
            } else {
                CompanyProfileContentHorizontal(
                    companyName = state.companyName,
                    address = state.address,
                    phone = state.phone,
                    users = state.users,
                    isValidPhone = isValidPhone,
                    showError = state.showError,
                    errorMessage = state.errorMessage,
                    isCompanyDetailsSaved = state.isCompanyDetailsSaved,
                    onCompanyNameChange = { viewModel.updateCompanyName(it) },
                    onAddressChange = { viewModel.updateAddress(it) },
                    onPhoneChange = { viewModel.updatePhone(it) },
                    onAddUser = onAddUser,
                    onRemoveUser = { user -> viewModel.removeUser(user.id) },
                    onCancelCompanyDetails = {
                        viewModel.resetCompanyDetails()
                        onCancel()
                    },
                    onSaveCompanyDetails = { viewModel.saveCompanyDetails() },
                    onGoToDashboard = { viewModel.goToDashboard(onSave) },
                    focusManager = focusManager
                )
            }
        }
    }
}

@Composable
private fun CompanyProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "Create Company",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Set up your company profile",
            style = MaterialTheme.typography.bodyLarge,
            color = Grey
        )
    }
}

@Composable
private fun CompanyProfileContentVertical(
    companyName: String,
    address: String,
    phone: String,
    users: List<User>,
    isValidPhone: Boolean,
    showError: Boolean,
    errorMessage: String,
    isCompanyDetailsSaved: Boolean,
    onCompanyNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddUser: () -> Unit,
    onRemoveUser: (User) -> Unit,
    onCancelCompanyDetails: () -> Unit,
    onSaveCompanyDetails: () -> Unit,
    onGoToDashboard: () -> Unit,
    focusManager: FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Company Details Card
        CompanyDetailsCard(
            companyName = companyName,
            address = address,
            phone = phone,
            isValidPhone = isValidPhone,
            isLocked = isCompanyDetailsSaved,
            onCompanyNameChange = onCompanyNameChange,
            onAddressChange = onAddressChange,
            onPhoneChange = onPhoneChange,
            focusManager = focusManager
        )

        // Cancel/Save buttons for Company Details (only show if not saved yet)
        if (!isCompanyDetailsSaved) {
            if (showError) {
                ErrorMessage(errorMessage)
            }

            CompanyDetailsButtons(
                onCancel = onCancelCompanyDetails,
                onSave = onSaveCompanyDetails
            )
        }

        // Users section (only show after company details are saved)
        if (isCompanyDetailsSaved) {
            UsersCard(
                users = users,
                onAddUser = onAddUser,
                onRemoveUser = onRemoveUser
            )

            if (showError) {
                ErrorMessage(errorMessage)
            }

            // Go to Dashboard button
            DashboardButton(
                enabled = users.isNotEmpty(),
                onClick = onGoToDashboard
            )
        }
    }
}

@Composable
private fun CompanyProfileContentHorizontal(
    companyName: String,
    address: String,
    phone: String,
    users: List<User>,
    isValidPhone: Boolean,
    showError: Boolean,
    errorMessage: String,
    isCompanyDetailsSaved: Boolean,
    onCompanyNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddUser: () -> Unit,
    onRemoveUser: (User) -> Unit,
    onCancelCompanyDetails: () -> Unit,
    onSaveCompanyDetails: () -> Unit,
    onGoToDashboard: () -> Unit,
    focusManager: FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (!isCompanyDetailsSaved) {
            // Step 1: Company Details only
            CompanyDetailsCard(
                companyName = companyName,
                address = address,
                phone = phone,
                isValidPhone = isValidPhone,
                isLocked = isCompanyDetailsSaved,
                onCompanyNameChange = onCompanyNameChange,
                onAddressChange = onAddressChange,
                onPhoneChange = onPhoneChange,
                focusManager = focusManager
            )

            if (showError) {
                ErrorMessage(errorMessage)
            }

            CompanyDetailsButtons(
                onCancel = onCancelCompanyDetails,
                onSave = onSaveCompanyDetails
            )
        } else {
            // Step 2: Show both Company Details (locked) and Users side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CompanyDetailsCard(
                    companyName = companyName,
                    address = address,
                    phone = phone,
                    isValidPhone = isValidPhone,
                    isLocked = isCompanyDetailsSaved,
                    onCompanyNameChange = onCompanyNameChange,
                    onAddressChange = onAddressChange,
                    onPhoneChange = onPhoneChange,
                    focusManager = focusManager,
                    modifier = Modifier.weight(1f)
                )

                UsersCard(
                    users = users,
                    onAddUser = onAddUser,
                    onRemoveUser = onRemoveUser,
                    modifier = Modifier.weight(1f)
                )
            }

            if (showError) {
                ErrorMessage(errorMessage)
            }

            DashboardButton(
                enabled = users.isNotEmpty(),
                onClick = onGoToDashboard
            )
        }
    }
}

@Composable
private fun CompanyDetailsCard(
    companyName: String,
    address: String,
    phone: String,
    isValidPhone: Boolean,
    isLocked: Boolean,
    onCompanyNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) LightGray.copy(alpha = 0.3f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Company Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Grey,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Company Name
            OutlinedTextField(
                value = companyName,
                onValueChange = onCompanyNameChange,
                label = { Text("Company Name *") },
                leadingIcon = {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Grey)
                },
                singleLine = true,
                enabled = !isLocked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = LightGray,
                    disabledBorderColor = LightGray,
                    disabledTextColor = Color.Black
                )
            )

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Address *") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Grey)
                },
                minLines = 2,
                maxLines = 3,
                enabled = !isLocked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = LightGray,
                    disabledBorderColor = LightGray,
                    disabledTextColor = Color.Black
                )
            )

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Phone *") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Grey)
                },
                singleLine = true,
                enabled = !isLocked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                isError = phone.isNotEmpty() && !isValidPhone && !isLocked,
                supportingText = if (phone.isNotEmpty() && !isValidPhone && !isLocked) {
                    { Text("Phone number must be 10 digits", color = Red) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = LightGray,
                    disabledBorderColor = LightGray,
                    disabledTextColor = Color.Black
                )
            )
        }
    }
}

@Composable
private fun CompanyDetailsButtons(
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Grey
            )
        ) {
            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UsersCard(
    users: List<User>,
    onAddUser: () -> Unit,
    onRemoveUser: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Users *",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "(${users.size})",
                    fontSize = 14.sp,
                    color = Grey
                )
            }

            if (users.isEmpty()) {
                EmptyUsersPlaceholder(onAddUser)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    users.forEach { user ->
                        UserItem(
                            user = user,
                            onRemove = { onRemoveUser(user) }
                        )
                    }
                }
            }

            // Add User Button
            OutlinedButton(
                onClick = onAddUser,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add User")
            }

            if (users.isEmpty()) {
                Text(
                    text = "At least one Admin user is required",
                    fontSize = 12.sp,
                    color = Orange,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = LightGray
        )
    ) {
        Icon(
            imageVector = Icons.Default.Dashboard,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Go to Login",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyUsersPlaceholder(onAddUser: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, LightGray, RoundedCornerShape(12.dp))
            .clickable { onAddUser() }
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = Grey,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No users added yet",
                color = Grey,
                fontSize = 14.sp
            )
            Text(
                text = "Click + Add User to get started",
                color = Grey,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun UserItem(
    user: User,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteSmoke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = Grey
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Role Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (user.role) {
                        UserRole.ADMIN -> MaterialTheme.colorScheme.primaryContainer
                        UserRole.SALES -> MaterialTheme.colorScheme.secondaryContainer
                        UserRole.RECEIPTS -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                ) {
                    Text(
                        text = user.role.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (user.role) {
                            UserRole.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                            UserRole.SALES -> MaterialTheme.colorScheme.onSecondaryContainer
                            UserRole.RECEIPTS -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }

                // Remove Button
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove user",
                        tint = Red
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Red
            )
            Text(
                text = message,
                color = Red,
                fontSize = 14.sp
            )
        }
    }
}