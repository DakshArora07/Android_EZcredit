package sfu.cmpt362.android_ezcredit.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sfu.cmpt362.android_ezcredit.data.FirebaseAuthManager
import sfu.cmpt362.android_ezcredit.ui.theme.*
import sfu.cmpt362.android_ezcredit.ui.viewmodel.UserProfileScreenViewModel
import kotlinx.coroutines.withContext

@Composable
fun UserProfileScreen(
    existingUsers: List<User> = emptyList(),
    onCancel: () -> Unit = {},
    onSave: (User) -> Unit = {},
    isEditMode: Boolean = false,
    currentUserEmail: String? = null,
    modifier: Modifier = Modifier
) {
    val viewModel: UserProfileScreenViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val authManager = FirebaseAuthManager()

    var passwordVisible by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(!isEditMode) }

    // Load user data from Firebase when in edit mode
    LaunchedEffect(isEditMode, currentUserEmail) {
        if (isEditMode && currentUserEmail != null) {
            viewModel.loadUserData(currentUserEmail)
            viewModel.updatePassword("")
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
            UserProfileHeader(isEditMode = isEditMode)

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(if (isVertical) 16.dp else 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Main Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!isEditMode || isEditing) Color.White else LightGray.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "User Information",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                // Edit/Lock icon in edit mode
                                if (isEditMode) {
                                    IconButton(onClick = { isEditing = !isEditing }) {
                                        Icon(
                                            imageVector = if (isEditing) Icons.Default.Lock else Icons.Default.Edit,
                                            contentDescription = if (isEditing) "Lock" else "Edit",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Name Field
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = { viewModel.updateName(it) },
                                label = { Text("Name *") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Grey)
                                },
                                singleLine = true,
                                enabled = isEditing,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = LightGray,
                                    disabledBorderColor = LightGray,
                                    disabledTextColor = Color.Black
                                )
                            )

                            // Email Field - Editable in CREATE mode, Disabled in EDIT mode
                            OutlinedTextField(
                                value = state.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                label = { Text("Email *") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Grey)
                                },
                                singleLine = true,
                                enabled = !isEditMode, // Only enabled in create mode (isEditMode = false)
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                isError = !isEditMode && state.email.isNotEmpty() && !viewModel.isValidEmail(),
                                supportingText = if (!isEditMode && state.email.isNotEmpty() && !viewModel.isValidEmail()) {
                                    { Text("Invalid email address", color = Red) }
                                } else if (isEditMode) {
                                    { Text("Email cannot be changed", color = Grey, fontSize = 12.sp) }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = LightGray,
                                    disabledBorderColor = LightGray,
                                    disabledTextColor = Color.Black,
                                    disabledLabelColor = Grey,
                                    disabledLeadingIconColor = Grey
                                )
                            )

                            // Password Field
                            if (!isEditMode || isEditing) {
                                OutlinedTextField(
                                    value = state.password,
                                    onValueChange = { viewModel.updatePassword(it) },
                                    label = {
                                        Text(if (isEditMode) "New Password (optional)" else "Password *")
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Grey)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = Grey
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    enabled = isEditing,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    isError = !isEditMode && state.password.isNotEmpty() && state.password.length < 6,
                                    supportingText = if (!isEditMode && state.password.isNotEmpty() && state.password.length < 6) {
                                        { Text("Password must be at least 6 characters", color = Red) }
                                    } else if (isEditMode && isEditing) {
                                        { Text("Leave empty to keep current password", color = Grey, fontSize = 12.sp) }
                                    } else null,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = LightGray,
                                        disabledBorderColor = LightGray,
                                        disabledTextColor = Color.Black
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Access Level Section
                            if (!isEditMode) {
                                // CREATE MODE: Show role selector
                                Text(
                                    text = "Access Level *",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )

                                AccessLevelSelector(
                                    selectedRole = state.selectedRole,
                                    existingUsers = existingUsers,
                                    onRoleSelected = { viewModel.updateRole(it) }
                                )
                            } else {
                                // EDIT MODE: Show role as read-only
                                Text(
                                    text = "Access Level",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = LightGray.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (state.selectedRole) {
                                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                                UserRole.SALES -> Icons.Default.PointOfSale
                                                UserRole.RECEIPTS -> Icons.Default.Receipt
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Column {
                                            Text(
                                                text = state.selectedRole.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "Role cannot be changed",
                                                fontSize = 12.sp,
                                                color = Grey
                                            )
                                        }
                                    }
                                }
                            }

                            // Error Message
                            if (state.showError) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Red.copy(alpha = 0.1f)
                                    )
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
                                            text = state.errorMessage,
                                            color = Red,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Buttons
                            if (isEditMode) {
                                if (isEditing) {
                                    // Cancel and Save buttons when editing
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                // Reload data to cancel changes
                                                if (currentUserEmail != null) {
                                                    viewModel.loadUserData(currentUserEmail)
                                                    viewModel.updatePassword("")
                                                }
                                                isEditing = false
                                            },
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Grey
                                            )
                                        ) {
                                            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                if (state.name.isNotEmpty()) {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        // Update password if provided
                                                        if (state.password.isNotEmpty() && state.password.length >= 6) {
                                                            val success = authManager.changePassword(state.password)
                                                            if (!success) {
                                                                withContext(Dispatchers.Main) {
                                                                    viewModel.setError("Failed to update password")
                                                                }
                                                                return@launch
                                                            }
                                                        }

                                                        // Update user in Firebase
                                                        viewModel.updateUserInFirebase(state.email, state.name)

                                                        // Update user in local database
                                                        val updatedUser = User(
                                                            name = state.name,
                                                            email = state.email,
                                                            role = state.selectedRole
                                                        )

                                                        withContext(Dispatchers.Main) {
                                                            isEditing = false
                                                            onSave(updatedUser)
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = state.name.isNotEmpty() &&
                                                    (state.password.isEmpty() || state.password.length >= 6),
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = LightGray
                                            )
                                        ) {
                                            Text(
                                                text = "Save Changes",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    // Back button when not editing
                                    Button(
                                        onClick = onCancel,
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
                            } else {
                                // Create mode buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.clearState()
                                            onCancel()
                                        },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Grey
                                        )
                                    ) {
                                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.validateAndSave(existingUsers) { name, email, password, role ->
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val firebaseUser = authManager.createUser(email, password)
                                                    if (firebaseUser != null) {
                                                        val newUser = User(name = name, email = email, role = role)
                                                        withContext(Dispatchers.Main) {
                                                            viewModel.clearState()
                                                            onSave(newUser)
                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            viewModel.setError("Failed to create user account")
                                                        }
                                                        Log.d("Authentication", "User not created")
                                                    }
                                                }
                                            }
                                        },
                                        enabled = viewModel.canSave(existingUsers),
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            disabledContainerColor = LightGray
                                        )
                                    ) {
                                        Text(
                                            text = "Save",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeader(isEditMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isEditMode) Icons.Default.Person else Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = if (isEditMode) "User Profile" else "Add User",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isEditMode) "View and edit your account information" else "Create a new user account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Grey
                )
            }
        }
    }
}

