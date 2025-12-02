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
    isViewMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var isEditing by remember { mutableStateOf(!isViewMode) }

    val isValidPhone = state.phone.isEmpty() || state.phone.length == 10

    // Load company data in view mode
    LaunchedEffect(isViewMode) {
        if (isViewMode) {
            viewModel.loadCompanyData()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isVertical = maxWidth < 800.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteSmoke)
        ) {
            // Header
            CompanyProfileHeader(isViewMode = isViewMode)

            if (isViewMode) {
                // View Mode: Show company details and users
                CompanyProfileViewMode(
                    companyName = state.companyName,
                    address = state.address,
                    phone = state.phone,
                    users = state.users,
                    isValidPhone = isValidPhone,
                    showError = state.showError,
                    errorMessage = state.errorMessage,
                    isEditing = isEditing,
                    onToggleEdit = { isEditing = !isEditing },
                    onAddressChange = { viewModel.updateAddress(it) },
                    onPhoneChange = { viewModel.updatePhone(it) },
                    onAddUser = onAddUser,
                    onRemoveUser = { user -> viewModel.removeUser(user.id) },
                    onChangeUserRole = { user, newRole -> viewModel.changeUserRole(user.id, newRole) },
                    onCancel = {
                        viewModel.loadCompanyData()
                        isEditing = false
                        onCancel()
                    },
                    onSave = {
                        viewModel.saveCompanyDetailsInViewMode()
                        isEditing = false
                    },
                    onBack = onCancel,
                    focusManager = focusManager,
                    isVertical = isVertical
                )
            } else {
                // Create Mode: Original flow
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
}

@Composable
private fun CompanyProfileHeader(isViewMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = if (isViewMode) "Company Profile" else "Create Company",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isViewMode) "View and manage your company information" else "Set up your company profile",
            style = MaterialTheme.typography.bodyLarge,
            color = Grey
        )
    }
}

@Composable
private fun CompanyProfileViewMode(
    companyName: String,
    address: String,
    phone: String,
    users: List<User>,
    isValidPhone: Boolean,
    showError: Boolean,
    errorMessage: String,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddUser: () -> Unit,
    onRemoveUser: (User) -> Unit,
    onChangeUserRole: (User, UserRole) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    focusManager: FocusManager,
    isVertical: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(if (isVertical) 16.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (isVertical) {
            // Vertical layout
            CompanyDetailsCardViewMode(
                companyName = companyName,
                address = address,
                phone = phone,
                isValidPhone = isValidPhone,
                isEditing = isEditing,
                onToggleEdit = onToggleEdit,
                onAddressChange = onAddressChange,
                onPhoneChange = onPhoneChange,
                focusManager = focusManager
            )

            UsersCardViewMode(
                users = users,
                isEditing = isEditing,
                onAddUser = onAddUser,
                onRemoveUser = onRemoveUser,
                onChangeUserRole = onChangeUserRole
            )

            if (showError) {
                ErrorMessage(errorMessage)
            }

            // Buttons
            if (isEditing) {
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
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to App", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Horizontal layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CompanyDetailsCardViewMode(
                    companyName = companyName,
                    address = address,
                    phone = phone,
                    isValidPhone = isValidPhone,
                    isEditing = isEditing,
                    onToggleEdit = onToggleEdit,
                    onAddressChange = onAddressChange,
                    onPhoneChange = onPhoneChange,
                    focusManager = focusManager,
                    modifier = Modifier.weight(1f)
                )

                UsersCardViewMode(
                    users = users,
                    isEditing = isEditing,
                    onAddUser = onAddUser,
                    onRemoveUser = onRemoveUser,
                    onChangeUserRole = onChangeUserRole,
                    modifier = Modifier.weight(1f)
                )
            }

            if (showError) {
                ErrorMessage(errorMessage)
            }

            // Buttons
            if (isEditing) {
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
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CompanyDetailsCardViewMode(
    companyName: String,
    address: String,
    phone: String,
    isValidPhone: Boolean,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isEditing) LightGray.copy(alpha = 0.3f) else Color.White
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

                IconButton(onClick = onToggleEdit) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Lock else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Lock" else "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Company Name - ALWAYS DISABLED (cannot be edited)
            OutlinedTextField(
                value = companyName,
                onValueChange = {},
                label = { Text("Company Name *") },
                leadingIcon = {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Grey)
                },
                singleLine = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = LightGray,
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Grey,
                    disabledLeadingIconColor = Grey
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
                enabled = isEditing,
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
                enabled = isEditing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                isError = phone.isNotEmpty() && !isValidPhone && isEditing,
                supportingText = if (phone.isNotEmpty() && !isValidPhone && isEditing) {
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
private fun UsersCardViewMode(
    users: List<User>,
    isEditing: Boolean,
    onAddUser: () -> Unit,
    onRemoveUser: (User) -> Unit,
    onChangeUserRole: (User, UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isEditing) LightGray.copy(alpha = 0.3f) else Color.White
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
                    text = "Users",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "(${users.size})",
                        fontSize = 14.sp,
                        color = Grey
                    )
                    if (!isEditing) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Grey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No users found",
                        color = Grey,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    users.forEach { user ->
                        UserItemViewModeEditable(
                            user = user,
                            isEditing = isEditing,
                            onRemove = { onRemoveUser(user) },
                            onChangeRole = { newRole -> onChangeUserRole(user, newRole) }
                        )
                    }
                }
            }

            // Add User Button (only when editing)
            if (isEditing) {
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
            } else {
                Text(
                    text = "Click Edit to manage users and roles",
                    fontSize = 12.sp,
                    color = Grey,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun UserItemViewModeEditable(
    user: User,
    isEditing: Boolean,
    onRemove: () -> Unit,
    onChangeRole: (UserRole) -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

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
                // Role Badge - clickable when editing
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (user.role) {
                            UserRole.ADMIN -> MaterialTheme.colorScheme.primaryContainer
                            UserRole.SALES -> MaterialTheme.colorScheme.secondaryContainer
                            UserRole.RECEIPTS -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        modifier = if (isEditing) Modifier.clickable { showRoleMenu = true } else Modifier
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user.role.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (user.role) {
                                    UserRole.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                                    UserRole.SALES -> MaterialTheme.colorScheme.onSecondaryContainer
                                    UserRole.RECEIPTS -> MaterialTheme.colorScheme.onTertiaryContainer
                                }
                            )
                            if (isEditing) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change role",
                                    modifier = Modifier.size(16.dp),
                                    tint = when (user.role) {
                                        UserRole.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                                        UserRole.SALES -> MaterialTheme.colorScheme.onSecondaryContainer
                                        UserRole.RECEIPTS -> MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )
                            }
                        }
                    }

                    // Role selection dropdown
                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.displayName) },
                                onClick = {
                                    onChangeRole(role)
                                    showRoleMenu = false
                                },
                                leadingIcon = {
                                    if (user.role == role) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Remove Button (only when editing)
                if (isEditing) {
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

        if (!isCompanyDetailsSaved) {
            if (showError) {
                ErrorMessage(errorMessage)
            }

            CompanyDetailsButtons(
                onCancel = onCancelCompanyDetails,
                onSave = onSaveCompanyDetails
            )
        }

        if (isCompanyDetailsSaved) {
            UsersCard(
                users = users,
                onAddUser = onAddUser,
                onRemoveUser = onRemoveUser
            )

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