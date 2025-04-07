@file:OptIn(ExperimentalMaterial3Api::class) // 중복 제거

package com.example.kakaoimagevideosearch

// rememberBottomSheetScaffoldState 제거 (SnackbarHostState 직접 사용)
// itemContentType, itemKey는 items 확장 함수에서 람다로 사용되므로 제거
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import com.example.kakaoimagevideosearch.presentation.bookmark.BookmarkScreen
import com.example.kakaoimagevideosearch.presentation.search.SearchEffect
import com.example.kakaoimagevideosearch.presentation.search.SearchEvent
import com.example.kakaoimagevideosearch.presentation.search.SearchViewModel
import com.example.kakaoimagevideosearch.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 탭 바
        TabRow(
            selectedTabIndex = pagerState.currentPage,
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("검색") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("북마크") }
            )
        }
        
        // 페이저
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> SearchScreen()
                1 -> BookmarkScreen()
            }
        }
        
        // 페이지 인디케이터
        Row(
            Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                }
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = mavericksViewModel()
) {
    val state by viewModel.collectAsState()
    val lazyPagingItems: LazyPagingItems<SearchResult> = state.pagingDataFlow.collectAsLazyPagingItems()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() } // SnackbarHostState 사용
    val gridState = rememberLazyGridState() // LazyGridState 사용

    // 좋아요 상태 Flow 구독
    val favoriteStatuses by remember(state.query) { // query를 키로 사용
        if (state.query.isNotBlank()) {
            viewModel.getFavoriteStatusFlow(state.query)
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    // Side Effect 처리
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // 스크롤 상태 로깅 (Optional)
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                Log.d("SearchScreen", "First Visible Item Index: $index")
            }
    }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.map { it.index } }
            .distinctUntilChanged()
            .collect { visibleIndices ->
                Log.d("SearchScreen", "Visible Item Indices: $visibleIndices")
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) } // SnackbarHost 추가
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 검색 UI - 검색 버튼 제거
            OutlinedTextField(
                value = state.searchTextInput,
                onValueChange = { 
                    // 텍스트 입력 변경 시 UpdateSearchInput 이벤트 전송
                    viewModel.onEvent(SearchEvent.UpdateSearchInput(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("검색어를 입력하세요") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    // 키보드 검색 버튼 클릭 시 포커스만 제거
                    focusManager.clearFocus()
                })
            )

            // Flow 설정 에러 Text
            if (state.searchSetupAsync is Fail) {
                Text(
                    text = "검색 Flow 설정 오류: ${state.setupError?.message ?: "알 수 없는 오류"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 로딩 상태 표시
            if (state.searchSetupAsync is Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            // LazyVerticalGrid 사용
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2), // 2열
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LoadState 가져오기
                val loadState = lazyPagingItems.loadState
                val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(2) }
                
                // 앱 시작 초기 상태 - 검색어가 없고 결과도 없는 경우 안내 표시
                if (state.query.isBlank() && lazyPagingItems.itemCount == 0) {
                    // 초기 안내 메시지 표시 (로딩 중일 때도 이 메시지 표시)
                    item(span = span, key = "Initial Guide") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "이미지와 비디오를 검색해보세요",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "상단 검색창에 검색어를 입력하여 원하는 콘텐츠를 찾아보세요",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    // 일반 검색 결과 항목들
                    items(
                        count = lazyPagingItems.itemCount,
                        key = { index -> lazyPagingItems.peek(index)?.id ?: index },
                    ) { index ->
                        val item = lazyPagingItems[index]
                        if (item != null) {
                            // items 람다 내에서 좋아요 상태 찾기
                            val isFavorite = favoriteStatuses.find { it.id == item.id }?.isFavorite
                                ?: item.isFavorite // 기본값 사용

                            // Grid 아이템 표시
                            GridSearchResultItem(
                                item = item,
                                isFavorite = isFavorite, // 계산된 좋아요 상태 전달
                                onFavoriteClick = { viewModel.toggleFavorite(item.id) }
                            )
                        } else {
                            // Placeholder (현재 설정에서는 거의 호출되지 않음)
                            Box(modifier = Modifier.aspectRatio(1f).background(Color.Gray))
                        }
                    }

                    // 검색어가 있는 경우에만 로딩 및 에러 표시
                    if (state.query.isNotBlank()) {
                        // Refresh 상태
                        when (val refreshState = loadState.refresh) {
                            is LoadState.Loading -> item(span = span, key = "Refresh Loading") {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { 
                                    CircularProgressIndicator() 
                                }
                            }
                            is LoadState.Error -> item(span = span, key = "Refresh Error") {
                                ErrorItem(message = refreshState.error.message ?: "데이터 로딩 실패", onRetry = { lazyPagingItems.retry() })
                            }
                            is LoadState.NotLoading -> {
                                if (lazyPagingItems.itemCount == 0 && refreshState.endOfPaginationReached) {
                                    item(span = span, key = "Empty Result") {
                                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { 
                                            Text("검색 결과가 없습니다.") 
                                        }
                                    }
                                }
                            }
                        }

                        // Append 상태
                        when (val appendState = loadState.append) {
                            is LoadState.Loading -> item(span = span, key = "Append Loading") {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) { 
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp)) 
                                }
                            }
                            is LoadState.Error -> item(span = span, key = "Append Error") {
                                ErrorItem(message = appendState.error.message ?: "추가 로딩 실패", onRetry = { lazyPagingItems.retry() }, isCompact = true)
                            }
                            is LoadState.NotLoading -> { /* Append 완료 시 특별히 표시할 내용 없음 */ }
                        }
                    }
                }
            }
        }
    }
}

// 그리드 아이템 Composable (크기 안정화, isFavorite 파라미터 사용)
@Composable
fun GridSearchResultItem(
    item: SearchResult,
    isFavorite: Boolean, // 좋아요 상태 받기
    onFavoriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // 정사각형 비율 유지 -> 크기 안정화
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) // 아이템 영역 확인용
    ) {
        // 이미지 표시 (Coil 사용)
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = item.title, // 접근성을 위한 설명
            contentScale = ContentScale.Crop, // 비율에 맞게 이미지 자르기
            modifier = Modifier.fillMaxSize()
            // Coil RequestBuilder를 사용하여 placeholder, error 이미지 설정 권장
        )

        // --- 오버레이 UI들 ---
        // 타입 표시 (좌측 하단)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)) // 배경 약간 진하게
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (item.type == SearchResultType.IMAGE) "IMG" else "VID",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall // 작은 폰트 스타일
            )
        }

        // 좋아요 버튼 (우측 하단)
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp) // 버튼 주변 여백
                .size(32.dp)   // 버튼 크기
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50)) // 원형 배경
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "좋아요", // 접근성
                // 좋아요 상태에 따라 색상 변경
                tint = if (isFavorite) Color.Red else Color.White,
                modifier = Modifier.size(18.dp) // 아이콘 크기
            )
        }
    }
}

// 에러 아이템 Composable
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
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onRetry) {
                Text("재시도")
            }
        }
    }
}
