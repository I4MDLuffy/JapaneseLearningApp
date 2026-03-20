package com.example.personalproject.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<S : BaseState, A : BaseAction>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    abstract fun dispatchAction(action: A)

    protected fun updateState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun <T> execute(
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null,
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { block() }
                onSuccess(result)
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
}
