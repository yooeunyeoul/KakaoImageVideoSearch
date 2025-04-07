package com.example.kakaoimagevideosearch.presentation.bookmark.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
 * 북마크 로딩 실패 시 오류를 표시하는 컴포넌트
 * 
 * @param errorMessage 표시할 오류 메시지
 * @param onRetry 재시도 버튼 클릭 시 호출할 콜백
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun BookmarkErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "북마크 로딩 실패: $errorMessage",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetry
        ) {
            Text("다시 시도")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkErrorViewPreview() {
    MyApplicationTheme {
        BookmarkErrorView(
            errorMessage = "네트워크 오류",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkErrorViewLongMessagePreview() {
    MyApplicationTheme {
        BookmarkErrorView(
            errorMessage = "데이터베이스 접근 오류가 발생했습니다. 앱을 재시작하거나 다시 시도해주세요.",
            onRetry = {}
        )
    }
} 