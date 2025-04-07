package com.example.kakaoimagevideosearch.presentation.search.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme

/**
 * 검색 Flow 설정 오류를 표시하는 컴포넌트
 *
 * @param errorMessage 표시할 오류 메시지
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun SearchErrorView(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "검색 Flow 설정 오류: $errorMessage",
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SearchErrorViewPreview() {
    MyApplicationTheme {
        SearchErrorView(
            errorMessage = "네트워크 오류가 발생했습니다"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchErrorViewLongMessagePreview() {
    MyApplicationTheme {
        SearchErrorView(
            errorMessage = "서버에 접속할 수 없습니다. 네트워크 연결을 확인하고 다시 시도해주세요."
        )
    }
} 