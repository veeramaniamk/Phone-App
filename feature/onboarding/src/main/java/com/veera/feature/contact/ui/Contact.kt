package com.veera.feature.contact.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import com.veera.feature.contact_detail.ui.ContactDetailScreen
import com.veera.feature.new_contact.ui.NewContactScreen

data class ContactEntry(
    val id: String,
    val name: String,
    val number: String,
    val initial: String = name.take(1).uppercase()
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ContactScreen(
    modifier: Modifier = Modifier,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    onContactClick: (ContactEntry) -> Unit = {}
) {
    var showNewContact by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ContactEntry?>(null) }

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
            val horizontalPadding = if (screenWidth > 600.dp) 32.dp else 20.dp
            val titleSize = if (screenWidth > 400.dp) 32.sp else 28.sp
            val itemHeight = if (screenHeight > 800.dp) 80.dp else 64.dp
            val avatarSize = if (screenWidth > 400.dp) 48.dp else 40.dp

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600)) + expandVertically(animationSpec = tween(600)),
                modifier = Modifier.fillMaxSize()
            ) {
                Scaffold(
                    topBar = {
                        ContactHeader(
                            titleSize = titleSize,
                            padding = horizontalPadding,
                            onAddClick = { showNewContact = true }
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    ContactsList(
                        modifier = Modifier.padding(paddingValues),
                        horizontalPadding = horizontalPadding,
                        itemHeight = itemHeight,
                        avatarSize = avatarSize,
                        onContactClick = { contact ->
                            selectedContact = contact
                            onContactClick(contact)
                        }
                    )
                }
            }
            
            // New Contact Screen Overlay
            if (showNewContact) {
                NewContactScreen(
                    onDismiss = { showNewContact = false },
                    onSave = { first, last, phone ->
                        showNewContact = false
                    },
                    isDarkModeEnabled = isDarkModeEnabled
                )
            }

            // Contact Detail Screen Overlay
            selectedContact?.let { contact ->
                ContactDetailScreen(
                    contact = contact,
                    onBackClick = { selectedContact = null },
                    isDarkModeEnabled = isDarkModeEnabled
                )
            }
        }
    }
}

@Composable
private fun ContactHeader(
    titleSize: TextUnit,
    padding: Dp,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = padding, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Contacts",
                style = AppTheme.typography.titleLarge.copy(
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "My contact list",
                style = AppTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeaderActionIcon(Icons.Default.Add, onClick = onAddClick)
            HeaderActionIcon(Icons.Default.Search)
        }
    }
}

@Composable
private fun HeaderActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ContactsList(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp,
    itemHeight: Dp,
    avatarSize: Dp,
    onContactClick: (ContactEntry) -> Unit
) {
    val dummyContacts = remember {
        listOf(
            ContactEntry("1", "Alex Rivera", "+1 234 567 890"),
            ContactEntry("2", "Alice Johnson", "+1 333 444 555"),
            ContactEntry("3", "Bob Smith", "+1 666 777 888"),
            ContactEntry("4", "Charlie Brown", "+1 999 000 111"),
            ContactEntry("5", "David Miller", "+1 321 654 987"),
            ContactEntry("6", "Emily Davis", "+1 777 888 999"),
            ContactEntry("7", "Frank Wilson", "+1 555 123 456"),
            ContactEntry("8", "Grace Hopper", "+1 888 999 000"),
            ContactEntry("9", "John Doe", "+1 987 654 321"),
            ContactEntry("10", "Kevin Wilson", "+1 999 888 777"),
            ContactEntry("11", "Michael Brown", "+1 444 222 111"),
            ContactEntry("12", "Sarah Wilson", "+1 555 019 283")
        ).sortedBy { it.name }
    }

    val grouped = dummyContacts.groupBy { it.initial }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
    ) {
        grouped.forEach { (initial, contacts) ->
            item {
                Text(
                    text = initial,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
                    style = AppTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.Primary,
                        letterSpacing = 2.sp
                    )
                )
            }
            items(contacts, key = { it.id }) { contact ->
                ContactItem(
                    contact = contact,
                    horizontalPadding = horizontalPadding,
                    height = itemHeight,
                    avatarSize = avatarSize,
                    onClick = { onContactClick(contact) }
                )
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: ContactEntry,
    horizontalPadding: Dp,
    height: Dp,
    avatarSize: Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(avatarSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = contact.initial,
                    style = AppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (avatarSize.value * 0.4).sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = contact.number,
                style = AppTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
