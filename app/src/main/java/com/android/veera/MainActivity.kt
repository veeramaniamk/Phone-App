package com.android.veera

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.veera.core.theme.DialerTheme
import com.veera.core.util.DialerManager
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

                when (currentScreen) {
                    Screen.Splash -> {
                        SplashScreen(
                            onSplashComplete = {
                                currentScreen = if (dialerManager.isDefaultDialer()) {
                                    Screen.Home
                                } else {
                                    Screen.Onboarding
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
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            DialerScreen()
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Home : Screen()
}

@Composable
fun DialerScreen() {
    val context = LocalContext.current
    var isDefaultDialer by remember { mutableStateOf(false) }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            isDefaultDialer = true
            Toast.makeText(context, "App is now the default dialer", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to set as default dialer", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        isDefaultDialer = checkIsDefaultDialer(context)
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isDefaultDialer) "This is your default Phone App" else "This app is NOT your default Phone App",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        if (!isDefaultDialer) {
            Button(onClick = {
                requestDefaultDialerRole(context, roleLauncher)
            }) {
                Text("Set as Default Phone App")
            }
        } else {
            Text(
                text = "Eligibility confirmed for Android 12, 13, and 14+",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun checkIsDefaultDialer(context: Context): Boolean {
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    return context.packageName == telecomManager.defaultDialerPackage
}

private fun requestDefaultDialerRole(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                launcher.launch(intent)
            }
        }
    } else {
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
            putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
        }
        launcher.launch(intent)
    }
}



