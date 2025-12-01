package sfu.cmpt362.android_ezcredit.ui.screens

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
import sfu.cmpt362.android_ezcredit.ui.theme.*
import sfu.cmpt362.android_ezcredit.ui.viewmodel.UserProfileScreenViewModel

@Composable
fun UserProfileScreen(
    existingUsers: List<User>,
    onCancel: () -> Unit = {},
    onSave: (User) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: UserProfileScreenViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var passwordVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isVertical = maxWidth < 800.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteSmoke)
        ) {
            // Header
            UserProfileHeader()

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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "User Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Name Field
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Name *") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Grey)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = LightGray
                            )
                        )

                        // Email Field
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Email *") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Grey)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            isError = state.email.isNotEmpty() && !viewModel.isValidEmail(),
                            supportingText = if (state.email.isNotEmpty() && !viewModel.isValidEmail()) {
                                { Text("Invalid email address", color = Red) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = LightGray
                            )
                        )

                        // Password Field
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.updatePassword(it) },
                            label = { Text("Password *") },
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            isError = state.password.isNotEmpty() && state.password.length < 6,
                            supportingText = if (state.password.isNotEmpty() && state.password.length < 6) {
                                { Text("Password must be at least 6 characters", color = Red) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Access Level Section
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
                                        val newUser = User(
                                            name = name,
                                            email = email,
                                            role = role
                                        )
                                        viewModel.clearState()
                                        onSave(newUser)
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
                                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeader() {
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
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Add User",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Create a new user account",
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
                text = "⚠️ Only one Admin user is allowed",
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