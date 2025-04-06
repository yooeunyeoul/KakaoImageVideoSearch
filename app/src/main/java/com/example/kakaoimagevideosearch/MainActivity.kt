package com.example.kakaoimagevideosearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.kakaoimagevideosearch.domain.model.ApiError
import com.example.kakaoimagevideosearch.domain.model.Item
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel = mavericksViewModel()
) {
    val state by viewModel.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val itemsAsync = state.itemsAsync) {
            is Success -> {
                LazyColumn {
                    items(itemsAsync()) { item ->
                        ItemCard(item = item)
                    }
                }
            }
            is Fail -> {
                when (val apiError = state.apiError) {
                    is ApiError.NetworkError -> {
                        Text(
                            text = "네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is ApiError.ServerError -> {
                        Text(
                            text = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is ApiError.UnknownError -> {
                        Text(
                            text = "알 수 없는 오류가 발생했습니다.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    null -> {
                        Text(
                            text = state.error ?: "오류가 발생했습니다.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            is Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Uninitialized -> {
                // 초기 상태에서는 아무것도 표시하지 않음
            }
        }
    }
}

@Composable
fun ItemCard(item: Item) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}