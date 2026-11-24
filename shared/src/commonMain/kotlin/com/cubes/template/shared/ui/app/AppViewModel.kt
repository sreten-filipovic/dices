package com.cubes.template.shared.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.data.auth.AuthRepository
import com.cubes.template.shared.data.local.LocalDataSource
import com.cubes.template.shared.data.session.UserSession
import com.cubes.template.shared.ui.splash.SplashAnimation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AppStartupState {
    data object Loading : AppStartupState
    data object Error : AppStartupState
    data class Ready(val navigator: AppScreenNavigatorImpl) : AppStartupState
}

class AppViewModel(
    private val localDataSource: LocalDataSource,
    private val userSession: UserSession,
    private val authRepository: AuthRepository,
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
                coroutineScope {
                    launch { delay(SplashAnimation.totalMillis) }
                    val user = authRepository.restoreSession(userSession.currentUser.value)
                    if (user != null) userSession.signIn(user) else userSession.signOut()
                }

                val nextScreen = if (userSession.isLoggedIn.value) AppScreen.Main() else AppScreen.Auth()
                localDataSource.setOnboardingDone(true)

                _state.value = AppStartupState.Ready(AppScreenNavigatorImpl(listOf(nextScreen)))
            } catch (e: Exception) {
                _state.value = AppStartupState.Error
            }
        }
    }
}
