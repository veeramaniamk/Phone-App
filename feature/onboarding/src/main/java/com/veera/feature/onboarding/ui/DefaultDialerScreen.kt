package com.veera.feature.onboarding.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.veera.core.theme.ButtonGradientEnd
import com.veera.core.theme.ButtonGradientStart
import com.veera.core.theme.IllustrationBlue
import com.veera.core.theme.TextSecondary

@Composable
fun DefaultDialerScreen(
    viewModel: DefaultDialerViewModel = hiltViewModel(),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration Section
        PhoneIllustration(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(32.dp))

        // Text Content
        Text(
            text = "Set as Default Dialer",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To make and manage calls, please set\nthis app as your default dialer",
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

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
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ButtonGradientStart, ButtonGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Set Default Dialer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onExit) {
            Text(
                text = "Exit App",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            )
        }
    }
}

@Composable
fun PhoneIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background card effect
        Surface(
            modifier = Modifier
                .width(280.dp)
                .height(400.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.05f),
            shadowElevation = 8.dp
        ) {}

        // Phone Frame
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(420.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF1B1B2F))
                .padding(8.dp)
        ) {
            // Screen Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("9:41", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.SignalCellular4Bar, null, Modifier.size(12.dp), tint = Color.Black)
                        Icon(Icons.Default.Wifi, null, Modifier.size(12.dp), tint = Color.Black)
                        Icon(Icons.Default.BatteryFull, null, Modifier.size(12.dp), tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Profile
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFF7B8CFE)
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.padding(16.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Alex Rivera", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Calling...", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.weight(1f))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButton(Icons.Default.MicOff, "MUTE")
                    ControlButton(Icons.Default.Apps, "KEYPAD")
                    ControlButton(Icons.Default.VolumeUp, "SPEAKER")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Decline Button
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color(0xFFE91E63)
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        null,
                        modifier = Modifier.padding(12.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFF7B8CFE), modifier = Modifier.size(24.dp))
                    Icon(Icons.Default.Schedule, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                    Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }
        }

        // Floating Call-out Icon
        Surface(
            modifier = Modifier
                .offset(x = 110.dp, y = (-20).dp)
                .size(64.dp),
            shape = CircleShape,
            color = IllustrationBlue,
            shadowElevation = 12.dp
        ) {
            Icon(
                Icons.Default.PhoneCallback,
                null,
                modifier = Modifier.padding(16.dp),
                tint = Color(0xFF1B1B2F)
            )
        }
    }
}

@Composable
fun ControlButton(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color.LightGray.copy(alpha = 0.2f)
        ) {
            Icon(icon, null, modifier = Modifier.padding(12.dp), tint = Color.Black)
        }
        Text(label, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}
