@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.kakaoimagevideosearch.presentation.search

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import com.example.kakaoimagevideosearch.presentation.common.SearchResultItem
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = mavericksViewModel()
) {
    val state by viewModel.collectAsState()
    
    val pagingFlow = remember(state.searchCounter) {
        state.pagingDataFlow
    }
    val lazyPagingItems = pagingFlow.collectAsLazyPagingItems()
    
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()

    val favoriteStatuses by state.favoriteStatusFlow.collectAsState(initial = emptyList())

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is SearchEffect.FavoriteToggled -> {
                    // 북마크 토글 완료 효과 처리를 위한 추가 로직 (필요시)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchTextInput,
                onValueChange = { viewModel.onEvent(SearchEvent.UpdateSearchInput(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("검색어를 입력하세요") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            if (state.searchSetupAsync is Fail) {
                Text(
                    text = "검색 Flow 설정 오류: ${state.setupError?.message ?: "알 수 없는 오류"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (state.searchSetupAsync is Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val loadState = lazyPagingItems.loadState
                val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(2) }
                
                if (state.query.isBlank() && lazyPagingItems.itemCount == 0) {
                    item(span = span, key = "Initial Guide") {
                        EmptySearchPlaceholder()
                    }
                } else {
                    items(
                        count = lazyPagingItems.itemCount,
                        key = { index -> lazyPagingItems.peek(index)?.id ?: index },
                    ) { index ->
                        val item = lazyPagingItems[index]
                        if (item != null) {
                            val isFavorite = favoriteStatuses.find { it.id == item.id }?.isFavorite
                                ?: item.isFavorite

                            SearchResultItem(
                                item = item,
                                isFavorite = isFavorite,
                                onFavoriteClick = { 
                                    viewModel.onEvent(SearchEvent.ToggleFavorite(item.id))
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            )
                        }
                    }

                    if (state.query.isNotBlank()) {
                        when (val refreshState = loadState.refresh) {
                            is LoadState.Loading -> item(span = span, key = "Refresh Loading") {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { 
                                    CircularProgressIndicator() 
                                }
                            }
                            is LoadState.Error -> item(span = span, key = "Refresh Error") {
                                ErrorItem(message = refreshState.error.message ?: "데이터 로딩 실패", onRetry = { lazyPagingItems.retry() })
                            }
                            is LoadState.NotLoading -> {
                                if (lazyPagingItems.itemCount == 0 && refreshState.endOfPaginationReached) {
                                    item(span = span, key = "Empty Result") {
                                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { 
                                            Text("검색 결과가 없습니다.") 
                                        }
                                    }
                                }
                            }
                        }

                        when (val appendState = loadState.append) {
                            is LoadState.Loading -> item(span = span, key = "Append Loading") {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) { 
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp)) 
                                }
                            }
                            is LoadState.Error -> item(span = span, key = "Append Error") {
                                ErrorItem(message = appendState.error.message ?: "추가 로딩 실패", onRetry = { lazyPagingItems.retry() }, isCompact = true)
                            }
                            else -> { /* 추가 로딩 완료 시 표시할 내용 없음 */ }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "이미지와 비디오를 검색해보세요",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "상단 검색창에 검색어를 입력하여 원하는 콘텐츠를 찾아보세요",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 8.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onRetry) {
                Text("재시도")
            }
        }
    }
} 