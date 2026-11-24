package com.cubes.template.shared.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.auth.AuthRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.data.session.UserSession
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.auth_failed_title
import com.cubes.template.shared.ui.common.ValidationResult
import com.cubes.template.shared.ui.common.Validator
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    canGoBack: Boolean,
    private val authRepository: AuthRepository,
    private val userSession: UserSession,
    private val validator: Validator,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val onFinished: () -> Unit,
) : ViewModel(), AuthViewListener {

    private val _viewData = MutableStateFlow(Auth.ViewData(canGoBack = canGoBack, listener = this))
    val viewData: StateFlow<Auth.ViewData> = _viewData.asStateFlow()

    override fun onModeSelected(mode: Auth.Mode) {
        _viewData.update {
            if (it.isLoading) it
            else it.copy(mode = mode, usernameError = null, emailError = null, passwordError = null)
        }
    }

    override fun onUsernameChanged(username: String) {
        _viewData.update { it.copy(username = username, usernameError = null) }
    }

    override fun onEmailChanged(email: String) {
        _viewData.update { it.copy(email = email, emailError = null) }
    }

    override fun onPasswordChanged(password: String) {
        _viewData.update { it.copy(password = password, passwordError = null) }
    }

    override fun onSubmitClick() {
        val data = _viewData.value
        if (data.isLoading || !validate(data)) return

        _viewData.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val email = data.email.trim()
            val result = when (data.mode) {
                Auth.Mode.LOGIN -> authRepository.login(email, data.password)
                Auth.Mode.REGISTER -> authRepository.register(data.username.trim(), email, data.password)
            }
            _viewData.update { it.copy(isLoading = false) }
            when (result) {
                is Result.Success -> {
                    userSession.signIn(result.data)
                    onFinished()
                }
                is Result.Error -> popupManager.show(
                    PopupState.Info(
                        title = stringProvider.getString(Res.string.auth_failed_title),
                        message = result.ex.message,
                    )
                )
            }
        }
    }

    override fun onSkipClick() {
        if (!_viewData.value.isLoading) onFinished()
    }

    override fun onBackClick() {
        if (!_viewData.value.isLoading) onFinished()
    }

    private fun validate(data: Auth.ViewData): Boolean {
        val usernameError = if (data.mode == Auth.Mode.REGISTER) {
            validator.validateUsername(data.username).errorOrNull()
        } else {
            null
        }
        val emailError = validator.validateEmail(data.email).errorOrNull()
        val passwordError = if (data.mode == Auth.Mode.REGISTER) {
            validator.validatePassword(data.password, Auth.PASSWORD_MIN_LENGTH).errorOrNull()
        } else {
            validator.validateRequired(data.password).errorOrNull()
        }
        _viewData.update {
            it.copy(usernameError = usernameError, emailError = emailError, passwordError = passwordError)
        }
        return usernameError == null && emailError == null && passwordError == null
    }

    private fun ValidationResult.errorOrNull(): String? = (this as? ValidationResult.Fail)?.msg
}
