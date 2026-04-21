package com.veera.feature.ongoing.ui

import android.annotation.SuppressLint
import android.telecom.Call
import androidx.compose.animation.*
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
    status: String,
    photoUri: String? = null,
    isMuted: Boolean = false,
    isSpeakerOn: Boolean = false,
    onMuteClick: () -> Unit = {},
    onSpeakerClick: () -> Unit = {},
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    connectTimeMillis: Long = 0L,
    onEndCall: () -> Unit
) {
    var callDuration by remember { mutableLongStateOf(0L) }
    val isCallActive = status == "Connected"
    
    LaunchedEffect(isCallActive, connectTimeMillis) {
        if (isCallActive) {
            while (true) {
                val now = System.currentTimeMillis()
                callDuration = if (connectTimeMillis > 0L) {
                    (now - connectTimeMillis) / 1000
                } else {
                    0L
                }
                delay(500)
            }
        } else {
            callDuration = 0L
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
                            listOf(Color(0xFF1A1A1A), Color(0xFF0D1B2A))
                        } else {
                            listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                        }
                    )
                )
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
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

                // Profile / Avatar
                Surface(
                    modifier = Modifier.size(avatarSize),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (photoUri != null) {
                            coil.compose.AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
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
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Identity & Status
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
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Connection Status / Timer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = status,
                        style = AppTheme.typography.bodyLarge.copy(
                            color = if (status == "Connected") Color(0xFF4CAF50) else Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    )
                    
                    if (isCallActive) {
                        Text(
                            text = durationText,
                            style = AppTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CallControlButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isMuted) "Unmute" else "Mute",
                            size = controlBtnSize,
                            isActive = isMuted,
                            onClick = onMuteClick
                        )
                        CallControlButton(Icons.Default.Apps, "Keypad", controlBtnSize)
                        CallControlButton(
                            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            label = "Speaker",
                            size = controlBtnSize,
                            isActive = isSpeakerOn,
                            onClick = onSpeakerClick
                        )
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

                // Action
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
    size: Dp,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(size)
                .clickable { onClick() },
            shape = CircleShape,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.Black else Color.White,
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
