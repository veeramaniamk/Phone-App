package com.veera.feature.home.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veera.core.theme.AppTheme
import com.veera.feature.home.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentCallsList(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp,
    itemHeight: Dp,
    avatarSize: Dp,
    viewModel: HomeViewModel,
    hasPermission: Boolean,
    onPermissionRequest: () -> Unit,
    onCallClick: (RecentCall) -> Unit
) {
    val listState = rememberLazyListState()
    
    val isSearching by viewModel.isSearching
    val displayList = if (isSearching) viewModel.searchResults else viewModel.allRecents
    val isLoading by if (isSearching) viewModel.isSearchLoading else viewModel.isLoading
    val isInitialLoading by viewModel.isInitialLoading
    val isEndReached by if (isSearching) viewModel.isSearchEndReached else viewModel.isEndReached

    // Initial load when permission is granted
    LaunchedEffect(hasPermission) {
        if (hasPermission && viewModel.allRecents.isEmpty()) {
            viewModel.loadNextPage()
        }
    }

    // Infinite scroll detection
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            
            lastVisibleItem.index >= displayList.size - 5 && !isLoading && !isEndReached
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && hasPermission) {
            if (isSearching) viewModel.loadNextSearchPage() else viewModel.loadNextPage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> {
                PermissionRequiredView(onPermissionRequest)
            }
            isInitialLoading && displayList.isEmpty() && !isSearching -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = AppTheme.colors.Primary,
                        strokeWidth = 3.dp
                    )
                }
            }
            isSearching && isInitialLoading && displayList.isEmpty() -> {
                // Shimmer or searching state
            }
            !isInitialLoading && displayList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(initialScale = 0.9f)
                    ) {
                        NoDataFoundView()
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
                ) {
                    items(
                        items = displayList,
                        key = { it.id }
                    ) { call ->
                        RecentCallItem(
                            modifier = Modifier.animateItem(
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            call = call,
                            horizontalPadding = horizontalPadding,
                            height = itemHeight,
                            avatarSize = avatarSize,
                            onClick = { onCallClick(call) }
                        )
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = AppTheme.colors.Primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequiredView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Permission Required",
            style = AppTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "To show your call history, we need access to your call log. This is required for the app to function as your dialer.",
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Grant Permission",
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}