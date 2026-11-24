package com.cubes.template.shared.ui.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.auth.AuthFailure
import com.cubes.template.shared.data.auth.AuthRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.data.session.UserSession
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.password_change_title
import com.cubes.template.shared.resources.password_changed_message
import com.cubes.template.shared.resources.password_changed_title
import com.cubes.template.shared.resources.password_code_invalid
import com.cubes.template.shared.resources.password_code_resent
import com.cubes.template.shared.resources.password_forgot_title
import com.cubes.template.shared.resources.password_mismatch
import com.cubes.template.shared.resources.password_reset_message
import com.cubes.template.shared.ui.auth.authErrorMessage
import com.cubes.template.shared.ui.auth.authFailureReason
import com.cubes.template.shared.ui.common.ValidationResult
import com.cubes.template.shared.ui.common.Validator
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordViewModel(
    mode: Password.Mode,
    email: String,
    private val authRepository: AuthRepository,
    private val userSession: UserSession,
    private val validator: Validator,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val onBack: () -> Unit,
    private val onSignedIn: () -> Unit,
) : ViewModel(), PasswordViewListener {

    private val _viewData = MutableStateFlow(
        Password.ViewData(
            mode = mode,
            step = if (mode == Password.Mode.CHANGE) Password.Step.NEW_PASSWORD else Password.Step.EMAIL,
            email = email,
            listener = this,
        )
    )
    val viewData: StateFlow<Password.ViewData> = _viewData.asStateFlow()

    private val title: String
        get() = stringProvider.getString(
            if (_viewData.value.mode == Password.Mode.CHANGE) Res.string.password_change_title
            else Res.string.password_forgot_title
        )

    override fun onEmailChanged(email: String) {
        _viewData.update { it.copy(email = email, emailError = null) }
    }

    override fun onCodeChanged(code: String) {
        _viewData.update { it.copy(code = code.filter(Char::isDigit), codeError = null) }
    }

    override fun onCurrentPasswordChanged(password: String) {
        _viewData.update { it.copy(currentPassword = password, currentPasswordError = null) }
    }

    override fun onNewPasswordChanged(password: String) {
        _viewData.update { it.copy(newPassword = password, newPasswordError = null) }
    }

    override fun onConfirmPasswordChanged(password: String) {
        _viewData.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    override fun onSubmitClick() {
        val data = _viewData.value
        if (data.isLoading) return
        when {
            data.mode == Password.Mode.CHANGE -> changePassword(data)
            data.step == Password.Step.EMAIL -> sendCode(data)
            else -> resetPassword(data)
        }
    }

    override fun onResendCodeClick() {
        val data = _viewData.value
        if (data.isLoading || data.isCodeVerified) return
        runLoading {
            when (val result = authRepository.sendPasswordResetCode(data.email.trim())) {
                is Result.Success -> showInfo(stringProvider.getString(Res.string.password_code_resent))
                is Result.Error -> showError(result.ex)
            }
        }
    }

    override fun onBackClick() {
        val data = _viewData.value
        if (data.isLoading) return
        if (data.isCodeVerified) {
            runLoading {
                authRepository.logout()
                onBack()
            }
        } else {
            onBack()
        }
    }

    private fun changePassword(data: Password.ViewData) {
        val user = userSession.currentUser.value ?: return
        val currentError = validator.validateRequired(data.currentPassword).errorOrNull()
        _viewData.update { it.copy(currentPasswordError = currentError) }
        if (!validateNewPassword(data) || currentError != null) return

        runLoading {
            when (val result = authRepository.changePassword(user, data.currentPassword, data.newPassword)) {
                is Result.Success -> {
                    popupManager.show(
                        PopupState.Info(
                            title = stringProvider.getString(Res.string.password_changed_title),
                            message = stringProvider.getString(Res.string.password_changed_message),
                        )
                    )
                    onBack()
                }
                is Result.Error -> showError(result.ex)
            }
        }
    }

    private fun sendCode(data: Password.ViewData) {
        val emailError = validator.validateEmail(data.email).errorOrNull()
        _viewData.update { it.copy(emailError = emailError) }
        if (emailError != null) return

        runLoading {
            when (val result = authRepository.sendPasswordResetCode(data.email.trim())) {
                is Result.Success -> _viewData.update { it.copy(step = Password.Step.CODE) }
                is Result.Error -> showError(result.ex)
            }
        }
    }

    private fun resetPassword(data: Password.ViewData) {
        val codeError = if (data.isCodeVerified || data.code.length in Password.CODE_LENGTH) null
        else stringProvider.getString(Res.string.password_code_invalid)
        _viewData.update { it.copy(codeError = codeError) }
        if (!validateNewPassword(data) || codeError != null) return

        runLoading {
            if (!data.isCodeVerified) {
                when (val verified = authRepository.verifyPasswordResetCode(data.email.trim(), data.code)) {
                    is Result.Success -> _viewData.update { it.copy(isCodeVerified = true) }
                    is Result.Error -> {
                        showError(verified.ex)
                        return@runLoading
                    }
                }
            }
            when (val result = authRepository.setNewPassword(data.newPassword)) {
                is Result.Success -> {
                    userSession.signIn(result.data)
                    popupManager.show(
                        PopupState.Info(
                            title = stringProvider.getString(Res.string.password_changed_title),
                            message = stringProvider.getString(Res.string.password_reset_message),
                        )
                    )
                    onSignedIn()
                }
                is Result.Error -> showError(result.ex)
            }
        }
    }

    private fun validateNewPassword(data: Password.ViewData): Boolean {
        val newError = validator.validatePassword(data.newPassword, Password.PASSWORD_MIN_LENGTH).errorOrNull()
        val confirmError = when {
            data.confirmPassword.isBlank() -> validator.validateRequired(data.confirmPassword).errorOrNull()
            data.confirmPassword != data.newPassword -> stringProvider.getString(Res.string.password_mismatch)
            else -> null
        }
        _viewData.update { it.copy(newPasswordError = newError, confirmPasswordError = confirmError) }
        return newError == null && confirmError == null
    }

    private fun showError(error: Exception) {
        val message = stringProvider.authErrorMessage(error)
        when (error.authFailureReason) {
            AuthFailure.Reason.WRONG_CURRENT_PASSWORD ->
                _viewData.update { it.copy(currentPasswordError = message) }
            AuthFailure.Reason.SAME_PASSWORD, AuthFailure.Reason.WEAK_PASSWORD ->
                _viewData.update { it.copy(newPasswordError = message) }
            AuthFailure.Reason.INVALID_CODE ->
                _viewData.update { it.copy(codeError = message) }
            else -> showInfo(message)
        }
    }

    private fun showInfo(message: String) {
        popupManager.show(PopupState.Info(title = title, message = message))
    }

    private fun runLoading(block: suspend () -> Unit) {
        _viewData.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                block()
            } finally {
                _viewData.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun ValidationResult.errorOrNull(): String? = (this as? ValidationResult.Fail)?.msg
}
