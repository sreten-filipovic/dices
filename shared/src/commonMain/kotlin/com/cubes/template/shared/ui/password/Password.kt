package com.cubes.template.shared.ui.password

object Password {

    const val PASSWORD_MIN_LENGTH = 8
    val CODE_LENGTH = 6..10

    enum class Mode { CHANGE, FORGOT }

    enum class Step { EMAIL, CODE, NEW_PASSWORD }

    data class ViewData(
        val mode: Mode,
        val step: Step,
        val email: String = "",
        val emailError: String? = null,
        val code: String = "",
        val codeError: String? = null,
        val isCodeVerified: Boolean = false,
        val currentPassword: String = "",
        val currentPasswordError: String? = null,
        val newPassword: String = "",
        val newPasswordError: String? = null,
        val confirmPassword: String = "",
        val confirmPasswordError: String? = null,
        val isLoading: Boolean = false,
        val listener: PasswordViewListener,
    ) {
        val showsCurrentPassword: Boolean get() = mode == Mode.CHANGE
        val showsEmail: Boolean get() = step == Step.EMAIL
        val showsCode: Boolean get() = step == Step.CODE
        val showsNewPassword: Boolean get() = step != Step.EMAIL
    }
}

interface PasswordViewListener {
    fun onEmailChanged(email: String)
    fun onCodeChanged(code: String)
    fun onCurrentPasswordChanged(password: String)
    fun onNewPasswordChanged(password: String)
    fun onConfirmPasswordChanged(password: String)
    fun onSubmitClick()
    fun onResendCodeClick()
    fun onBackClick()
}
