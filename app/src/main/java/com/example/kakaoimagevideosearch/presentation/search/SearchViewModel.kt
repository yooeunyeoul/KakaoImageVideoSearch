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
import com.example.kakaoimagevideosearch.utils.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

data class SearchState(
    val query: String = "",
    // 검색 Flow 설정 작업의 상태 (초기 페이징 로드 상태가 아님)
    val searchSetupAsync: Async<Unit> = Uninitialized,
    // PagingData를 방출하는 Flow 자체를 상태로 관리
    val pagingDataFlow: Flow<PagingData<SearchResult>> = emptyFlow(), // 초기값으로 emptyFlow 사용
    // Flow 설정 실패 시의 에러 정보 (선택적)
    val setupError: Throwable? = null
) : BaseState {
    // BaseState에 apiError가 있다면 nullable로 변경하거나 제거 고려
    override val apiError: ApiError?
        get() = (setupError as? ApiException)?.let { ApiError.fromCode(it.code) }
}

sealed class SearchEvent : BaseUiEvent {
    data class Search(val query: String) : SearchEvent()
    object Refresh : SearchEvent()
}

sealed class SearchEffect : BaseUiEffect {
    data class ShowError(val message: String) : SearchEffect()
}

class SearchViewModel @AssistedInject constructor(
    @Assisted initialState: SearchState,
    private val searchRepository: SearchRepository
) : BaseMviViewModel<SearchState, SearchEvent, SearchEffect>(initialState) {

    companion object : MavericksViewModelFactory<SearchViewModel, SearchState> by hiltMavericksViewModelFactory() {
        private const val TAG = "SearchViewModel"
    }

    init {
        Log.d(TAG, "SearchViewModel 초기화")
        // query가 변경되면 자동으로 검색 실행 (Debounce 등을 추가하면 더 좋음)
        onEach(SearchState::query) { query ->
            Log.d(TAG, "쿼리 변경 감지: '$query'")
            if (query.isNotBlank()) {
                executeSearch(query)
            } else {
                // 쿼리가 비어있으면 페이징 데이터 클리어
                Log.d(TAG, "쿼리가 비어있어 페이징 데이터 초기화")
                setState { copy(pagingDataFlow = emptyFlow(), searchSetupAsync = Uninitialized, setupError = null) }
            }
        }
    }

    private fun executeSearch(query: String) {
        Log.d(TAG, "executeSearch 호출: 쿼리='$query'")
        
        // 검색 시작 시 상태 업데이트 (로딩 상태 표시 - Flow 설정 단계)
        setState { copy(searchSetupAsync = Loading(), setupError = null) }

        try {
            // Repository에서 Flow를 가져와서 State에 설정
            // cachedIn은 ViewModelScope 내에서 호출되어야 Flow가 유지됨
            Log.d(TAG, "페이징 데이터 Flow 요청 시작: 쿼리='$query'")
            val newPagingFlow = searchRepository.getSearchResults(query)
                .cachedIn(viewModelScope) // ViewModelScope에서 캐싱
                .distinctUntilChanged() // 중복 발행 방지를 위해 추가
            
            Log.d(TAG, "페이징 데이터 Flow 설정 완료: 쿼리='$query'")

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
                setState { copy(query = event.query) }
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
        }
    }

    // 메모리 상태 추적을 위한 추가 메서드
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "SearchViewModel onCleared 호출됨 (ViewModel 소멸)")
    }

    /**
     * 검색 결과 ID로 좋아요 상태 토글
     */
    fun toggleFavorite(resultId: String) {
        viewModelScope.launch {
            searchRepository.toggleFavorite(resultId)
            // 필요하면 UI 갱신을 위해 State를 업데이트할 수도 있음
        }
    }

    /**
     * 특정 검색어에 대한 좋아요 상태 변경을 관찰하는 Flow
     * UI에서 이 Flow를 구독하여 좋아요 상태 변경을 실시간으로 반영할 수 있음
     */
    fun getFavoriteStatusFlow(query: String): Flow<List<SearchResult>> {
        return searchRepository.getCachedSearchResultsFlow(query)
            .map { entityList -> entityList.map { it } }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<SearchViewModel, SearchState> {
        override fun create(state: SearchState): SearchViewModel
    }
} 