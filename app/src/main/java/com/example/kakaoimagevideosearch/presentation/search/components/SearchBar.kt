package com.example.kakaoimagevideosearch.presentation.search.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme

/**
 * 검색 입력창 컴포넌트
 * 
 * @param searchText 현재 검색어 텍스트
 * @param onSearchTextChange 검색어 변경 시 호출되는 콜백
 * @param modifier 컴포넌트 수정자
 */
@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        placeholder = { Text("검색어를 입력하세요") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    MyApplicationTheme {
        SearchBar(
            searchText = "",
            onSearchTextChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarWithTextPreview() {
    MyApplicationTheme {
        SearchBar(
            searchText = "고양이",
            onSearchTextChange = {}
        )
    }
} 