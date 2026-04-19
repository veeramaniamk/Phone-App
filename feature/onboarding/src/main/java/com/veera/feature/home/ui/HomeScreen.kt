package com.veera.feature.home.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
    var isSearchActive by remember { mutableStateOf(false) }

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
                            AnimatedContent(
                                targetState = isSearchActive,
                                transitionSpec = {
                                    if (targetState) {
                                        slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                    } else {
                                        slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                                    }
                                },
                                label = "TopBarTransition"
                            ) { active ->
                                if (active) {
                                    SearchHeader(
                                        query = viewModel.searchQuery.value,
                                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                                        onBackClick = { 
                                            isSearchActive = false
                                            viewModel.onSearchQueryChanged("")
                                        },
                                        padding = horizontalPadding
                                    )
                                } else {
                                    HomeHeader(
                                        titleSize = titleSize,
                                        padding = horizontalPadding,
                                        totalCount = viewModel.totalItemCount.value,
                                        currentPage = viewModel.currentPage.value,
                                        pageSize = viewModel.pageSize,
                                        onSearchClick = { isSearchActive = true }
                                    )
                                }
                            }
                        },
                        floatingActionButton = {
                            if (!isSearchActive) {
                                DialerFab(
                                    size = fabSize,
                                    onClick = { isDialpadVisible = true }
                                )
                            }
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

                    BackHandler(enabled = isSearchActive) {
                        isSearchActive = false
                        viewModel.onSearchQueryChanged("")
                    }

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
