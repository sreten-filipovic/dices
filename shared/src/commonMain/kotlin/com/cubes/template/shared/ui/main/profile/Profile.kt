package com.cubes.template.shared.ui.main.profile

import com.cubes.template.shared.data.auth.User

object Profile {

    data class ViewData(
        val user: User? = null,
        val usernameInput: String = "",
        val usernameError: String? = null,
        val isSavingUsername: Boolean = false,
        val isDeleting: Boolean = false,
        val listener: ProfileViewListener,
    ) {
        val isBusy: Boolean get() = isSavingUsername || isDeleting
        val canSaveUsername: Boolean
            get() = user != null && !isBusy && usernameInput.trim() != user.username
    }
}

interface ProfileViewListener {
    fun onLoginClick()
    fun onUsernameChanged(username: String)
    fun onSaveUsernameClick()
    fun onChangePasswordClick()
    fun onLogoutClick()
    fun onDeleteAccountClick()
}

interface ProfileScreenNavigator {
    fun showAuth()
    fun showChangePassword()
}
