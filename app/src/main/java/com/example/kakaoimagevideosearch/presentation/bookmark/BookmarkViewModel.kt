package com.example.kakaoimagevideosearch.presentation.bookmark

import com.airbnb.mvrx.Async
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
import com.example.kakaoimagevideosearch.domain.repository.BookmarkRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class BookmarkState(
    val bookmarksAsync: Async<List<SearchResult>> = Uninitialized,
    val bookmarkCount: Int = 0,
    val error: String? = null
) : BaseState {
    override val apiError: ApiError? = null
}

sealed class BookmarkEvent : BaseUiEvent {
    data object LoadBookmarks : BookmarkEvent()
}

sealed class BookmarkEffect : BaseUiEffect {
    data class ShowMessage(val message: String) : BookmarkEffect()
}

class BookmarkViewModel @AssistedInject constructor(
    @Assisted initialState: BookmarkState,
    private val bookmarkRepository: BookmarkRepository
) : BaseMviViewModel<BookmarkState, BookmarkEvent, BookmarkEffect>(initialState) {

    companion object : MavericksViewModelFactory<BookmarkViewModel, BookmarkState> by hiltMavericksViewModelFactory()

    init {
        loadBookmarks()
        
        viewModelScope.launch {
            bookmarkRepository.getBookmarkCount().collectLatest { count ->
                setState { copy(bookmarkCount = count) }
            }
        }
    }
    
    private fun loadBookmarks() {
        setState { copy(bookmarksAsync = Loading()) }
        
        viewModelScope.launch {
            try {
                bookmarkRepository.getAllBookmarks().collectLatest { bookmarks ->
                    setState { 
                        copy(
                            bookmarksAsync = Success(bookmarks),
                            error = null
                        ) 
                    }
                }
            } catch (e: Exception) {
                setState { 
                    copy(
                        bookmarksAsync = com.airbnb.mvrx.Fail(e),
                        error = e.message
                    ) 
                }
                sendEffect(BookmarkEffect.ShowMessage("북마크 목록을 불러오는 중 오류가 발생했습니다."))
            }
        }
    }
    
    override fun onEvent(event: BookmarkEvent) {
        when (event) {
            is BookmarkEvent.LoadBookmarks -> loadBookmarks()
        }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<BookmarkViewModel, BookmarkState> {
        override fun create(state: BookmarkState): BookmarkViewModel
    }
} 