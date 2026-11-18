package com.cubes.template.shared.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.error_occurred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SimpleUiState<out T> {
    data object Loading : SimpleUiState<Nothing>()
    data class Success<T>(val data: T) : SimpleUiState<T>()
    data class Error(val message: String) : SimpleUiState<Nothing>()
}

abstract class SimpleBaseViewModel<T : Any>(
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimpleUiState<T>>(SimpleUiState.Loading)
    val uiState = _uiState.asStateFlow()

    protected abstract suspend fun loadData(): Result<T>

    fun load() = viewModelScope.launch {
        handleResult(loadData())
    }

    fun reload() = viewModelScope.launch {
        _uiState.value = SimpleUiState.Loading
        handleResult(loadData())
    }

    private fun handleResult(result: Result<T>) = when (result) {
        is Result.Success -> _uiState.value = SimpleUiState.Success(result.data)
        is Result.Error -> _uiState.value = SimpleUiState.Error(
            result.ex.message ?: stringProvider.getString(Res.string.error_occurred)
        )
    }
}
