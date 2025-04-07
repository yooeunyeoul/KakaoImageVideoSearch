package com.example.kakaoimagevideosearch.presentation.bookmark.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme

/**
 * 북마크 화면 상단에 표시되는 타이틀 컴포넌트
 * 
 * @param count 북마크 개수
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun BookmarkTitle(
    count: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "북마크 ($count)",
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(vertical = 16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun BookmarkTitlePreview() {
    MyApplicationTheme {
        BookmarkTitle(count = 5)
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkTitleEmptyPreview() {
    MyApplicationTheme {
        BookmarkTitle(count = 0)
    }
} 