package com.veera.feature.home.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.hilt.navigation.compose.hiltViewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import com.veera.feature.dialpad.ui.DialpadScreen
import com.veera.feature.home.HomeViewModel
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
    onDialpadCall: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    var isDialpadVisible by remember { mutableStateOf(false) }
    var ongoingCall by remember { mutableStateOf<CallInfo?>(null) }

    val context = LocalContext.current
    var hasCallLogPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCallLogPermission = isGranted
        if (isGranted) {
            viewModel.loadNextPage()
        }
    }

    LaunchedEffect(hasCallLogPermission) {
        if (!hasCallLogPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

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
                            viewModel = viewModel,
                            hasPermission = hasCallLogPermission,
                            onPermissionRequest = {
                                permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
                            },
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
 fun PermissionRequiredView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Permission Required",
            style = AppTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "To show your call history, we need access to your call log. This is required for the app to function as your dialer.",
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Grant Permission",
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

