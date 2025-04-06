package com.example.myapplication.base

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.example.myapplication.domain.model.ApiError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface BaseUiEvent

interface BaseUiEffect

interface BaseState : MavericksState {
    val apiError: ApiError?
}

abstract class BaseMviViewModel<S : BaseState, E : BaseUiEvent, F : BaseUiEffect>(
    initialState: S
) : MavericksViewModel<S>(initialState) {

    private val _effect = Channel<F>()
    val effect = _effect.receiveAsFlow()

    protected fun sendEffect(effect: F) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    abstract fun onEvent(event: E)
}
