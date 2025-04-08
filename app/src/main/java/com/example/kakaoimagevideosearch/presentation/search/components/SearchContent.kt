package com.example.kakaoimagevideosearch.presentation.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import com.example.kakaoimagevideosearch.presentation.common.SearchResultItem
import com.example.kakaoimagevideosearch.presentation.search.components.EmptySearchPlaceholder
import com.example.kakaoimagevideosearch.presentation.search.components.EmptySearchResultItem
import com.example.kakaoimagevideosearch.presentation.search.components.ErrorItem
import com.example.kakaoimagevideosearch.presentation.search.components.LoadingIndicator
import com.example.kakaoimagevideosearch.presentation.search.components.ShimmerItem
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.flowOf

/**
 * 검색 결과 컨텐츠 컴포넌트
 * 
 * @param lazyPagingItems 페이징된 검색 결과 아이템들
 * @param query 현재 검색 쿼리
 * @param favoriteStatuses 즐겨찾기 상태 목록
 * @param gridState LazyGridState 객체
 * @param onFavoriteClick 즐겨찾기 버튼 클릭 시 호출되는 콜백
 * @param onRetry 재시도 버튼 클릭 시 호출되는 콜백
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun SearchContent(
    lazyPagingItems: LazyPagingItems<SearchResult>,
    query: String,
    favoriteStatuses: List<SearchResult>,
    gridState: LazyGridState,
    onFavoriteClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val loadState = lazyPagingItems.loadState
        val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(2) }
        
        if (query.isBlank() && lazyPagingItems.itemCount == 0) {
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
                            onFavoriteClick(item.id)
                        }
                    )
                } else {
                    ShimmerItem()
                }
            }

            if (query.isNotBlank()) {
                when (val refreshState = loadState.refresh) {
                    is LoadState.Loading -> item(span = span, key = "Refresh Loading") {
                        LoadingIndicator(size = 48.dp, boxHeight = 200)
                    }
                    is LoadState.Error -> item(span = span, key = "Refresh Error") {
                        ErrorItem(message = refreshState.error.message ?: "데이터 로딩 실패", onRetry = onRetry)
                    }
                    is LoadState.NotLoading -> {
                        if (lazyPagingItems.itemCount == 0 && refreshState.endOfPaginationReached) {
                            item(span = span, key = "Empty Result") {
                                EmptySearchResultItem()
                            }
                        }
                    }
                }

                when (val appendState = loadState.append) {
                    is LoadState.Loading -> item(span = span, key = "Append Loading") {
                        LoadingIndicator(size = 32.dp)
                    }
                    is LoadState.Error -> item(span = span, key = "Append Error") {
                        ErrorItem(message = appendState.error.message ?: "추가 로딩 실패", onRetry = onRetry, isCompact = true)
                    }
                    else -> { /* 추가 로딩 완료 시 표시할 내용 없음 */ }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchContentEmptyPreview() {
    MyApplicationTheme {
        // 빈 상태의 검색 결과 프리뷰
        val emptyPagingItems = flowOf(PagingData.empty<SearchResult>()).collectAsLazyPagingItems()
        
        SearchContent(
            lazyPagingItems = emptyPagingItems,
            query = "",
            favoriteStatuses = emptyList(),
            gridState = LazyGridState(),
            onFavoriteClick = {},
            onRetry = {}
        )
    }
} 