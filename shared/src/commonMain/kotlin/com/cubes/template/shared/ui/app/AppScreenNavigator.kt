package com.cubes.template.shared.ui.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface AppScreenNavigator {
    val backStack: StateFlow<List<AppScreen>>

    fun showMain()
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

    override fun back() {
        _backStack.update { current ->
            if (current.size > 1) current.dropLast(1) else current
        }
    }
}
