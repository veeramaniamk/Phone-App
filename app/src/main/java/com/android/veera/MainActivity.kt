package com.android.veera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.veera.core.theme.DialerTheme
import com.veera.core.util.DialerManager
import com.veera.feature.contact.ui.ContactScreen
import com.veera.feature.home.HomeScreen
import com.veera.feature.incommingcall.ui.IncomingCallScreen
import com.veera.feature.new_contact.ui.NewContactScreen
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
                                        Screen.MainContainer
                                    } else {
                                        Screen.MainContainer
                                    }
                                }
                            )
                        }
                        Screen.Onboarding -> {
                            DefaultDialerScreen(
                                onComplete = {
                                    currentScreen = Screen.MainContainer
                                },
                                onExit = {
                                    finish()
                                }
                            )
                        }
                        Screen.MainContainer -> {
                            MainContainer()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun MainContainer() {
    var currentTab by remember { mutableStateOf(BottomTab.Recents) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    BottomTab.Recents -> HomeScreen()
                    BottomTab.Contacts -> ContactScreen()
                }
            }
        }
    }
}

enum class BottomTab(val title: String, val icon: ImageVector) {
    Recents("Recents", Icons.Default.History),
    Contacts("Contacts", Icons.Default.Contacts)
}

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object MainContainer : Screen()
    @Deprecated("Use MainContainer")
    object Home : Screen()
}

