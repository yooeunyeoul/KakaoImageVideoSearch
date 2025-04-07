package com.example.kakaoimagevideosearch.presentation.search

import android.util.Log
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hilt.AssistedViewModelFactory
import com.airbnb.mvrx.hilt.hiltMavericksViewModelFactory
import com.example.kakaoimagevideosearch.base.BaseMviViewModel
import com.example.kakaoimagevideosearch.base.BaseState
import com.example.kakaoimagevideosearch.base.BaseUiEffect
import com.example.kakaoimagevideosearch.base.BaseUiEvent
import com.example.kakaoimagevideosearch.domain.model.ApiError
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.repository.SearchRepository
import com.example.kakaoimagevideosearch.domain.repository.BookmarkRepository
import com.example.kakaoimagevideosearch.utils.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val searchTextInput: String = "", // 입력 중인 검색어 (debounce 적용 전)
    // 검색 Flow 설정 작업의 상태 (초기 페이징 로드 상태가 아님)
    val searchSetupAsync: Async<Unit> = Uninitialized,
    // PagingData를 방출하는 Flow 자체를 상태로 관리
    val pagingDataFlow: Flow<PagingData<SearchResult>> = emptyFlow(), // 초기값으로 emptyFlow 사용
    // Flow 설정 실패 시의 에러 정보 (선택적)
    val setupError: Throwable? = null,
    val searchCounter: Int = 0,
    val favoriteStatusFlow: Flow<List<SearchResult>> = emptyFlow()
) : BaseState {
    // BaseState에 apiError가 있다면 nullable로 변경하거나 제거 고려
    override val apiError: ApiError?
        get() = (setupError as? ApiException)?.let { ApiError.fromCode(it.code) }
}

sealed class SearchEvent : BaseUiEvent {
    data class Search(val query: String) : SearchEvent()
    data class UpdateSearchInput(val input: String) : SearchEvent() // 검색어 입력 이벤트 추가
    data object Refresh : SearchEvent()
    data class ToggleFavorite(val resultId: String) : SearchEvent() // 북마크 토글 이벤트 추가
    data class LoadFavoriteStatus(val query: String) : SearchEvent() // 북마크 상태 로드 이벤트 추가
}

sealed class SearchEffect : BaseUiEffect {
    data class ShowError(val message: String) : SearchEffect()
}

