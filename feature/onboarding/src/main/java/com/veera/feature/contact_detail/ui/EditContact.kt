package com.veera.feature.contact_detail.ui

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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EditContactScreen(
    initialFirstName: String = "",
    initialLastName: String = "",
    initialPhoneNumber: String = "",
    initialEmail: String = "",
    initialSaveType: String = "Device",
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit, // firstName, lastName, phone, saveLocation
    onDelete: () -> Unit,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme()
) {
    DialerTheme(darkTheme = isDarkModeEnabled) {
        var firstName by remember { mutableStateOf(initialFirstName) }
        var lastName by remember { mutableStateOf(initialLastName) }
        var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
        var email by remember { mutableStateOf(initialEmail) }
        
        // Parse initial save type if it contains "Google: "
        val initialType = if (initialSaveType.startsWith("Google:")) "Google" else if (initialSaveType.isBlank()) "Device" else initialSaveType
        val initialGoogleEmail = if (initialSaveType.startsWith("Google:")) initialSaveType.substringAfter("Google:").trim() else ""
        
        var saveType by remember { mutableStateOf(initialType) }
        var googleEmail by remember { mutableStateOf(initialGoogleEmail) }
        
        var isVisible by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            isVisible = true
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(500, easing = LinearOutSlowInEasing)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                
                // Responsive sizing logic based on parent height and width
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
                            modifier = Modifier.clickable { 
                                isVisible = false
                                onDismiss() 
                            },
                            style = AppTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                fontSize = fontSize
                            )
                        )
                        Text(
                            text = "Edit Contact",
                            style = AppTheme.typography.titleLarge.copy(
                                fontSize = titleSize,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Save",
                            modifier = Modifier.clickable { 
                                if (firstName.isNotBlank() && phoneNumber.isNotBlank()) {
                                    val finalSaveType = if (saveType == "Google" && googleEmail.isNotBlank()) "Google: $googleEmail" else saveType
                                    onSave(firstName, lastName, phoneNumber, finalSaveType)
                                }
                            },
                            style = AppTheme.typography.bodyLarge.copy(
                                color = if (firstName.isNotBlank() && phoneNumber.isNotBlank()) 
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSize
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Profile Image Section
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.CenterHorizontally)
                            .clickable { /* Handle photo pick */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Photo",
                            modifier = Modifier.size(avatarSize / 2.5f),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Input Fields
                    EditContactInputField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First Name",
                        icon = Icons.Default.Person,
                        fontSize = fontSize
                    )
                    
                    Spacer(modifier = Modifier.height(fieldSpacing))
                    
                    EditContactInputField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last Name",
                        icon = Icons.Default.Person,
                        fontSize = fontSize
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    EditContactInputField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Phone Number",
                        icon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        fontSize = fontSize
                    )
                    
                    Spacer(modifier = Modifier.height(fieldSpacing))

                    EditContactInputField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        fontSize = fontSize
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                    
                    // Save Location Selection
                    EditSaveLocationSelector(
                        selectedType = saveType,
                        onTypeSelected = { saveType = it },
                        googleEmail = googleEmail,
                        onGoogleEmailChange = { googleEmail = it },
                        fontSize = fontSize
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Delete Contact Button
                    Button(
                        onClick = {
                            isVisible = false
                            onDelete()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Contact",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Contact",
                            style = AppTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSize
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
private fun EditSaveLocationSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    googleEmail: String,
    onGoogleEmailChange: (String) -> Unit,
    fontSize: TextUnit
) {
    val options = listOf(
        "Device" to Icons.Default.Smartphone,
        "SIM" to Icons.Default.SimCard,
        "Google" to Icons.Default.Cloud
    )
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = "Storage Location",
            style = AppTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = fontSize
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { (title, icon) ->
                val isSelected = selectedType == title
                Surface(
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = { onTypeSelected(title) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = AppTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = selectedType == "Google",
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                EditContactInputField(
                    value = googleEmail,
                    onValueChange = onGoogleEmailChange,
                    label = "Google Account Email",
                    icon = Icons.Default.AccountCircle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    fontSize = fontSize
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditContactInputField(
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
