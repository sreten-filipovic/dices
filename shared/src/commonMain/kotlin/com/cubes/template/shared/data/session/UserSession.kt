package com.cubes.template.shared.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the signed-in state of the player. Accounts do not exist yet, so the player is never
 * logged in; sign-in and sign-out will drive [isLoggedIn] once online multiplayer gets a backend.
 */
class UserSession {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
}
