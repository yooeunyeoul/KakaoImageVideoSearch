package com.example.kakaoimagevideosearch.presentation.bookmark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import com.example.kakaoimagevideosearch.presentation.bookmark.components.BookmarkContent
import com.example.kakaoimagevideosearch.presentation.bookmark.components.BookmarkErrorView
import com.example.kakaoimagevideosearch.presentation.bookmark.components.BookmarkLoadingIndicator
import com.example.kakaoimagevideosearch.presentation.bookmark.components.BookmarkTitle
import com.example.kakaoimagevideosearch.presentation.bookmark.components.EmptyBookmarksPlaceholder
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

/**
 * 북마크 화면 메인 컴포넌트
 */
@Composable
fun BookmarkScreen(
    viewModel: BookmarkViewModel = mavericksViewModel()
) {
    val state by viewModel.collectAsState()

    BookmarkScreenContent(
        bookmarksAsync = state.bookmarksAsync,
        bookmarkCount = state.bookmarkCount,
        effectFlow = viewModel.effect,
        onLoadBookmarks = { viewModel.onEvent(BookmarkEvent.LoadBookmarks) }
    )
}

/**
 * 북마크 화면 내용 컴포넌트 - 상태 호이스팅 처리된 화면
 */
@Composable
fun BookmarkScreenContent(
    bookmarksAsync: Async<List<SearchResult>>,
    bookmarkCount: Int,
    effectFlow: Flow<BookmarkEffect>,
    onLoadBookmarks: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 사이드 이펙트 처리
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                is BookmarkEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // 초기화 시 북마크 로드 (필요한 경우)
    LaunchedEffect(Unit) {
        if (bookmarksAsync is Uninitialized) {
            onLoadBookmarks()
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
            // 북마크 타이틀
            BookmarkTitle(count = bookmarkCount)
            
            when (bookmarksAsync) {
                is Uninitialized -> {
                    // 초기 상태 - 안내 메시지 표시
                    EmptyBookmarksPlaceholder(
                        message = "검색 화면에서 하트 아이콘을 눌러 좋아하는 콘텐츠를 북마크에 저장하세요"
                    )
                }
                is Loading -> {
                    // 로딩 중 상태
                    BookmarkLoadingIndicator()
                }
                is Success -> {
                    val bookmarks = bookmarksAsync()
                    if (bookmarks.isEmpty()) {
                        EmptyBookmarksPlaceholder(
                            message = "저장된 북마크가 없습니다."
                        )
                    } else {
                        BookmarkContent(
                            bookmarks = bookmarks,
                            gridState = gridState
                        )
                    }
                }
                is Fail -> {
                    BookmarkErrorView(
                        errorMessage = bookmarksAsync.error.message ?: "알 수 없는 오류",
                        onRetry = onLoadBookmarks
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkScreenContentEmptyPreview() {
    MyApplicationTheme {
        BookmarkScreenContent(
            bookmarksAsync = Success(emptyList()),
            bookmarkCount = 0,
            effectFlow = emptyFlow(),
            onLoadBookmarks = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkScreenContentItemsPreview() {
    MyApplicationTheme {
        BookmarkScreenContent(
            bookmarksAsync = Success(
                listOf(
                    SearchResult(
                        id = "1",
                        thumbnailUrl = "https://example.com/image.jpg",
                        title = "예시 이미지 제목",
                        source = "예시 사이트",
                        datetime = "2023-05-01T12:00:00.000+09:00",
                        type = SearchResultType.IMAGE,
                        isFavorite = true
                    ),
                    SearchResult(
                        id = "2",
                        thumbnailUrl = "https://example.com/video.jpg",
                        title = "예시 비디오 제목",
                        source = "예시 비디오 사이트",
                        datetime = "2023-05-02T15:30:00.000+09:00",
                        type = SearchResultType.VIDEO,
                        isFavorite = true
                    )
                )
            ),
            bookmarkCount = 2,
            effectFlow = emptyFlow(),
            onLoadBookmarks = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkScreenContentLoadingPreview() {
    MyApplicationTheme {
        BookmarkScreenContent(
            bookmarksAsync = Loading(),
            bookmarkCount = 0,
            effectFlow = emptyFlow(),
            onLoadBookmarks = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkScreenContentErrorPreview() {
    MyApplicationTheme {
        BookmarkScreenContent(
            bookmarksAsync = Fail(Exception("네트워크 오류")),
            bookmarkCount = 0,
            effectFlow = emptyFlow(),
            onLoadBookmarks = {}
        )
    }
} 