package com.veera.feature.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import androidx.hilt.navigation.compose.hiltViewModel

import com.veera.core.theme.AppTheme

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onSplashComplete: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        viewModel.navigateToNext.collect {
            onSplashComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.colors.DarkSurface,
                        AppTheme.colors.DarkBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Container
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(AppTheme.dimensions.cornerExtraLarge),
                color = AppTheme.colors.ButtonGradientEnd.copy(alpha = 0.8f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone Icon",
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.dimensions.paddingLarge))

            Text(
                text = "Phone",
                style = AppTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 32.sp // Keeping logo text large
                )
            )

            Text(
                text = "— LUMINOUS CONCIERGE —",
                style = AppTheme.typography.labelMedium.copy(
                    color = AppTheme.colors.TextSecondary,
                    letterSpacing = 2.sp
                )
            )
        }

        // Bottom progress bar and version
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = AppTheme.dimensions.paddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp),
                color = AppTheme.colors.Primary,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(AppTheme.dimensions.paddingLarge))

            Text(
                text = "VERSION 4.0.2 • SECURE CONNECTION",
                style = AppTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}