@Composable
private fun AccessLevelSelector(
    selectedRole: UserRole,
    existingUsers: List<User>,
    onRoleSelected: (UserRole) -> Unit
) {
    val hasAdmin = existingUsers.any { it.role == UserRole.ADMIN }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Admin Role
        RoleCard(
            role = UserRole.ADMIN,
            isSelected = selectedRole == UserRole.ADMIN,
            isDisabled = hasAdmin,
            onSelect = { onRoleSelected(UserRole.ADMIN) },
            description = "Full access to all features"
        )

        // Sales Role
        RoleCard(
            role = UserRole.SALES,
            isSelected = selectedRole == UserRole.SALES,
            isDisabled = false,
            onSelect = { onRoleSelected(UserRole.SALES) },
            description = "Manage customers and invoices"
        )

        // Receipts Role
        RoleCard(
            role = UserRole.RECEIPTS,
            isSelected = selectedRole == UserRole.RECEIPTS,
            isDisabled = false,
            onSelect = { onRoleSelected(UserRole.RECEIPTS) },
            description = "Track payments and receipts"
        )

        if (hasAdmin && selectedRole == UserRole.ADMIN) {
            Text(
                text = "Only one Admin user is allowed",
                fontSize = 12.sp,
                color = Orange,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    isSelected: Boolean,
    isDisabled: Boolean,
    onSelect: () -> Unit,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDisabled -> LightGray.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> WhiteSmoke
            }
        ),
        onClick = { if (!isDisabled) onSelect() },
        enabled = !isDisabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
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
                    imageVector = when (role) {
                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                        UserRole.SALES -> Icons.Default.PointOfSale
                        UserRole.RECEIPTS -> Icons.Default.Receipt
                    },
                    contentDescription = null,
                    tint = when {
                        isDisabled -> Grey
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = role.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDisabled) Grey else Color.Black
                        )
                        if (isDisabled) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Disabled",
                                tint = Grey,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Grey
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = { if (!isDisabled) onSelect() },
                enabled = !isDisabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = Grey
                )
            )
        }
    }
}