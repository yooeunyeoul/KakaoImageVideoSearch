package com.example.kakaoimagevideosearch.presentation.search.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * 오류 상태를 표시하는 컴포넌트
 *
 * @param message 표시할 오류 메시지
 * @param onRetry 재시도 버튼 클릭 시 호출되는 콜백
 * @param modifier 컴포넌트의 수정자
 * @param isCompact 컴팩트 모드 여부 (간소화된 디자인)
 */
@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 8.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onRetry) {
                Text("재시도")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorItemPreview() {
    MyApplicationTheme {
        ErrorItem(
            message = "데이터 로딩 실패",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorItemCompactPreview() {
    MyApplicationTheme {
        ErrorItem(
            message = "추가 로딩 실패",
            onRetry = {},
            isCompact = true
        )
    }
} 