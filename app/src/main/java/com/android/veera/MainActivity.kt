package com.android.veera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.veera.core.theme.DialerTheme
import com.veera.core.util.DialerManager
import com.veera.feature.home.HomeScreen
import com.veera.feature.onboarding.ui.DefaultDialerScreen
import com.veera.feature.splash.ui.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dialerManager: DialerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DialerTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith
                                fadeOut(animationSpec = tween(500))
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        Screen.Splash -> {
                            SplashScreen(
                                onSplashComplete = {
                                    currentScreen = if (dialerManager.isDefaultDialer()) {
                                        Screen.Home
                                    } else {
//                                        Screen.Onboarding
                                        Screen.Home
                                    }
                                }
                            )
                        }
                        Screen.Onboarding -> {
                            DefaultDialerScreen(
                                onComplete = {
                                    currentScreen = Screen.Home
                                },
                                onExit = {
                                    finish()
                                }
                            )
                        }
                        Screen.Home -> {
                            HomeScreen(
                                onCallClick = { /* Handle call action */ },
                                onDialpadCall = { /* Open dialpad */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Home : Screen()
}
