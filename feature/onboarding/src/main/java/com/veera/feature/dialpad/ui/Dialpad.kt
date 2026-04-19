package com.veera.feature.dialpad.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.veera.core.theme.AppTheme
import com.veera.core.theme.DialerTheme
import com.veera.feature.dialpad.DialpadViewModel
import com.veera.core.telephony.repository.DialpadSuggestion

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DialpadScreen(
    modifier: Modifier = Modifier,
    isDarkModeEnabled: Boolean = isSystemInDarkTheme(),
    viewModel: DialpadViewModel = hiltViewModel(),
    onCallClick: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    onAddContactClick: (String) -> Unit = {},
    onContactDetailClick: (String) -> Unit = {}
) {
    val phoneNumber by viewModel.phoneNumber
    val suggestions = viewModel.suggestions
    val isSearching by remember { derivedStateOf { phoneNumber.isNotEmpty() } }
    
    var isKeypadVisible by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    // Keypad visibility management on scroll
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && isKeypadVisible && suggestions.isNotEmpty()) {
            isKeypadVisible = false
        }
    }

    DialerTheme(darkTheme = isDarkModeEnabled) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val screenHeight = maxHeight
            
            val keySize = if (screenHeight > 800.dp) 82.dp else if (screenHeight > 600.dp) 68.dp else 56.dp
            val spacing = if (screenHeight > 800.dp) 20.dp else 12.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Number and Suggestions Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Displayed Number
                    Text(
                        text = phoneNumber,
                        style = AppTheme.typography.titleLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                            .fillMaxWidth(),
                        maxLines = 1
                    )
                    
                    // Add to Contacts Button
                    AnimatedVisibility(
                        visible = phoneNumber.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        TextButton(
                            onClick = { onAddContactClick(phoneNumber) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create New Contact", style = AppTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary))
                        }
                    }

                    // Suggestions List
                    AnimatedVisibility(
                        visible = isSearching,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.weight(1f)
                    ) {
                        LaunchedEffect(listState) {
                            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                                .collect { lastIndex ->
                                    if (lastIndex != null && lastIndex >= suggestions.size - 5) {
                                        viewModel.loadMoreSuggestions()
                                    }
                                }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(suggestions) { suggestion ->
                                SuggestionItem(
                                    suggestion = suggestion,
                                    onClick = { 
                                        if (suggestion.contactId != null) {
                                            onContactDetailClick(suggestion.contactId!!)
                                        } else {
                                            viewModel.onNumberChanged(suggestion.number) 
                                        }
                                    }
                                )
                            }
                            
                            if (viewModel.isLoading.value) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    }
                                }
                            }
                        }
                    }
                    
                    if (!isSearching) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Keypad Area
                AnimatedVisibility(
                    visible = isKeypadVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                            )
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DialKeysGrid(
                            keySize = keySize,
                            spacing = spacing,
                            onKeyClick = { digit ->
                                if (phoneNumber.length < 15) {
                                    viewModel.onNumberChanged(phoneNumber + digit)
                                }
                            },
                            onLongKeyClick = { digit ->
                                when (digit) {
                                    "0" -> if (phoneNumber.length < 15) viewModel.onNumberChanged(phoneNumber + "+")
                                    "1" -> { /* Voicemail? */ }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(spacing * 1.5f))

                        // Action Row
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
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .combinedClickable(onClick = { if (phoneNumber.isNotEmpty()) onCallClick(phoneNumber) }),
                                color = AppTheme.colors.Success,
                                shadowElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Call, "Call", tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }

                            // Backspace
                            IconButton(
                                onClick = { if (phoneNumber.isNotEmpty()) viewModel.onNumberChanged(phoneNumber.dropLast(1)) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Backspace, "Clear", tint = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }

                // Restore Keypad Button (When hidden)
                if (!isKeypadVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { isKeypadVisible = true },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Restore Keypad", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(suggestion: DialpadSuggestion, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp).clip(CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (suggestion.photoUri != null) {
                        AsyncImage(
                            model = suggestion.photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            suggestion.name.firstOrNull()?.toString() ?: "#",
                            style = AppTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(suggestion.name, style = AppTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(suggestion.number, style = AppTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                suggestion.source,
                style = AppTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun DialKeysGrid(
    keySize: Dp,
    spacing: Dp,
    onKeyClick: (String) -> Unit,
    onLongKeyClick: (String) -> Unit
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
                        onClick = { onKeyClick(digit) },
                        onLongClick = { onLongKeyClick(digit) }
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
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
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
