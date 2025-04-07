package com.example.kakaoimagevideosearch.presentation.bookmark.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme

/**
 * 북마크 로딩 중 상태를 표시하는 로딩 인디케이터 컴포넌트
 * 
 * @param message 표시할 로딩 메시지
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun BookmarkLoadingIndicator(
    message: String = "북마크 불러오는 중...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkLoadingIndicatorPreview() {
    MyApplicationTheme {
        BookmarkLoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkLoadingIndicatorCustomMessagePreview() {
    MyApplicationTheme {
        BookmarkLoadingIndicator(message = "데이터 로딩 중...")
    }
} 