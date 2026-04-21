package com.veera.feature.contact.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import com.veera.core.telephony.model.Contact
import com.veera.core.telephony.model.ContactAccount
import com.veera.core.telephony.model.FilterType
import com.veera.feature.contact_detail.ui.ContactDetailScreen
import com.veera.feature.new_contact.ui.NewContactScreen
import com.veera.core.telephony.repository.CallLogEntry

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ContactScreen(
    modifier: Modifier = Modifier,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    viewModel: ContactsViewModel = hiltViewModel<ContactsViewModel>(),
    onContactClick: (Contact) -> Unit = {}
) {
    var showNewContact by remember { mutableStateOf(false) }
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val totalCount by viewModel.totalContactsCount.collectAsState()
    
    val pagedContacts = viewModel.contacts.collectAsLazyPagingItems()

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
            
            // Responsive metrics
            val horizontalPadding = if (screenWidth > 600.dp) 32.dp else 20.dp
            val titleSize = if (screenWidth > 400.dp) 32.sp else 28.sp
            val itemHeight = 72.dp
            val avatarSize = 48.dp

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600)) + expandVertically(animationSpec = tween(600)),
                modifier = Modifier.fillMaxSize()
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            ContactHeader(
                                titleSize = titleSize,
                                padding = horizontalPadding,
                                totalCount = totalCount,
                                onAddClick = { showNewContact = true }
                            )
                            
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.padding(horizontal = horizontalPadding)
                            )
                            
                            FilterSection(
                                currentFilter = filterType,
                                onFilterChange = viewModel::onFilterTypeChange,
                                accounts = accounts,
                                selectedAccount = selectedAccount,
                                onAccountSelected = viewModel::onAccountSelected,
                                padding = horizontalPadding
                            )
                        }
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    ContactsList(
                        modifier = Modifier.padding(paddingValues),
                        horizontalPadding = horizontalPadding,
                        itemHeight = itemHeight,
                        avatarSize = avatarSize,
                        pagedContacts = pagedContacts,
                        onContactClick = { contact ->
                            onContactClick(contact)
                        }
                    )
                }
            }
            
            // New Contact Screen Overlay (Not modified here but kept for completeness)
            if (showNewContact) {
                NewContactScreen(
                    onDismiss = { showNewContact = false },
                    onSave = { first, last, phone, saveLocation ->
                        showNewContact = false
                    },
                    isDarkModeEnabled = isDarkModeEnabled
                )
            }

        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search contacts...",
                        style = AppTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    currentFilter: FilterType,
    onFilterChange: (FilterType) -> Unit,
    accounts: List<ContactAccount>,
    selectedAccount: ContactAccount?,
    onAccountSelected: (ContactAccount) -> Unit,
    padding: Dp
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = padding),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FilterType.values()) { type ->
                FilterChip(
                    text = type.name.lowercase().capitalize(),
                    selected = currentFilter == type,
                    onClick = { onFilterChange(type) }
                )
            }
        }

        AnimatedVisibility(
            visible = currentFilter == FilterType.EMAIL,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = padding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts) { account ->
                        FilterChip(
                            text = account.name,
                            selected = selectedAccount == account,
                            onClick = { onAccountSelected(account) },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary else containerColor,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun ContactHeader(
    titleSize: TextUnit,
    padding: Dp,
    totalCount: Int,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = padding, vertical = 16.dp),
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
            AnimatedContent(
                targetState = totalCount,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn() togetherWith 
                    slideOutVertically { -it } + fadeOut()).using(SizeTransform(clip = false))
                },
                label = "CountAnimation"
            ) { count ->
                Text(
                    text = "$count total contacts",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        
        HeaderActionIcon(Icons.Default.Add, onClick = onAddClick)
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
    pagedContacts: LazyPagingItems<Contact>,
    onContactClick: (Contact) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
    ) {
        items(
            count = pagedContacts.itemCount,
            key = { index -> 
                val contact = pagedContacts[index]
                contact?.let { "${it.id}_${it.number}" } ?: "placeholder_$index"
            }
        ) { index ->
            val contact = pagedContacts[index]
            if (contact != null) {
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
    contact: Contact,
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
                if (contact.photoUri != null) {
                    AsyncImage(
                        model = contact.photoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
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

private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
