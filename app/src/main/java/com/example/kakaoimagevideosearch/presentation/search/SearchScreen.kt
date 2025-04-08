@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.kakaoimagevideosearch.presentation.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.presentation.search.components.LoadingIndicator
import com.example.kakaoimagevideosearch.presentation.search.components.SearchBar
import com.example.kakaoimagevideosearch.presentation.search.components.SearchContent
import com.example.kakaoimagevideosearch.presentation.search.components.SearchErrorView
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * 검색 화면 컴포넌트
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = mavericksViewModel()
) {
    val state by viewModel.collectAsState()
    
    // 이펙트 핸들러 정의
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchEffect.ShowError -> {
                    // 에러 처리 로직은 SearchScreenContent에서 처리
                }
            }
        }
    }
    
    SearchScreenContent(
        searchTextInput = state.searchTextInput,
        query = state.query,
        searchCounter = state.searchCounter,
        pagingDataFlow = state.pagingDataFlow,
        favoriteStatusFlow = state.favoriteStatusFlow,
        isSetupLoading = state.searchSetupAsync is Loading,
        setupError = if (state.searchSetupAsync is Fail) state.setupError?.message ?: "알 수 없는 오류" else null,
        effectFlow = viewModel.effect,
        onSearchTextChange = { viewModel.onEvent(SearchEvent.UpdateSearchInput(it)) },
        onFavoriteClick = { viewModel.onEvent(SearchEvent.ToggleFavorite(it)) }
    )
}

/**
 * 검색 화면 내용 컴포넌트 - 상태 호이스팅 처리된 화면
 */
@Composable
fun SearchScreenContent(
    searchTextInput: String,
    query: String,
    searchCounter: Int,
    pagingDataFlow: Flow<PagingData<SearchResult>>,
    favoriteStatusFlow: Flow<List<SearchResult>>,
    isSetupLoading: Boolean,
    setupError: String?,
    effectFlow: Flow<SearchEffect>,
    onSearchTextChange: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
) {
    val pagingFlow = remember(searchCounter) {
        pagingDataFlow
    }
    val lazyPagingItems = pagingFlow.collectAsLazyPagingItems()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()

    val favoriteStatuses by favoriteStatusFlow.collectAsState(initial = emptyList())
    
    // 사이드 이펙트 처리
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                is SearchEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
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
            SearchBar(
                searchText = searchTextInput,
                onSearchTextChange = onSearchTextChange
            )

            if (setupError != null) {
                SearchErrorView(errorMessage = setupError)
            }

            if (isSetupLoading) {
                LoadingIndicator(size = 24.dp)
            }

            SearchContent(
                lazyPagingItems = lazyPagingItems,
                query = query,
                favoriteStatuses = favoriteStatuses,
                gridState = gridState,
                onFavoriteClick = onFavoriteClick,
                onRetry = { lazyPagingItems.retry() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenContentPreview() {
    MyApplicationTheme {
        SearchScreenContent(
            searchTextInput = "",
            query = "",
            searchCounter = 0,
            pagingDataFlow = flowOf(PagingData.empty()),
            favoriteStatusFlow = emptyFlow(),
            isSetupLoading = false,
            setupError = null,
            effectFlow = emptyFlow(),
            onSearchTextChange = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenContentLoadingPreview() {
    MyApplicationTheme {
        SearchScreenContent(
            searchTextInput = "고양이",
            query = "고양이",
            searchCounter = 0,
            pagingDataFlow = flowOf(PagingData.empty()),
            favoriteStatusFlow = emptyFlow(),
            isSetupLoading = true,
            setupError = null,
            effectFlow = emptyFlow(),
            onSearchTextChange = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenContentErrorPreview() {
    MyApplicationTheme {
        SearchScreenContent(
            searchTextInput = "고양이",
            query = "고양이",
            searchCounter = 0,
            pagingDataFlow = flowOf(PagingData.empty()),
            favoriteStatusFlow = emptyFlow(),
            isSetupLoading = false,
            setupError = "네트워크 오류가 발생했습니다",
            effectFlow = emptyFlow(),
            onSearchTextChange = {},
            onFavoriteClick = {}
        )
    }
} 