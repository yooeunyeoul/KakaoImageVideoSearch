package com.example.myapplication

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hilt.AssistedViewModelFactory
import com.airbnb.mvrx.hilt.hiltMavericksViewModelFactory
import com.example.myapplication.base.BaseMviViewModel
import com.example.myapplication.base.BaseState
import com.example.myapplication.base.BaseUiEvent
import com.example.myapplication.base.BaseUiEffect
import com.example.myapplication.data.ItemRepository
import com.example.myapplication.domain.model.ApiError
import com.example.myapplication.domain.model.Item
import com.example.myapplication.utils.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

data class MainState(
    val itemsAsync: Async<List<Item>> = Uninitialized,
    override val apiError: ApiError? = null,
    val error: String? = null
) : BaseState

sealed class MainEvent : BaseUiEvent {
    object LoadItems : MainEvent()
}

sealed class MainEffect : BaseUiEffect

class MainViewModel @AssistedInject constructor(
    @Assisted initialState: MainState,
    private val itemRepository: ItemRepository
) : BaseMviViewModel<MainState, MainEvent, MainEffect>(initialState) {

    init {
        loadItems()
    }

    private fun loadItems() {
        itemRepository.getItems().execute { asyncResult ->
            when (asyncResult) {
                is Success -> copy(itemsAsync = asyncResult, apiError = null, error = null)
                is Fail -> {
                    val apiException = asyncResult.error as? ApiException
                    val apiError =
                        ApiError.fromCode(apiException?.code ?: ApiError.NETWORK_ERROR_CODE)
                    copy(itemsAsync = asyncResult, apiError = apiError, error = asyncResult.error.message)
                }
                else -> copy(itemsAsync = asyncResult)
            }
        }
    }

    override fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.LoadItems -> loadItems()
        }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<MainViewModel, MainState> {
        override fun create(state: MainState): MainViewModel
    }

    companion object : MavericksViewModelFactory<MainViewModel, MainState> by hiltMavericksViewModelFactory()
} 