class SearchViewModel @AssistedInject constructor(
    @Assisted initialState: SearchState,
    private val searchRepository: SearchRepository,
) : BaseMviViewModel<SearchState, SearchEvent, SearchEffect>(initialState) {

    companion object : MavericksViewModelFactory<SearchViewModel, SearchState> by hiltMavericksViewModelFactory() {
        private const val TAG = "SearchViewModel"
        private const val SEARCH_DEBOUNCE_MS = 500L // 검색 디바운스 시간 (500ms)
    }

    init {
        Log.d(TAG, "SearchViewModel 초기화")
        
        // 검색어 입력 텍스트 상태에 debounce 적용하여 자동 검색
        onEach(SearchState::searchTextInput) { input ->
            if (input.isNotBlank()) {
                // debounce 적용하여 쿼리 업데이트
                debounceSearchInput(input)
            }
        }
        
        // 쿼리가 변경되면 실제 검색 실행
        onEach(SearchState::query) { query ->
            if (query.isNotBlank()) {
                executeSearch(query)
                onEvent(SearchEvent.LoadFavoriteStatus(query))
            } else {
                // 쿼리가 비어있으면 페이징 데이터 클리어
                setState { 
                    copy(
                        pagingDataFlow = emptyFlow(), 
                        searchSetupAsync = Uninitialized, 
                        setupError = null
                    ) 
                }
            }
        }
    }
    
    private fun debounceSearchInput(input: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(SEARCH_DEBOUNCE_MS)
            withState { state ->
                // 현재 입력과 디바운스 완료 후의 입력이 같은 경우에만 검색 실행
                if (state.searchTextInput == input) {
                    setState { copy(query = input) }
                }
            }
        }
    }

    private fun executeSearch(query: String) {
        // 먼저 로딩 상태로 변경하고 이전 페이징 데이터 초기화
        setState { 
            copy(
                searchSetupAsync = Loading(), 
                setupError = null,
                pagingDataFlow = emptyFlow(),
                searchCounter = searchCounter + 1
            ) 
        }

        try {
            // Repository에서 Flow를 가져와서 State에 설정
            // cachedIn은 ViewModelScope 내에서 호출되어야 Flow가 유지됨
            val newPagingFlow = searchRepository.getSearchResults(query)
                .cachedIn(viewModelScope) // ViewModelScope에서 캐싱
                .distinctUntilChanged() // 중복 발행 방지를 위해 추가
            
            setState {
                copy(
                    pagingDataFlow = newPagingFlow,
                    searchSetupAsync = Success(Unit) // Flow 설정 성공
                )
            }
            // ViewModel에서 직접 collect 하지 않음! UI 레이어에서 처리.

        } catch (e: Exception) {
            // Flow 생성/설정 자체에서 에러 발생 시 처리
            Log.e(TAG, "페이징 데이터 Flow 설정 실패: 쿼리='$query'", e)
            setState {
                copy(
                    searchSetupAsync = Fail(e),
                    setupError = e,
                    pagingDataFlow = emptyFlow() // 에러 시 빈 플로우로 초기화
                )
            }
            sendEffect(SearchEffect.ShowError(e.message ?: "검색 준비 중 오류가 발생했습니다."))
        }
    }
    
    override fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> {
                Log.d(TAG, "이벤트 처리: Search 이벤트 (쿼리='${event.query}')")
                setState { 
                    copy(
                        searchTextInput = event.query,
                        query = event.query
                    ) 
                }
            }
            is SearchEvent.UpdateSearchInput -> {
                Log.d(TAG, "이벤트 처리: UpdateSearchInput 이벤트 (입력='${event.input}')")
                setState { copy(searchTextInput = event.input) }
            }
            is SearchEvent.Refresh -> {
                withState { state ->
                    Log.d(TAG, "이벤트 처리: Refresh 이벤트 (현재 쿼리='${state.query}')")
                    if (state.query.isNotBlank()) {
                        executeSearch(state.query)
                    } else {
                        Log.d(TAG, "쿼리가 비어있어 Refresh 무시")
                    }
                }
            }
            is SearchEvent.ToggleFavorite -> {
                Log.d(TAG, "이벤트 처리: ToggleFavorite 이벤트 (resultId='${event.resultId}')")
                toggleFavorite(event.resultId)
            }
            is SearchEvent.LoadFavoriteStatus -> {
                Log.d(TAG, "이벤트 처리: LoadFavoriteStatus 이벤트 (쿼리='${event.query}')")
                loadFavoriteStatus(event.query)
            }
        }
    }

    /**
     * 검색 결과 ID로 좋아요 상태 토글 (내부 메서드로 변경)
     */
    private fun toggleFavorite(resultId: String) {
        viewModelScope.launch {
            try {
                // 검색 Repository에서 좋아요 상태 토글
                searchRepository.toggleFavorite(resultId)
                Log.d(TAG, "좋아요 상태 변경 요청 완료: $resultId")
                
                // 현재 쿼리에 대한 좋아요 상태 다시 로드
                withState { state ->
                    if (state.query.isNotBlank()) {
                        loadFavoriteStatus(state.query)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "좋아요 토글 중 오류 발생", e)
                sendEffect(SearchEffect.ShowError("좋아요 상태 변경 중 오류가 발생했습니다."))
            }
        }
    }

    /**
     * 특정 검색어에 대한 좋아요 상태 변경을 관찰하는 Flow 로드 (내부 메서드로 변경)
     */
    private fun loadFavoriteStatus(query: String) {
        if (query.isBlank()) return
        
        val favoriteStatusFlow = searchRepository.getCachedSearchResultsFlow(query)
            .map { entityList -> entityList.map { it } }
            
        setState { copy(favoriteStatusFlow = favoriteStatusFlow) }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<SearchViewModel, SearchState> {
        override fun create(state: SearchState): SearchViewModel
    }
} 