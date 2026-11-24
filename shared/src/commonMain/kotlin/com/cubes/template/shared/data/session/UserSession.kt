package com.cubes.template.shared.data.session

import com.cubes.template.shared.data.auth.User
import com.cubes.template.shared.data.local.LocalDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSession(private val localDataSource: LocalDataSource) {

    private val _currentUser = MutableStateFlow(localDataSource.getUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(_currentUser.value != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun signIn(user: User) {
        localDataSource.saveUser(user)
        _currentUser.value = user
        _isLoggedIn.value = true
    }

    fun signOut() {
        localDataSource.clearUser()
        _currentUser.value = null
        _isLoggedIn.value = false
    }
}
