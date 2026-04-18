package com.veera.feature.ongoing.ui

import android.annotation.SuppressLint
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import kotlinx.coroutines.delay

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OngoingCallScreen(
    name: String,
    number: String,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    onEndCall: () -> Unit
) {
    var callDuration by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDuration++
        }
    }

    val minutes = (callDuration / 60).toString().padStart(2, '0')
    val seconds = (callDuration % 60).toString().padStart(2, '0')
    val durationText = "$minutes:$seconds"

    DialerTheme(darkTheme = isDarkModeEnabled) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkModeEnabled) {
                            listOf(Color(0xFF1A237E), Color(0xFF0D1B2A))
                        } else {
                            listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
                        }
                    )
                )
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive metrics
            val avatarSize = if (screenHeight > 800.dp) 160.dp else 120.dp
            val titleSize = if (screenWidth > 400.dp) 32.sp else 28.sp
            val controlBtnSize = if (screenHeight > 800.dp) 72.dp else 64.dp
            val endCallBtnSize = if (screenHeight > 800.dp) 80.dp else 72.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Profile Image / Initial
                Surface(
                    modifier = Modifier.size(avatarSize),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1).uppercase(),
                            style = AppTheme.typography.titleLarge.copy(
                                fontSize = (avatarSize.value * 0.4).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Contact Info
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
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Call Status/Duration
                Text(
                    text = durationText,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Control Buttons Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CallControlButton(Icons.Default.MicOff, "Mute", controlBtnSize)
                        CallControlButton(Icons.Default.Apps, "Keypad", controlBtnSize)
                        CallControlButton(Icons.Default.VolumeUp, "Speaker", controlBtnSize)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CallControlButton(Icons.Default.AddIcCall, "Add call", controlBtnSize)
                        CallControlButton(Icons.Default.VideoCall, "Video", controlBtnSize)
                        CallControlButton(Icons.Default.Bluetooth, "Bluetooth", controlBtnSize)
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                // End Call Button
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = Color(0xFFE63946),
                    modifier = Modifier
                        .size(endCallBtnSize)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    label: String,
    size: Dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f)
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = AppTheme.typography.labelMedium.copy(
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        )
    }
}
