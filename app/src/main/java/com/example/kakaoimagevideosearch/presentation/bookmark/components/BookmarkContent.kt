package com.example.kakaoimagevideosearch.presentation.bookmark.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import com.example.kakaoimagevideosearch.presentation.common.SearchResultItem
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme

/**
 * 북마크 목록을 그리드로 표시하는 컴포넌트
 * 
 * @param bookmarks 표시할 북마크 목록
 * @param gridState LazyGridState
 * @param onItemClick 아이템 클릭 시 호출할 콜백
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun BookmarkContent(
    bookmarks: List<SearchResult>,
    gridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bookmarks, key = {it.id}) { bookmark ->
            SearchResultItem(
                item = bookmark,
                showFavoriteButton = false, // 북마크 화면에서는 하트 버튼 표시 안 함
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkContentPreview() {
    MyApplicationTheme {
        BookmarkContent(
            bookmarks = listOf(
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
            ),
            gridState = LazyGridState()
        )
    }
} 