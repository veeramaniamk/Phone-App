package com.veera.feature.home

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import com.veera.feature.dialpad.ui.DialpadScreen
import com.veera.feature.ongoing.ui.OngoingCallScreen

data class RecentCall(
    val id: String,
    val name: String,
    val number: String,
    val timestamp: String,
    val type: CallType,
    val isMissed: Boolean = false
)

enum class CallType {
    INCOMING, OUTGOING, MISSED
}

data class CallInfo(val name: String, val number: String)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    onCallClick: (RecentCall) -> Unit = {},
    onDialpadCall: (String) -> Unit = {}
) {
    var isDialpadVisible by remember { mutableStateOf(false) }
    var ongoingCall by remember { mutableStateOf<CallInfo?>(null) }

    DialerTheme(darkTheme = isDarkModeEnabled) {
        var visible by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            visible = true
        }

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive metrics
            val horizontalPadding = if (screenWidth > 600.dp) 32.dp else 20.dp
            val fabSize = if (screenHeight > 800.dp) 64.dp else 56.dp
            val titleSize = if (screenWidth > 400.dp) 32.sp else 28.sp
            val itemHeight = if (screenHeight > 800.dp) 88.dp else 72.dp
            val avatarSize = if (screenWidth > 400.dp) 56.dp else 48.dp

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(durationMillis = 700, easing = EaseOutBack)
                ) + fadeIn(animationSpec = tween(700)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            HomeHeader(
                                titleSize = titleSize,
                                padding = horizontalPadding
                            )
                        },
                        floatingActionButton = {
                            DialerFab(
                                size = fabSize,
                                onClick = { isDialpadVisible = true }
                            )
                        },
                        containerColor = Color.Transparent
                    ) { paddingValues ->
                        RecentCallsList(
                            modifier = Modifier.padding(paddingValues),
                            horizontalPadding = horizontalPadding,
                            itemHeight = itemHeight,
                            avatarSize = avatarSize,
                            onCallClick = { call ->
                                ongoingCall = CallInfo(call.name, call.number)
                                onCallClick(call)
                            }
                        )
                    }

                    // Overlay Dialpad
                    if (isDialpadVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clickable { isDialpadVisible = false }
                        ) {
                            DialpadScreen(
                                isDarkModeEnabled = isDarkModeEnabled,
                                onDismiss = { isDialpadVisible = false },
                                onCallClick = { number ->
                                    isDialpadVisible = false
                                    ongoingCall = CallInfo("Unknown", number)
                                    onDialpadCall(number)
                                }
                            )
                        }
                        
                        BackHandler {
                            isDialpadVisible = false
                        }
                    }

                    // Overlay Ongoing Call
                    AnimatedVisibility(
                        visible = ongoingCall != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ongoingCall?.let { info ->
                            OngoingCallScreen(
                                name = info.name,
                                number = info.number,
                                isDarkModeEnabled = isDarkModeEnabled,
                                onEndCall = { ongoingCall = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    titleSize: TextUnit,
    padding: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = padding, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Recents",
                style = AppTheme.typography.titleLarge.copy(
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Recent calls from your contacts",
                style = AppTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
        }
        
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            onClick = {}
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DialerFab(
    size: Dp,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(12.dp),
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppTheme.colors.ButtonGradientStart,
                            AppTheme.colors.ButtonGradientEnd
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Dialpad,
                contentDescription = "Open Dialer",
                tint = Color.White,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}

@Composable
private fun RecentCallsList(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp,
    itemHeight: Dp,
    avatarSize: Dp,
    onCallClick: (RecentCall) -> Unit
) {
    val dummyRecents = remember {
        listOf(
            RecentCall("1", "Alex Rivera", "+1 234 567 890", "10:30 AM", CallType.INCOMING),
            RecentCall("2", "John Doe", "+1 987 654 321", "Yesterday", CallType.MISSED, isMissed = true),
            RecentCall("3", "Sarah Wilson", "+1 555 019 283", "Monday", CallType.OUTGOING),
            RecentCall("4", "Michael Brown", "+1 444 222 111", "Monday", CallType.INCOMING),
            RecentCall("5", "Emily Davis", "+1 777 888 999", "Oct 12", CallType.OUTGOING),
            RecentCall("6", "Jessica Thompson", "+1 123 456 789", "Oct 11", CallType.MISSED, isMissed = true),
            RecentCall("7", "David Miller", "+1 321 654 987", "Oct 10", CallType.INCOMING),
            RecentCall("8", "Kevin Wilson", "+1 999 888 777", "Oct 09", CallType.OUTGOING)
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
    ) {
        items(dummyRecents, key = { it.id }) { call ->
            RecentCallItem(
                call = call,
                horizontalPadding = horizontalPadding,
                height = itemHeight,
                avatarSize = avatarSize,
                onClick = { onCallClick(call) }
            )
        }
    }
}

@Composable
private fun RecentCallItem(
    call: RecentCall,
    horizontalPadding: Dp,
    height: Dp,
    avatarSize: Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with initials
        Surface(
            modifier = Modifier.size(avatarSize),
            shape = CircleShape,
            color = if (call.isMissed) AppTheme.colors.Error.copy(alpha = 0.15f) 
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = call.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2),
                    style = AppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (avatarSize.value * 0.35).sp,
                        color = if (call.isMissed) AppTheme.colors.Error else MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name, Number and Call Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.name,
                maxLines = 1,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = if (call.isMissed) AppTheme.colors.Error else MaterialTheme.colorScheme.onBackground
                )
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = getCallTypeIcon(call.type),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (call.isMissed) AppTheme.colors.Error else AppTheme.colors.Tertiary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${call.type.name.lowercase().replaceFirstChar { it.uppercase() }} • ${call.timestamp}",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Action Button (Call Icon)
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call ${call.name}",
                tint = AppTheme.colors.Success,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getCallTypeIcon(type: CallType): ImageVector {
    return when (type) {
        CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
        CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
        CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
    }
}
