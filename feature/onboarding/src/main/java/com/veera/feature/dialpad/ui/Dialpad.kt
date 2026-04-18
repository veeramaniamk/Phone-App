package com.veera.feature.dialpad.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DialpadScreen(
    modifier: Modifier = Modifier,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    onCallClick: (String) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    
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
            val keySize = if (screenHeight > 800.dp) 84.dp else if (screenHeight > 600.dp) 72.dp else 60.dp
            val spacing = if (screenHeight > 800.dp) 24.dp else 16.dp
            val numberTextSize = if (screenWidth > 400.dp) 44.sp else 36.sp
            val dialBtnSize = if (screenHeight > 800.dp) 84.dp else 72.dp

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 600, easing = EaseOutQuart)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 400, easing = EaseInQuart)
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Minimize",
                                modifier = Modifier.size(20.dp).graphicsLayer(rotationZ = 135f),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.5f))

                    // Phone Number Display
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = phoneNumber,
                            style = AppTheme.typography.titleLarge.copy(
                                fontSize = numberTextSize,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            maxLines = 1
                        )
                        
                        if (phoneNumber.isNotEmpty()) {
                            Text(
                                text = "Add to contacts",
                                style = AppTheme.typography.bodyMedium.copy(
                                    color = AppTheme.colors.Primary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .combinedClickable(onClick = { /* Handle add contact */ })
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.8f))

                    // Dial Keys Grid
                    DialKeysGrid(
                        keySize = keySize,
                        spacing = spacing,
                        onKeyClick = { digit ->
                            if (phoneNumber.length < 15) phoneNumber += digit
                        }
                    )

                    Spacer(modifier = Modifier.height(spacing * 2))

                    // Bottom Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp))

                        // Call Button
                        Surface(
                            modifier = Modifier
                                .size(dialBtnSize)
                                .clip(CircleShape)
                                .combinedClickable(onClick = { if (phoneNumber.isNotEmpty()) onCallClick(phoneNumber) }),
                            color = Color.Transparent,
                            shadowElevation = 12.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                AppTheme.colors.Success,
                                                AppTheme.colors.Success.copy(alpha = 0.85f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(dialBtnSize * 0.45f)
                                )
                            }
                        }

                        // Backspace Button
                        IconButton(
                            onClick = { if (phoneNumber.isNotEmpty()) phoneNumber = phoneNumber.dropLast(1) },
                            enabled = phoneNumber.isNotEmpty(),
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer {
                                    alpha = if (phoneNumber.isNotEmpty()) 1f else 0.3f
                                }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialKeysGrid(
    keySize: Dp,
    spacing: Dp,
    onKeyClick: (String) -> Unit
) {
    val keys = listOf(
        listOf("1" to " ", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
        listOf("*" to " ", "0" to "+", "#" to " ")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing * 1.5f)) {
                row.forEach { (digit, letters) ->
                    DialKey(
                        digit = digit,
                        letters = letters,
                        size = keySize,
                        onClick = { onKeyClick(digit) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialKey(
    digit: String,
    letters: String,
    size: Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { /* Handle long press */ }
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = CircleShape
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                style = AppTheme.typography.titleLarge.copy(
                    fontSize = (size.value * 0.38).sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = letters,
                style = AppTheme.typography.labelMedium.copy(
                    fontSize = (size.value * 0.12).sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
