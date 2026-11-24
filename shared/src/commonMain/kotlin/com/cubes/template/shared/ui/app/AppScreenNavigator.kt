package com.cubes.template.shared.ui.app

import com.cubes.template.shared.ui.password.Password
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface AppScreenNavigator {
    val backStack: StateFlow<List<AppScreen>>

    fun showMain()
    fun showAuth()
    fun showPassword(mode: Password.Mode, email: String = "")
    fun finishSignIn()
    fun back()
}

class AppScreenNavigatorImpl(
    initialBackStack: List<AppScreen>,
) : AppScreenNavigator {

    private val _backStack = MutableStateFlow(initialBackStack)
    override val backStack: StateFlow<List<AppScreen>> = _backStack

    override fun showMain() {
        _backStack.value = listOf(AppScreen.Main())
    }

    override fun showAuth() {
        _backStack.update { it + AppScreen.Auth(canGoBack = true) }
    }

    override fun showPassword(mode: Password.Mode, email: String) {
        _backStack.update { it + AppScreen.Password(mode, email) }
    }

    override fun finishSignIn() {
        _backStack.update { stack -> stack.filterIsInstance<AppScreen.Main>().ifEmpty { listOf(AppScreen.Main()) } }
    }

    override fun back() {
        _backStack.update { current ->
            if (current.size > 1) current.dropLast(1) else current
        }
    }
}
