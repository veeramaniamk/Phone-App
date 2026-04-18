package com.veera.feature.onboarding.ui

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DefaultDialerScreen(
    viewModel: DefaultDialerViewModel = hiltViewModel(),
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    onComplete: () -> Unit,
    onExit: () -> Unit
) {
    val isDefault by viewModel.isDefaultDialer.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkStatus()
    }

    LaunchedEffect(isDefault) {
        if (isDefault) {
            onComplete()
        }
    }

    DialerTheme(darkTheme = isDarkModeEnabled) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive metrics
            val contentPadding = if (screenWidth > 400.dp) 32.dp else 24.dp
            val illustrationWeight = if (screenHeight > 700.dp) 1.2f else 1f
            val titleSize = if (screenWidth > 400.dp) 28.sp else 24.sp
            val buttonHeight = if (screenHeight > 800.dp) 64.dp else 56.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Illustration Section
                Box(modifier = Modifier.weight(illustrationWeight), contentAlignment = Alignment.Center) {
                    PhoneIllustration(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Text Content
                Text(
                    text = "Set as Default Dialer",
                    style = AppTheme.typography.titleLarge.copy(
                        fontSize = titleSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To make and manage calls, please set\nthis app as your default dialer",
                    style = AppTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )

                Spacer(modifier = Modifier.height(if (screenHeight > 700.dp) 48.dp else 32.dp))

                // Buttons
                Button(
                    onClick = {
                        val intent = viewModel.getRequestIntent()
                        if (intent != null) {
                            launcher.launch(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    shape = RoundedCornerShape(buttonHeight / 2),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        AppTheme.colors.ButtonGradientStart,
                                        AppTheme.colors.ButtonGradientEnd
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Set Default Dialer",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onExit,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "Exit App",
                        style = AppTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.TextSecondary,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneIllustration(
    screenWidth: Dp,
    screenHeight: Dp
) {
    val scale = if (screenHeight < 700.dp) 0.8f else 1f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background card effect
        Surface(
            modifier = Modifier
                .width(260.dp * scale)
                .height(380.dp * scale),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            shadowElevation = 4.dp
        ) {}

        // Phone Frame
        Box(
            modifier = Modifier
                .width(230.dp * scale)
                .height(400.dp * scale)
                .clip(RoundedCornerShape(32.dp))
                .background(if (isSystemInDarkTheme()) Color(0xFF0A0A1A) else Color(0xFF1B1B2F))
                .padding(6.dp * scale)
        ) {
            // Screen Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(26.dp * scale))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp * scale, vertical = 8.dp * scale),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("9:41", color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp * scale, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp * scale)) {
                        Icon(Icons.Default.SignalCellular4Bar, null, Modifier.size(10.dp * scale), tint = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.Wifi, null, Modifier.size(10.dp * scale), tint = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.BatteryFull, null, Modifier.size(10.dp * scale), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp * scale))

                // Profile
                Surface(
                    modifier = Modifier.size(72.dp * scale),
                    shape = CircleShape,
                    color = AppTheme.colors.ButtonGradientEnd
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.padding(14.dp * scale),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp * scale))

                Text("Alex Rivera", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp * scale)
                Text("Calling...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp * scale)

                Spacer(modifier = Modifier.weight(1f))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp * scale),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButton(Icons.Default.MicOff, "MUTE", scale)
                    ControlButton(Icons.Default.Apps, "KEYPAD", scale)
                    ControlButton(Icons.Default.VolumeUp, "SPEAKER", scale)
                }

                Spacer(modifier = Modifier.height(20.dp * scale))

                // Decline Button
                Surface(
                    modifier = Modifier.size(52.dp * scale),
                    shape = CircleShape,
                    color = AppTheme.colors.Error
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        null,
                        modifier = Modifier.padding(10.dp * scale),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp * scale))

                // Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(10.dp * scale),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(Icons.Default.Star, null, tint = AppTheme.colors.Primary, modifier = Modifier.size(20.dp * scale))
                    Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(20.dp * scale))
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(20.dp * scale))
                }
            }
        }

        // Floating Call-out Icon
        Surface(
            modifier = Modifier
                .offset(x = 100.dp * scale, y = (-20).dp * scale)
                .size(56.dp * scale),
            shape = CircleShape,
            color = AppTheme.colors.IllustrationBlue,
            shadowElevation = 8.dp
        ) {
            Icon(
                Icons.Default.PhoneCallback,
                null,
                modifier = Modifier.padding(14.dp * scale),
                tint = Color(0xFF1B1B2F)
            )
        }
    }
}

@Composable
fun ControlButton(icon: ImageVector, label: String, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(42.dp * scale),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        ) {
            Icon(icon, null, modifier = Modifier.padding(10.dp * scale), tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(label, fontSize = 8.sp * scale, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp * scale))
    }
}
