package com.example.kakaoimagevideosearch.presentation.search.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * 검색은 했지만 결과가 없을 때 표시되는 컴포넌트
 * 
 * @param message 표시할 메시지
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun EmptySearchResultItem(
    message: String = "검색 결과가 없습니다.",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptySearchResultItemPreview() {
    MyApplicationTheme {
        EmptySearchResultItem()
    }
}

@Preview(showBackground = true)
@Composable
fun EmptySearchResultItemCustomMessagePreview() {
    MyApplicationTheme {
        EmptySearchResultItem(message = "해당 검색어에 맞는 결과를 찾을 수 없습니다.")
    }
} 