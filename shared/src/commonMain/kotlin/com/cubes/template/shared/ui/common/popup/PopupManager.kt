package com.cubes.template.shared.ui.common.popup

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide bottom-sheet style popups. ViewModels call [PopupManager.show]; the root
 * [GlobalPopupRenderer] renders whatever is in [PopupManager.popupState].
 * Add a new subclass + a branch in [GlobalPopupRenderer] for each new popup type.
 */
sealed interface PopupState {
    data class Error(
        val title: String? = null,
        val subtitle: String? = null,
        val message: String? = null,
    ) : PopupState

    data class Success(
        val title: String? = null,
        val subtitle: String? = null,
        val message: String? = null,
        val onDismiss: (() -> Unit)? = null,
    ) : PopupState
}

interface PopupManager {
    val popupState: StateFlow<PopupState?>
    fun show(state: PopupState)
    fun dismiss()
}

class GlobalPopupManager : PopupManager {
    private val _popupState = MutableStateFlow<PopupState?>(null)
    override val popupState: StateFlow<PopupState?> = _popupState.asStateFlow()

    override fun show(state: PopupState) {
        _popupState.value = state
    }

    override fun dismiss() {
        val currentState = _popupState.value
        _popupState.value = null
        if (currentState is PopupState.Success) {
            currentState.onDismiss?.invoke()
        }
    }
}
