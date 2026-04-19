package com.veera.feature.incommingcall.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun IncomingCallScreen(
    name: String,
    number: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme()
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    DialerTheme(darkTheme = isDarkModeEnabled) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDarkModeEnabled) {
                                listOf(Color(0xFF121212), Color(0xFF1A1A1A), Color(0xFF0D1B2A))
                            } else {
                                listOf(Color(0xFF2196F3), Color(0xFF1976D2), Color(0xFF0D47A1))
                            }
                        )
                    )
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                
                // Responsive metrics
                val avatarSize = if (screenHeight > 800.dp) 140.dp else 100.dp
                val titleSize = if (screenWidth > 400.dp) 34.sp else 28.sp
                val actionBtnSize = if (screenHeight > 800.dp) 80.dp else 70.dp
                val spacing = if (screenHeight > 800.dp) 60.dp else 40.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(spacing))

                    // Incoming Label
                    Text(
                        text = "Incoming Call",
                        style = AppTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Light
                        )
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Profile Avatar
                    Surface(
                        modifier = Modifier.size(avatarSize),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = name.take(1).uppercase(),
                                style = AppTheme.typography.titleLarge.copy(
                                    fontSize = (avatarSize.value * 0.45).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Caller Identity
                    Text(
                        text = name,
                        style = AppTheme.typography.titleLarge.copy(
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    
                    Text(
                        text = number,
                        style = AppTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 80.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline Button
                        ActionCircle(
                            icon = Icons.Default.CallEnd,
                            label = "Decline",
                            color = Color(0xFFE63946),
                            size = actionBtnSize,
                            onClick = onDecline
                        )

                        // Accept Button
                        ActionCircle(
                            icon = Icons.Default.Call,
                            label = "Accept",
                            color = Color(0xFF2D6A4F),
                            size = actionBtnSize,
                            onClick = onAccept,
                            isPulse = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCircle(
    icon: ImageVector,
    label: String,
    color: Color,
    size: Dp,
    onClick: () -> Unit,
    isPulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scaleFactor by if (isPulse) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Scale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scaleFactor)
    ) {
        Surface(
            modifier = Modifier
                .size(size)
                .clickable { onClick() },
            shape = CircleShape,
            color = color,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            style = AppTheme.typography.labelMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        )
    }
}
