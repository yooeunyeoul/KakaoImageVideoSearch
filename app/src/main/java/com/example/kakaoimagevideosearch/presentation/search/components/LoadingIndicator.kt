package com.example.kakaoimagevideosearch.presentation.search.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme

/**
 * 로딩 인디케이터 컴포넌트
 * 
 * @param modifier 컴포넌트 수정자
 * @param size 로딩 인디케이터 크기
 * @param boxHeight 컨테이너 높이
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    boxHeight: Int = 0
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (boxHeight > 0) Modifier.height(boxHeight.dp)
                else Modifier.padding(vertical = 8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingIndicatorPreview() {
    MyApplicationTheme {
        LoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingIndicatorLargePreview() {
    MyApplicationTheme {
        LoadingIndicator(
            size = 48,
            boxHeight = 200
        )
    }
} 