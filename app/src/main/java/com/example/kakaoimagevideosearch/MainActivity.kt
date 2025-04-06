@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.kakaoimagevideosearch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import com.example.kakaoimagevideosearch.presentation.search.SearchEffect
import com.example.kakaoimagevideosearch.presentation.search.SearchEvent
import com.example.kakaoimagevideosearch.presentation.search.SearchViewModel
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

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
                    SearchScreen()
//                    SearchScreen(
//                        onNavigateToBookmark = {
//                            // TODO: Implement navigation to bookmark screen
//                        }
//                    )
                }
            }
        }
    }
}




@Composable
fun SearchScreen(
    // MvRx ViewModel 주입
    viewModel: SearchViewModel = mavericksViewModel()
) {
    // ViewModel의 State 구독
    val state by viewModel.collectAsState()
    // PagingData Flow를 LazyPagingItems로 변환
    val lazyPagingItems: LazyPagingItems<SearchResult> = state.pagingDataFlow.collectAsLazyPagingItems()
    // 키보드 포커스 관리를 위한 객체
    val focusManager = LocalFocusManager.current
    // 스낵바 상태 관리
    // 검색어 입력 상태 관리
    val scaffoldState = rememberBottomSheetScaffoldState()
    var searchQuery by remember { mutableStateOf("") }

    // ViewModel의 Side Effect 처리 (에러 메시지 표시 등)
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchEffect.ShowError -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
    // LazyListState 생성 및 기억
    val listState = rememberLazyListState() // <--- 여기! listState 선언 및 생성

    // 스크롤 상태 로깅 (LaunchedEffect 또는 derivedStateOf 사용 고려)
    // LaunchedEffect는 키가 변경될 때마다 재시작, derivedStateOf는 계산 중 읽는 상태가 변경될 때 재계산
    // 여기서는 스크롤 시마다 로그가 필요하므로 snapshotFlow 사용하는 것이 더 적합할 수 있음
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                Log.d("SearchScreen", "First Visible Item Index: $index")
            }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            .collect { visibleIndices ->
                Log.d("SearchScreen", "Visible Item Indices: $visibleIndices")
            }
    }

    Scaffold() { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold의 내부 패딩 적용
                .padding(horizontal = 16.dp) // 좌우 패딩 추가
        ) {
            // 검색 입력 필드 및 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("검색어를 입력하세요") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.onEvent(SearchEvent.Search(searchQuery))
                            focusManager.clearFocus() // 검색 실행 후 키보드 숨김
                        }
                    })
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.onEvent(SearchEvent.Search(searchQuery))
                            focusManager.clearFocus() // 검색 실행 후 키보드 숨김
                        }
                    },
                    // ViewModel의 searchSetupAsync 상태가 Loading일 때 버튼 비활성화 (선택적)
                    enabled = state.searchSetupAsync !is Loading
                ) {
                    // ViewModel의 searchSetupAsync 상태가 Loading일 때 로딩 인디케이터 표시 (선택적)
                    if (state.searchSetupAsync is Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("검색")
                    }
                }
            }

            // ViewModel의 Flow 설정 단계 에러 표시 (선택적)
            if (state.searchSetupAsync is Fail) {
                Text(
                    text = "검색 Flow 설정 오류: ${state.setupError?.message ?: "알 수 없는 오류"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }


            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp) // 아이템 간 간격
            ) {
                // Paging 아이템 표시
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { it.id },
                    contentType = lazyPagingItems.itemContentType { "MyPagingItems" }
                ) { index ->
                    val item = lazyPagingItems[index]
                    if (item != null) {
                        SearchResultItem(item = item)
                    }
                }

                // --- Paging LoadState 처리 ---
                val loadState = lazyPagingItems.loadState

                // 1. 초기 로드(Refresh) 상태 처리
                when (val refreshState = loadState.refresh) {
                    is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            ErrorItem(
                                message = refreshState.error.message ?: "데이터 로딩 실패",
                                onRetry = { lazyPagingItems.retry() }
                            )
                        }
                    }
                    is LoadState.NotLoading -> {
                        // 초기 로드 성공 후 데이터가 없을 때
                        if (lazyPagingItems.itemCount == 0) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("검색 결과가 없습니다.")
                                }
                            }
                        }
                    }
                }

                // 2. 추가 로드(Append) 상태 처리
                when (val appendState = loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            ErrorItem(
                                message = appendState.error.message ?: "추가 로딩 실패",
                                onRetry = { lazyPagingItems.retry() },
                                isCompact = true // 간단한 재시도 버튼 표시
                            )
                        }
                    }
                    is LoadState.NotLoading -> {
                        // 추가 로드할 데이터가 없거나 로드 완료
                    }
                }
            }
        }
    }
}

// 개별 검색 결과 아이템 Composable
@Composable
fun SearchResultItem(item: SearchResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // 실제 이미지 로딩 라이브러리(Coil, Glide 등) 사용 필요
            // 예시: Coil 사용 시
            // AsyncImage(
            //     model = item.thumbnailUrl,
            //     contentDescription = item.title,
            //     modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
            //     contentScale = ContentScale.Crop
            // )
            Box(modifier = Modifier.size(80.dp).align(Alignment.CenterVertically)) { // Placeholder for image
                Text(if (item.type == SearchResultType.IMAGE) "IMG" else "VID", modifier=Modifier.align(Alignment.Center))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title.ifBlank { "(제목 없음)" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Text(
                    text = "${item.source} | ${item.datetime}", // 날짜 형식 변환 필요 시 추가
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = if (item.type == SearchResultType.IMAGE) "[이미지]" else "[비디오]",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// 에러 및 재시도 표시 Composable
@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false // 추가 로딩 에러 시 간단히 표시할지 여부
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 8.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onRetry) {
                Text("재시도")
            }
        }
    }
}