package com.cubes.template.shared.ui.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.auth.AuthFailure
import com.cubes.template.shared.data.auth.AuthRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.data.session.UserSession
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.profile_delete_confirm
import com.cubes.template.shared.resources.profile_delete_message
import com.cubes.template.shared.resources.profile_delete_title
import com.cubes.template.shared.resources.profile_deleted_message
import com.cubes.template.shared.resources.profile_deleted_title
import com.cubes.template.shared.resources.profile_error_title
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

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userSession: UserSession,
    private val validator: Validator,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val screenNavigator: ProfileScreenNavigator,
) : ViewModel(), ProfileViewListener {

    private val _viewData = MutableStateFlow(Profile.ViewData(listener = this))
    val viewData: StateFlow<Profile.ViewData> = _viewData.asStateFlow()

    init {
        viewModelScope.launch {
            userSession.currentUser.collect { user ->
                _viewData.update {
                    it.copy(user = user, usernameInput = user?.username.orEmpty(), usernameError = null)
                }
            }
        }
    }

    override fun onLoginClick() = screenNavigator.showAuth()

    override fun onUsernameChanged(username: String) {
        _viewData.update { it.copy(usernameInput = username, usernameError = null) }
    }

    override fun onSaveUsernameClick() {
        val data = _viewData.value
        val user = data.user ?: return
        if (!data.canSaveUsername) return
        val error = (validator.validateUsername(data.usernameInput) as? ValidationResult.Fail)?.msg
        if (error != null) {
            _viewData.update { it.copy(usernameError = error) }
            return
        }

        _viewData.update { it.copy(isSavingUsername = true) }
        viewModelScope.launch {
            val result = authRepository.updateUsername(user, data.usernameInput.trim())
            _viewData.update { it.copy(isSavingUsername = false) }
            when (result) {
                is Result.Success -> userSession.signIn(result.data)
                is Result.Error -> if (result.ex.authFailureReason == AuthFailure.Reason.USERNAME_TAKEN) {
                    _viewData.update { it.copy(usernameError = stringProvider.authErrorMessage(result.ex)) }
                } else {
                    showError(result.ex)
                }
            }
        }
    }

    override fun onChangePasswordClick() {
        if (!_viewData.value.isBusy) screenNavigator.showChangePassword()
    }

    override fun onLogoutClick() {
        if (_viewData.value.isBusy) return
        viewModelScope.launch {
            authRepository.logout()
            userSession.signOut()
        }
    }

    override fun onDeleteAccountClick() {
        if (_viewData.value.isBusy || _viewData.value.user == null) return
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.profile_delete_title),
                message = stringProvider.getString(Res.string.profile_delete_message),
                confirmText = stringProvider.getString(Res.string.profile_delete_confirm),
                isDestructive = true,
                onConfirm = ::deleteAccount,
            )
        )
    }

    private fun deleteAccount() {
        val user = _viewData.value.user ?: return
        _viewData.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            val result = authRepository.deleteAccount(user)
            _viewData.update { it.copy(isDeleting = false) }
            when (result) {
                is Result.Success -> {
                    userSession.signOut()
                    popupManager.show(
                        PopupState.Info(
                            title = stringProvider.getString(Res.string.profile_deleted_title),
                            message = stringProvider.getString(Res.string.profile_deleted_message),
                        )
                    )
                }
                is Result.Error -> showError(result.ex)
            }
        }
    }

    private fun showError(ex: Exception) {
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.profile_error_title),
                message = stringProvider.authErrorMessage(ex),
            )
        )
    }
}
