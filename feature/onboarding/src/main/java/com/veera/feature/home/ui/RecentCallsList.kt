package com.veera.feature.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val allRecents = viewModel.allRecents
    val isLoading by viewModel.isLoading
    val isInitialLoading by viewModel.isInitialLoading
    val isEndReached by viewModel.isEndReached

    // Initial load when permission is granted
    LaunchedEffect(hasPermission) {
        if (hasPermission && allRecents.isEmpty()) {
            viewModel.loadNextPage()
        }
    }

    // Infinite scroll detection
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false

            lastVisibleItem.index >= allRecents.size - 5 && !isLoading && !isEndReached
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && hasPermission) {
            viewModel.loadNextPage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> {
                PermissionRequiredView(onPermissionRequest)
            }
            isInitialLoading && allRecents.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = AppTheme.colors.Primary,
                        strokeWidth = 3.dp
                    )
                }
            }
            !isInitialLoading && allRecents.isEmpty() -> {
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
                        items = allRecents,
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

                    if (isLoading && !isInitialLoading) {
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