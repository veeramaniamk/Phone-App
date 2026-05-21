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
import android.telecom.CallAudioState
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
    isRecording: Boolean = false,
    recordingStartTimeMillis: Long = 0L,
    supportedAudioRoutes: Int = CallAudioState.ROUTE_EARPIECE or CallAudioState.ROUTE_SPEAKER,
    currentAudioRoute: Int = CallAudioState.ROUTE_EARPIECE,
    onMuteClick: () -> Unit = {},
    onSpeakerClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onAudioRouteSelect: (Int) -> Unit = {},
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    connectTimeMillis: Long = 0L,
    onEndCall: () -> Unit
) {
    var callDuration by remember { mutableLongStateOf(0L) }
    var showAudioRoutePicker by remember { mutableStateOf(false) }
    val isCallActive = status == "Connected"
    
    var recordingDuration by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(isRecording, recordingStartTimeMillis) {
        if (isRecording && recordingStartTimeMillis > 0L) {
            while (true) {
                recordingDuration = (System.currentTimeMillis() - recordingStartTimeMillis) / 1000
                delay(500)
            }
        } else {
            recordingDuration = 0L
        }
    }
    
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
                    
                    if (isRecording && recordingStartTimeMillis > 0L) {
                        val rMinutes = (recordingDuration / 60).toString().padStart(2, '0')
                        val rSeconds = (recordingDuration % 60).toString().padStart(2, '0')
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Recording",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$rMinutes:$rSeconds",
                                style = AppTheme.typography.bodyMedium.copy(
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
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
                            icon = when (currentAudioRoute) {
                                CallAudioState.ROUTE_BLUETOOTH -> Icons.Default.Bluetooth
                                CallAudioState.ROUTE_SPEAKER -> Icons.Default.VolumeUp
                                CallAudioState.ROUTE_WIRED_HEADSET -> Icons.Default.Headset
                                else -> Icons.Default.VolumeDown
                            },
                            label = "Audio",
                            size = controlBtnSize,
                            isActive = currentAudioRoute != CallAudioState.ROUTE_EARPIECE,
                            onClick = {
                                // If bluetooth or wired headset is supported, show picker
                                if ((supportedAudioRoutes and CallAudioState.ROUTE_BLUETOOTH) != 0 || 
                                    (supportedAudioRoutes and CallAudioState.ROUTE_WIRED_HEADSET) != 0) {
                                    showAudioRoutePicker = true
                                } else {
                                    onSpeakerClick()
                                }
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CallControlButton(Icons.Default.AddIcCall, "Add call", controlBtnSize)
                        CallControlButton(
                            icon = Icons.Default.FiberManualRecord, 
                            label = "Record", 
                            size = controlBtnSize,
                            isActive = isRecording,
                            onClick = onRecordClick
                        )
                        CallControlButton(Icons.Default.VideoCall, "Video", controlBtnSize)
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
            
            if (showAudioRoutePicker) {
                AlertDialog(
                    onDismissRequest = { showAudioRoutePicker = false },
                    title = { Text("Select Audio Route") },
                    text = {
                        Column {
                            if ((supportedAudioRoutes and CallAudioState.ROUTE_EARPIECE) != 0) {
                                TextButton(onClick = { 
                                    onAudioRouteSelect(CallAudioState.ROUTE_EARPIECE)
                                    showAudioRoutePicker = false
                                }) { Text("Earpiece") }
                            }
                            if ((supportedAudioRoutes and CallAudioState.ROUTE_SPEAKER) != 0) {
                                TextButton(onClick = { 
                                    onAudioRouteSelect(CallAudioState.ROUTE_SPEAKER)
                                    showAudioRoutePicker = false
                                }) { Text("Speaker") }
                            }
                            if ((supportedAudioRoutes and CallAudioState.ROUTE_BLUETOOTH) != 0) {
                                TextButton(onClick = { 
                                    onAudioRouteSelect(CallAudioState.ROUTE_BLUETOOTH)
                                    showAudioRoutePicker = false
                                }) { Text("Bluetooth") }
                            }
                            if ((supportedAudioRoutes and CallAudioState.ROUTE_WIRED_HEADSET) != 0) {
                                TextButton(onClick = { 
                                    onAudioRouteSelect(CallAudioState.ROUTE_WIRED_HEADSET)
                                    showAudioRoutePicker = false
                                }) { Text("Wired Headset") }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAudioRoutePicker = false }) {
                            Text("Cancel")
                        }
                    }
                )
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
