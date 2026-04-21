package com.veera.feature.new_contact.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.veera.core.telephony.model.Contact
import com.veera.core.telephony.model.ContactAccount
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.filter

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun NewContactScreen(
    initialNumber: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    viewModel: NewContactViewModel = hiltViewModel(),
    isDarkModeEnabled: Boolean = isSystemInDarkTheme()
) {
    DialerTheme(darkTheme = isDarkModeEnabled) {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf(initialNumber) }
        var email by remember { mutableStateOf("") }
        var selectedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

        val accounts by viewModel.accounts.collectAsState()
        var selectedAccount by remember { mutableStateOf<ContactAccount?>(null) }

        val duplicateByNumber by viewModel.duplicateContactByNumber.collectAsState()
        val duplicateByName by viewModel.duplicateContactByName.collectAsState()
        val isSaving by viewModel.isSaving.collectAsState()

        var isVisible by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> selectedPhotoUri = uri }
        )

        LaunchedEffect(Unit) {
            isVisible = true
        }

        // Initialize default account
        LaunchedEffect(accounts) {
            if (selectedAccount == null && accounts.isNotEmpty()) {
                selectedAccount =
                    accounts.firstOrNull { it.type.contains("google", ignoreCase = true) }
                        ?: accounts.firstOrNull()
            }
        }

        // Check duplicates
        LaunchedEffect(phoneNumber) {
            viewModel.checkDuplicateNumber(phoneNumber)
        }

        LaunchedEffect(firstName, lastName) {
            viewModel.checkDuplicateName(firstName, lastName)
        }

        val isFormValid = firstName.isNotBlank() && phoneNumber.isNotBlank()

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(500, easing = LinearOutSlowInEasing)
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val screenWidth = maxWidth
                    val screenHeight = maxHeight

                    val horizontalPadding = if (screenWidth > 600.dp) 32.dp else 24.dp
                    val topPadding = if (screenHeight > 800.dp) 48.dp else 24.dp
                    val avatarSize = if (screenHeight > 800.dp) 110.dp else 80.dp
                    val fontSize = if (screenWidth > 400.dp) 16.sp else 14.sp
                    val titleSize = if (screenWidth > 400.dp) 24.sp else 20.sp
                    val fieldSpacing = if (screenHeight > 800.dp) 24.dp else 16.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding)
                    ) {
                        Spacer(modifier = Modifier.height(topPadding))

                        // Top Navigation Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cancel",
                                modifier = Modifier.clickable(enabled = !isSaving) {
                                    scope.launch {
                                        isVisible = false
                                        delay(400)
                                        onDismiss()
                                    }
                                },
                                style = AppTheme.typography.bodyLarge.copy(
                                    color = if (isSaving) MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.3f
                                    ) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = fontSize
                                )
                            )
                            Text(
                                text = "New Contact",
                                style = AppTheme.typography.titleLarge.copy(
                                    fontSize = titleSize,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Text(
                                text = "Save",
                                modifier = Modifier.clickable(enabled = isFormValid && !isSaving) {
                                    if (isFormValid && !isSaving) {
                                        viewModel.saveContact(
                                            firstName = firstName,
                                            lastName = lastName,
                                            phoneNumber = phoneNumber,
                                            email = email.ifBlank { null },
                                            photoUri = selectedPhotoUri,
                                            account = selectedAccount,
                                            onSuccess = {
                                                scope.launch {
                                                    isVisible = false
                                                    delay(400)
                                                    onDismiss()
                                                }
                                            }
                                        )
                                    }
                                },
                                style = AppTheme.typography.bodyLarge.copy(
                                    color = if (isFormValid && !isSaving)
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.3f
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = fontSize
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Profile Image Section
                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .align(Alignment.CenterHorizontally)
                                .clickable(enabled = !isSaving) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedPhotoUri != null) {
                                AsyncImage(
                                    model = selectedPhotoUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Add Photo",
                                    modifier = Modifier.size(avatarSize / 2.5f),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }



                        Spacer(modifier = Modifier.height(30.dp))

                        // Duplicate Name Warning
                        duplicateByName?.let { duplicate ->
                            DuplicateWarning(
                                message = "Name already exists",
                                contact = duplicate,
                                fontSize = fontSize
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Input Fields
                        ContactInputField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = "First Name",
                            icon = Icons.Default.Person,
                            fontSize = fontSize
                        )

                        Spacer(modifier = Modifier.height(fieldSpacing))

                        ContactInputField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = "Last Name",
                            icon = Icons.Default.Person,
                            fontSize = fontSize
                        )

                        Spacer(modifier = Modifier.height(fieldSpacing))

                        // Duplicate Number Warning
                        duplicateByNumber?.let { duplicate ->
                            DuplicateWarning(
                                message = "Number already exists",
                                contact = duplicate,
                                fontSize = fontSize
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        ContactInputField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = "Phone Number",
                            icon = Icons.Default.Phone,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            fontSize = fontSize
                        )

                        Spacer(modifier = Modifier.height(fieldSpacing))

                        ContactInputField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            icon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            fontSize = fontSize
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // Save Location Selection
                        SaveLocationSelector(
                            accounts = accounts,
                            selectedAccount = selectedAccount,
                            onAccountSelected = { selectedAccount = it },
                            fontSize = fontSize
                        )

                        Spacer(modifier = Modifier.height(50.dp))
                    }

                    // Loading Overlay
                    if (isSaving) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clickable(enabled = false) { },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp
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

    @Composable
    fun DuplicateWarning(
        message: String,
        contact: Contact,
        fontSize: TextUnit
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = message,
                        style = AppTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize
                        )
                    )
                    val location = when {
                        contact.accountType?.contains(
                            "google",
                            ignoreCase = true
                        ) == true -> "Email (${contact.accountName})"

                        contact.accountType?.contains(
                            "sim",
                            ignoreCase = true
                        ) == true -> "SIM Card"

                        else -> "Device Storage"
                    }
                    Text(
                        text = "Saved in: $location as ${contact.name}",
                        style = AppTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            fontSize = (fontSize.value * 0.85).sp
                        )
                    )
                }
            }
        }
    }

    @Composable
    fun SaveLocationSelector(
        accounts: List<ContactAccount>,
        selectedAccount: ContactAccount?,
        onAccountSelected: (ContactAccount) -> Unit,
        fontSize: TextUnit
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(
                text = "Save Contact To",
                style = AppTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Group accounts: Device, SIM, Emails
            val googleAccounts = accounts.filter { it.type.contains("google", ignoreCase = true) }
            val simAccounts = accounts.filter { it.type.contains("sim", ignoreCase = true) }
            val deviceAccounts = accounts.filter {
                !it.type.contains(
                    "google",
                    ignoreCase = true
                ) && !it.type.contains("sim", ignoreCase = true)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Device Option
                val deviceAccount =
                    deviceAccounts.firstOrNull() ?: ContactAccount("Device", "local")
                AccountItem(
                    account = deviceAccount,
                    label = "Phone Storage",
                    icon = Icons.Default.Smartphone,
                    isSelected = selectedAccount?.type == deviceAccount.type && selectedAccount?.name == deviceAccount.name,
                    onClick = { onAccountSelected(deviceAccount) },
                    fontSize = fontSize
                )

                // SIM Option
                if (simAccounts.isNotEmpty()) {
                    simAccounts.forEachIndexed { index, acc ->
                        AccountItem(
                            account = acc,
                            label = "SIM Card ${if (simAccounts.size > 1) index + 1 else ""}",
                            icon = Icons.Default.SimCard,
                            isSelected = selectedAccount == acc,
                            onClick = { onAccountSelected(acc) },
                            fontSize = fontSize
                        )
                    }
                } else {
                    // Fallback if no SIM account found but user might want it (usually it should be in accounts)
                    AccountItem(
                        account = ContactAccount("SIM", "sim"),
                        label = "SIM Card",
                        icon = Icons.Default.SimCard,
                        isSelected = selectedAccount?.type == "sim",
                        onClick = { onAccountSelected(ContactAccount("SIM", "sim")) },
                        fontSize = fontSize
                    )
                }

                // Google/Email Options
                googleAccounts.forEach { acc ->
                    AccountItem(
                        account = acc,
                        label = acc.name,
                        icon = Icons.Default.Email,
                        isSelected = selectedAccount == acc,
                        onClick = { onAccountSelected(acc) },
                        fontSize = fontSize
                    )
                }
            }
        }
    }

    @Composable
    fun AccountItem(
        account: ContactAccount,
        label: String,
        icon: ImageVector,
        isSelected: Boolean,
        onClick: () -> Unit,
        fontSize: TextUnit
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            ),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            ) else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontSize = fontSize
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ContactInputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        icon: ImageVector,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        fontSize: TextUnit
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, fontSize = fontSize) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            },
            keyboardOptions = keyboardOptions,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = AppTheme.typography.bodyLarge.copy(
                fontSize = fontSize,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }

