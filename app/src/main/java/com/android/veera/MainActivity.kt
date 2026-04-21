package com.android.veera

import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.veera.core.theme.DialerTheme
import com.veera.core.util.DialerManager
import com.veera.feature.contact.ui.ContactScreen
import com.veera.feature.home.ui.HomeScreen
import com.veera.feature.onboarding.ui.DefaultDialerScreen
import com.veera.feature.splash.ui.SplashScreen
import com.veera.feature.ongoing.ui.OngoingCallScreen
import com.veera.feature.incommingcall.ui.IncomingCallScreen
import com.veera.core.telephony.viewmodel.CallViewModel
import com.veera.feature.new_contact.ui.NewContactScreen
import com.veera.core.telephony.model.Contact
import com.veera.feature.contact.ui.ContactsViewModel
import com.veera.feature.contact_detail.ui.ContactDetailScreen
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import com.veera.core.telephony.notification.CallNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dialerManager: DialerManager
    
    @Inject
    lateinit var notificationManager: CallNotificationManager
    
    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DialerTheme {
                var currentScreen by rememberSaveable { mutableStateOf(Screen.Splash) }
                val callState by callViewModel.callState.collectAsState()
                val callerName by callViewModel.callerName.collectAsState()
                val callerNumber by callViewModel.callerNumber.collectAsState()
                val callerPhotoUri by callViewModel.callerPhotoUri.collectAsState()
                val callStatus by callViewModel.callStatusString.collectAsState()
                val isMuted by callViewModel.isMuted.collectAsState()
                val isSpeakerOn by callViewModel.isSpeakerOn.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
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
                                            Screen.Onboarding
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
                                MainContainer(
                                    onCallClick = { number -> callViewModel.makeCall(number) },
                                    callViewModel = callViewModel
                                )
                            }
                        }
                    }

                    // Global Call Screens Overlay
                    AnimatedVisibility(
                        visible = callState != Call.STATE_DISCONNECTED,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        when (callState) {
                            Call.STATE_RINGING -> {
                                IncomingCallScreen(
                                    name = callerName,
                                    number = callerNumber,
                                    photoUri = callerPhotoUri,
                                    onAccept = { callViewModel.answerCall() },
                                    onDecline = { callViewModel.rejectCall() }
                                )
                            }
                            else -> {
                                if (callState != Call.STATE_DISCONNECTED) {
                                    OngoingCallScreen(
                                        name = callerName,
                                        number = callerNumber,
                                        status = callStatus,
                                        photoUri = callerPhotoUri,
                                        isMuted = isMuted,
                                        isSpeakerOn = isSpeakerOn,
                                        onMuteClick = { callViewModel.toggleMute() },
                                        onSpeakerClick = { callViewModel.toggleSpeaker() },
                                        onEndCall = { callViewModel.disconnectCall() },
                                        connectTimeMillis = callViewModel.currentCall.value?.details?.connectTimeMillis ?: 0L
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        // If we have an active call, cancel the notification when app starts/comes to foreground
        if (callViewModel.callState.value == Call.STATE_ACTIVE) {
            notificationManager.cancelNotification()
        }
    }

    override fun onStop() {
        super.onStop()
        // If we have an active call, show the notification when app goes to background
        if (callViewModel.callState.value == Call.STATE_ACTIVE) {
            val connectTime = callViewModel.currentCall.value?.details?.connectTimeMillis ?: System.currentTimeMillis()
            notificationManager.showOngoingCallNotification(
                callViewModel.callerName.value,
                callViewModel.callerNumber.value,
                callViewModel.isSpeakerOn.value,
                connectTime,
                callViewModel.callerPhotoUri.value
            )
        }
    }
}

@Composable
fun MainContainer(
    onCallClick: (String) -> Unit,
    callViewModel: CallViewModel
) {
    var currentTab by rememberSaveable { mutableStateOf(BottomTab.Recents) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showNewContact by remember { mutableStateOf<String?>(null) } // value is the number to pre-fill
    val contactsViewModel: ContactsViewModel = hiltViewModel()
    val context = LocalContext.current

    val onMessageClick: (String) -> Unit = { number ->
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
        }
        context.startActivity(intent)
    }

    val scope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(enabled = selectedContact != null || showNewContact != null || currentTab != BottomTab.Recents) {
        if (selectedContact != null) {
            selectedContact = null
        } else if (showNewContact != null) {
            showNewContact = null
        } else {
            currentTab = BottomTab.Recents
        }
    }

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
                    BottomTab.Recents -> HomeScreen(
                        onContactDetailClick = { recentCall ->
                            if (recentCall.contactId != null) {
                                scope.launch {
                                    selectedContact = contactsViewModel.fetchContactById(recentCall.contactId!!)
                                }
                            }
                        },
                        onDialpadCall = { number -> onCallClick(number) },
                        onNavigateToCreateContact = { number -> showNewContact = number }
                    )
                    BottomTab.Contacts -> ContactScreen(
                        onContactClick = { contact ->
                            selectedContact = contact
                        }
                    )
                }
            }

            // Page Overlays with Slide Animation
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = selectedContact,
                    transitionSpec = {
                        if (targetState != null) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "DetailTransition"
                ) { contact ->
                    if (contact != null) {
                        ContactDetailScreen(
                            contact = contact,
                            onBackClick = { selectedContact = null },
                            onCallClick = onCallClick,
                            onMessageClick = onMessageClick,
                            viewModel = contactsViewModel
                        )
                    }
                }

                AnimatedContent(
                    targetState = showNewContact,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { it } + fadeOut()
                    },
                    label = "NewContactTransition"
                ) { number ->
                    if (number != null) {
                        NewContactScreen(
                            initialNumber = number,
                            onDismiss = { showNewContact = null },
                            onSave = { first, last, phone, saveLocation ->
                                showNewContact = null
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class BottomTab(val title: String, val icon: ImageVector) {
    Recents("Recents", Icons.Default.History),
    Contacts("Contacts", Icons.Default.Contacts)
}

enum class Screen {
    Splash,
    Onboarding,
    MainContainer
}
