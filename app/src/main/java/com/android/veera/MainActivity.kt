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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                DialerHomeScreen()
                            }
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
fun DialerHomeScreen() {
    var showDialpad by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Content (Recent Calls / Contacts)
            HomeScreenContent(onDialpadClick = { showDialpad = true })
        }

        // Smooth Dialpad Animation
        AnimatedVisibility(
            visible = showDialpad,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ) + fadeOut()
        ) {
            DialpadOverlay(onDismiss = { showDialpad = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(onDialpadClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5)) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF8F9FF),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.StarBorder, null) },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("Recents") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.PersonOutline, null) },
                    label = { Text("Contacts") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDialpadClick,
                containerColor = Color(0xFF7B8CFE),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Apps, contentDescription = "Dialpad", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "Recent Calls",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F51B5),
                    fontSize = 20.sp
                )
                Text(
                    "Contacts",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    fontSize = 20.sp
                )
            }

            // Recent Calls List (Mock data to match screenshot)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "RECENT ACTIVITY",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                val recentCalls = listOf(
                    CallItem("Julian Vaca", "Missed", "10:42 AM", Color(0xFFFF5252)),
                    CallItem("Sarah O'Connell", "Outgoing", "Yesterday", Color(0xFF3F51B5)),
                    CallItem("Zoe Sterling", "Incoming", "Tuesday", Color(0xFF3F51B5)),
                    CallItem("+1 (555) 012-9934", "San Jose, CA", "Oct 24", Color(0xFF3F51B5))
                )

                items(recentCalls) { call ->
                    RecentCallRow(call)
                }
            }
        }
    }
}

data class CallItem(val name: String, val status: String, val time: String, val color: Color)

@Composable
fun RecentCallRow(call: CallItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EAF6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF3F51B5))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(call.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${call.status} • ${call.time}", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE8EAF6))
            ) {
                Icon(Icons.Default.Call, null, tint = call.color)
            }
        }
    }
}

@Composable
fun DialpadOverlay(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, "Close")
                }
                Text("Dialpad", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "+1 (555) 0128",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B2F)
            )

            TextButton(onClick = {}) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to contacts")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dialpad Grid
            val keys = listOf(
                listOf("1", ""), listOf("2", "ABC"), listOf("3", "DEF"),
                listOf("4", "GHI"), listOf("5", "JKL"), listOf("6", "MNO"),
                listOf("7", "PQRS"), listOf("8", "TUV"), listOf("9", "WXYZ"),
                listOf("*", ""), listOf("0", "+"), listOf("#", "")
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        for (j in 0 until 3) {
                            val key = keys[i * 3 + j]
                            DialKey(key[0], key[1])
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp)) // Offset for backspace
                FloatingActionButton(
                    onClick = {},
                    containerColor = Color(0xFF7B8CFE),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Backspace, null, tint = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DialKey(number: String, letters: String) {
    Column(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFFF0F2FF))
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(number, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B2F))
        if (letters.isNotEmpty()) {
            Text(letters, fontSize = 10.sp, color = Color.Gray)
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



