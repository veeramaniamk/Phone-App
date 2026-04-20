package com.veera.feature.splash.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    onSplashComplete: () -> Unit
) {
    DialerTheme(darkTheme = isDarkModeEnabled) {
        var startAnimation by remember { mutableStateOf(false) }
        
        LaunchedEffect(key1 = true) {
            startAnimation = true
            viewModel.navigateToNext.collect {
                onSplashComplete()
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkModeEnabled) {
                            listOf(AppTheme.colors.DarkSurface, AppTheme.colors.DarkBackground)
                        } else {
                            listOf(AppTheme.colors.LightSurface, AppTheme.colors.LightBackground)
                        }
                    )
                )
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // Responsive metrics
            val iconSize = if (screenHeight > 800.dp) 140.dp else 110.dp
            val logoIconSize = if (screenHeight > 800.dp) 70.dp else 56.dp
            val titleSize = if (screenWidth > 400.dp) 36.sp else 28.sp

            val contentAlpha by animateFloatAsState(
                targetValue = if (startAnimation) 1f else 0f,
                animationSpec = tween(1000),
                label = "ContentAlpha"
            )
            
            val contentScale by animateFloatAsState(
                targetValue = if (startAnimation) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "ContentScale"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        alpha = contentAlpha,
                        scaleX = contentScale,
                        scaleY = contentScale
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon Container
                Surface(
                    modifier = Modifier.size(iconSize),
                    shape = RoundedCornerShape(AppTheme.dimensions.cornerExtraLarge),
                    color = AppTheme.colors.ButtonGradientEnd.copy(alpha = 0.9f),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon",
                            modifier = Modifier.size(logoIconSize),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppTheme.dimensions.paddingExtraLarge))

                Text(
                    text = "Phone",
                    style = AppTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = titleSize,
                        letterSpacing = 1.sp
                    )
                )

            }

            // Bottom progress bar and version
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = AppTheme.dimensions.paddingExtraLarge)
                    .graphicsLayer(alpha = contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(screenWidth * 0.5f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AppTheme.colors.Primary,
                    trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(AppTheme.dimensions.paddingLarge))

                Text(
                    text = "VERSION 4.0.2 • SECURE CONNECTION",
                    style = AppTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
