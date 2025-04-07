package com.example.kakaoimagevideosearch.presentation.bookmark.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme

/**
 * 북마크가 없을 때 표시하는 플레이스홀더 컴포넌트
 * 
 * @param message 표시할 메시지
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun EmptyBookmarksPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "북마크 화면",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyBookmarksPlaceholderInitialPreview() {
    MyApplicationTheme {
        EmptyBookmarksPlaceholder(
            message = "검색 화면에서 하트 아이콘을 눌러 좋아하는 콘텐츠를 북마크에 저장하세요"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyBookmarksPlaceholderEmptyPreview() {
    MyApplicationTheme {
        EmptyBookmarksPlaceholder(
            message = "저장된 북마크가 없습니다."
        )
    }
} 