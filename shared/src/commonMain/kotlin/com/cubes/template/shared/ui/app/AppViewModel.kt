package com.cubes.template.shared.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.data.local.LocalDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AppStartupState {
    data object Loading : AppStartupState
    data object Error : AppStartupState
    data class Ready(val navigator: AppScreenNavigatorImpl) : AppStartupState
}

/**
 * Decides the first screen while the splash is visible
 * (session check, onboarding flag, forced update...).
 */
class AppViewModel(
    private val localDataSource: LocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    val state: StateFlow<AppStartupState> = _state

    init {
        start()
    }

    fun retry() {
        _state.value = AppStartupState.Loading
        start()
    }

    private fun start() {
        viewModelScope.launch {
            try {
                delay(500)

                // Place startup logic here, e.g.:
                // val loggedIn = authManager.userLoggedIn()
                // val nextScreen = if (loggedIn) AppScreen.Main() else AppScreen.Intro(...)
                val nextScreen = AppScreen.Main()
                localDataSource.setOnboardingDone(true)

                _state.value = AppStartupState.Ready(AppScreenNavigatorImpl(listOf(nextScreen)))
            } catch (e: Exception) {
                _state.value = AppStartupState.Error
            }
        }
    }
}
