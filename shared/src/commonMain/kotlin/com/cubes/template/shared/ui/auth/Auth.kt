package com.cubes.template.shared.ui.auth

object Auth {

    const val PASSWORD_MIN_LENGTH = 8

    enum class Mode { LOGIN, REGISTER }

    data class ViewData(
        val mode: Mode = Mode.LOGIN,
        val canGoBack: Boolean = false,
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val usernameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val isLoading: Boolean = false,
        val listener: AuthViewListener,
    )
}

interface AuthViewListener {
    fun onModeSelected(mode: Auth.Mode)
    fun onUsernameChanged(username: String)
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onSubmitClick()
    fun onSkipClick()
    fun onBackClick()
}
