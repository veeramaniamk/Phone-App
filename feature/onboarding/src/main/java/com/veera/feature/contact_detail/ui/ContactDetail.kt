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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import com.veera.feature.contact.ui.ContactEntry

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ContactDetailScreen(
    contact: ContactEntry,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    isDarkModeEnabled: Boolean = isSystemInDarkTheme()
) {
    DialerTheme(darkTheme = isDarkModeEnabled) {
        var isVisible by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            isVisible = true
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                
                // Responsive metrics
                val horizontalPadding = if (screenWidth > 600.dp) 32.dp else 24.dp
                val avatarSize = if (screenHeight > 800.dp) 120.dp else 90.dp
                val nameSize = if (screenWidth > 400.dp) 28.sp else 24.sp
                val actionIconSize = if (screenWidth > 400.dp) 28.dp else 24.dp
                val labelSize = if (screenWidth > 400.dp) 14.sp else 12.sp
                val valueSize = if (screenWidth > 400.dp) 18.sp else 16.sp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = horizontalPadding, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            isVisible = false
                            onBackClick()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        
                        Text(
                            text = "Edit",
                            modifier = Modifier.clickable { onEditClick() },
                            style = AppTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Profile Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(avatarSize),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = contact.initial,
                                    style = AppTheme.typography.titleLarge.copy(
                                        fontSize = (avatarSize.value * 0.4).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = contact.name,
                            style = AppTheme.typography.titleLarge.copy(
                                fontSize = nameSize,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quick Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionItem(Icons.Default.Call, "Call", actionIconSize)
                        QuickActionItem(Icons.AutoMirrored.Filled.Message, "Text", actionIconSize)
                        QuickActionItem(Icons.Default.VideoCall, "Video", actionIconSize)
                        QuickActionItem(Icons.Default.Email, "Email", actionIconSize)
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Contact Details List
                    DetailRow(
                        label = "Mobile",
                        value = contact.number,
                        icon = Icons.Default.Phone,
                        horizontalPadding = horizontalPadding,
                        labelSize = labelSize,
                        valueSize = valueSize
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                    
                    DetailRow(
                        label = "Email",
                        value = "${contact.name.lowercase().replace(" ", ".")}@example.com",
                        icon = Icons.Default.Email,
                        horizontalPadding = horizontalPadding,
                        labelSize = labelSize,
                        valueSize = valueSize
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    iconSize: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Surface(
            modifier = Modifier.size(iconSize + 24.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = AppTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    horizontalPadding: Dp,
    labelSize: TextUnit,
    valueSize: TextUnit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = label,
                style = AppTheme.typography.labelMedium.copy(
                    fontSize = labelSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = valueSize,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}
