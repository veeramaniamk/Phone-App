package com.veera.feature.contact_detail.ui

import android.annotation.SuppressLint
import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.veera.core.telephony.model.Contact
import com.veera.core.telephony.repository.CallLogEntry
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import com.veera.feature.contact.ui.ContactsViewModel
import android.content.Intent
import android.net.Uri

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ContactDetailScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onCallClick: (String) -> Unit = {},
    onMessageClick: (String) -> Unit = {},
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    viewModel: ContactsViewModel
) {
    val emails by viewModel.getEmails(contact.id).collectAsState(initial = emptyList())
    val recentLogs by viewModel.getCallHistory(contact.number).collectAsState(initial = emptyList())
    var showFullHistory by remember { mutableStateOf(false) }

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
                        QuickActionItem(Icons.Default.Call, "Call", actionIconSize, onClick = { onCallClick(contact.number) })
                        QuickActionItem(Icons.AutoMirrored.Filled.Message, "Text", actionIconSize, onClick = { onMessageClick(contact.number) })
                        QuickActionItem(Icons.Default.VideoCall, "Video", actionIconSize, onClick = { /* TODO */ })
                        QuickActionItem(Icons.Default.Email, "Email", actionIconSize, onClick = { /* TODO */ })
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Contact Details
                    DetailRow(
                        label = "Mobile",
                        value = contact.number,
                        icon = Icons.Default.Phone,
                        horizontalPadding = horizontalPadding,
                        labelSize = labelSize,
                        valueSize = valueSize
                    )
                    
                    if (emails.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        
                        emails.forEachIndexed { index, email ->
                            DetailRow(
                                label = if (index == 0) "Email" else "",
                                value = email,
                                icon = Icons.Default.Email,
                                horizontalPadding = horizontalPadding,
                                labelSize = labelSize,
                                valueSize = valueSize
                            )
                            if (index < emails.size - 1) Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Call History Section
                    if (recentLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        Text(
                            text = "Recent Interactions",
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        recentLogs.take(5).forEach { log ->
                            CallHistoryItem(
                                log = log,
                                horizontalPadding = horizontalPadding,
                                labelSize = labelSize,
                                valueSize = valueSize
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        TextButton(
                            onClick = { showFullHistory = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                        ) {
                            Text("View Full Call History")
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }

                if (showFullHistory) {
                    FullHistoryScreen(
                        contactName = contact.name,
                        phoneNumber = contact.number,
                        onBack = { showFullHistory = false },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    iconSize: Dp,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
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
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = AppTheme.typography.labelMedium.copy(
                        fontSize = labelSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
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

@Composable
private fun CallHistoryItem(
    log: CallLogEntry,
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
        val icon = when (log.type) {
            android.provider.CallLog.Calls.OUTGOING_TYPE -> Icons.AutoMirrored.Filled.CallMade
            android.provider.CallLog.Calls.MISSED_TYPE -> Icons.Default.CallMissed
            else -> Icons.AutoMirrored.Filled.CallReceived
        }
        val iconColor = if (log.type == android.provider.CallLog.Calls.MISSED_TYPE) Color.Red else MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconColor
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = DateUtils.getRelativeTimeSpanString(log.date, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                style = AppTheme.typography.labelMedium.copy(
                    fontSize = labelSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = if (log.duration > 0) "Duration: ${log.duration / 60}m ${log.duration % 60}s" else "No answer",
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = valueSize,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

@Composable
private fun FullHistoryScreen(
    contactName: String,
    phoneNumber: String,
    onBack: () -> Unit,
    viewModel: ContactsViewModel
) {
    val pagedHistory = viewModel.getCallHistoryPaged(phoneNumber).collectAsLazyPagingItems()
    
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Call History",
                        style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = contactName,
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    count = pagedHistory.itemCount,
                    key = { index -> pagedHistory[index]?.id ?: index }
                ) { index ->
                    val log = pagedHistory[index]
                    if (log != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (log.type) {
                                    android.provider.CallLog.Calls.OUTGOING_TYPE -> Icons.AutoMirrored.Filled.CallMade
                                    android.provider.CallLog.Calls.MISSED_TYPE -> Icons.Default.CallMissed
                                    else -> Icons.AutoMirrored.Filled.CallReceived
                                }
                                val iconColor = if (log.type == android.provider.CallLog.Calls.MISSED_TYPE) Color.Red else MaterialTheme.colorScheme.primary

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(iconColor.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconColor)
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = DateUtils.formatDateTime(LocalContext.current, log.date, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME),
                                        style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (log.duration > 0) "Duration: ${log.duration / 60}m ${log.duration % 60}s" else "Missed Call",
                                        style = AppTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = log.number,
                                        style = AppTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        BackHandler(onBack = onBack)
    }
}
