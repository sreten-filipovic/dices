package com.cubes.template.shared.ui.main.online.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.OnlineGame
import com.cubes.template.shared.data.online.RoomFailure
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.online_error_title
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import com.cubes.template.shared.ui.main.online.OnlineScreenNavigator
import com.cubes.template.shared.ui.main.online.roomErrorMessage
import com.cubes.template.shared.ui.main.online.roomFailureReason
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnlineLobbyViewModel(
    private val game: OnlineGame,
    playerOptions: List<Int>,
    private val roomRepository: RoomRepository,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val navigator: OnlineScreenNavigator,
) : ViewModel(), OnlineLobbyViewListener {

    private val _viewData = MutableStateFlow(
        OnlineLobby.ViewData(playerOptions = playerOptions, maxPlayers = playerOptions.last(), listener = this)
    )
    val viewData: StateFlow<OnlineLobby.ViewData> = _viewData.asStateFlow()

    suspend fun keepRefreshing() {
        while (true) {
            refresh()
            delay(REFRESH_MILLIS)
        }
    }

    private suspend fun refresh() {
        val result = roomRepository.openRooms(game)
        _viewData.update { data ->
            when (result) {
                is Result.Success -> data.copy(rooms = result.data, isLoadingRooms = false)
                is Result.Error -> data.copy(isLoadingRooms = false)
            }
        }
    }

    override fun onMaxPlayersSelected(count: Int) {
        _viewData.update { it.copy(maxPlayers = count) }
    }

    override fun onCreateRoomClick() {
        val data = _viewData.value
        if (data.busy != null) return
        runBusy(OnlineLobby.Busy.Creating) {
            when (val result = roomRepository.createRoom(game, data.maxPlayers)) {
                is Result.Success -> navigator.showOnlineRoom(result.data)
                is Result.Error -> showError(result.ex)
            }
        }
    }

    override fun onCodeChanged(code: String) {
        val cleaned = code.uppercase().filter { it.isLetterOrDigit() }.take(OnlineLobby.CODE_LENGTH)
        _viewData.update { it.copy(code = cleaned, codeError = null) }
    }

    override fun onJoinByCodeClick() {
        val data = _viewData.value
        if (!data.canJoinByCode) return
        runBusy(OnlineLobby.Busy.JoiningByCode) {
            when (val found = roomRepository.findRoom(data.code)) {
                is Result.Success -> join(found.data)
                is Result.Error -> if (found.ex.roomFailureReason == RoomFailure.Reason.ROOM_NOT_FOUND) {
                    _viewData.update { it.copy(codeError = stringProvider.roomErrorMessage(found.ex)) }
                } else {
                    showError(found.ex)
                }
            }
        }
    }

    override fun onRoomClick(roomId: String) {
        if (_viewData.value.busy != null) return
        runBusy(OnlineLobby.Busy.JoiningRoom(roomId)) { join(roomId) }
    }

    private suspend fun join(roomId: String) {
        when (val result = roomRepository.requestJoin(roomId)) {
            is Result.Success -> {
                _viewData.update { it.copy(code = "") }
                navigator.showOnlineRoom(roomId)
            }
            is Result.Error -> {
                showError(result.ex)
                refresh()
            }
        }
    }

    private fun runBusy(busy: OnlineLobby.Busy, block: suspend () -> Unit) {
        _viewData.update { it.copy(busy = busy) }
        viewModelScope.launch {
            try {
                block()
            } finally {
                _viewData.update { it.copy(busy = null) }
            }
        }
    }

    private fun showError(error: Throwable) {
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.online_error_title),
                message = stringProvider.roomErrorMessage(error),
            )
        )
    }

    private companion object {
        const val REFRESH_MILLIS = 5_000L
    }
}
