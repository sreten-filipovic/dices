package com.cubes.template.shared.ui.main.online.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.MemberStatus
import com.cubes.template.shared.data.online.OnlineGame
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomFailure
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.data.online.RoomStatus
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.online_declined_message
import com.cubes.template.shared.resources.online_declined_title
import com.cubes.template.shared.resources.online_error_title
import com.cubes.template.shared.resources.online_kicked_message
import com.cubes.template.shared.resources.online_room_closed_message
import com.cubes.template.shared.resources.online_room_closed_title
import com.cubes.template.shared.resources.online_room_expired_message
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OnlineRoomViewModel(
    private val roomId: String,
    private val roomRepository: RoomRepository,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val navigator: OnlineScreenNavigator,
) : ViewModel(), OnlineRoomViewListener {

    private val _viewData = MutableStateFlow(
        OnlineRoom.ViewData(myUserId = roomRepository.currentUserId, listener = this)
    )
    val viewData: StateFlow<OnlineRoom.ViewData> = _viewData.asStateFlow()

    private val refreshLock = Mutex()
    private var done = false

    init {
        viewModelScope.launch { roomRepository.roomChanges(roomId).collect { refresh() } }
        viewModelScope.launch {
            while (!done) {
                refresh()
                delay(REFRESH_MILLIS)
            }
        }
        viewModelScope.launch {
            while (!done) {
                if (_viewData.value.isHost) roomRepository.keepAlive(roomId)
                delay(KEEP_ALIVE_MILLIS)
            }
        }
    }

    private suspend fun refresh() = refreshLock.withLock {
        if (done) return@withLock
        when (val result = roomRepository.room(roomId)) {
            is Result.Success -> {
                _viewData.update { it.copy(room = result.data) }
                react(result.data)
            }
            is Result.Error -> if (result.ex.roomFailureReason == RoomFailure.Reason.ROOM_NOT_FOUND) {
                close(Res.string.online_room_closed_title, stringProvider.getString(Res.string.online_room_closed_message))
            }
        }
    }

    private fun react(room: Room) {
        val data = _viewData.value
        val me = data.me
        when {
            room.status == RoomStatus.PLAYING && me?.status == MemberStatus.ACCEPTED -> {
                done = true
                OnlineGame.fromKey(room.game)?.let { navigator.showOnlineGame(roomId, it) } ?: navigator.back()
            }
            me?.status == MemberStatus.REJECTED -> close(
                Res.string.online_declined_title,
                stringProvider.getString(Res.string.online_declined_message),
            )
            me?.status == MemberStatus.KICKED -> close(
                Res.string.online_room_closed_title,
                stringProvider.getString(Res.string.online_kicked_message),
            )
            room.status != RoomStatus.OPEN -> close(
                Res.string.online_room_closed_title,
                stringProvider.getString(
                    if (data.isHost) Res.string.online_room_expired_message else Res.string.online_room_closed_message
                ),
            )
        }
    }

    private fun close(title: org.jetbrains.compose.resources.StringResource, message: String) {
        if (done) return
        done = true
        popupManager.show(PopupState.Info(title = stringProvider.getString(title), message = message))
        navigator.back()
    }

    override fun onAcceptClick(userId: String) = respond(userId, accept = true)

    override fun onDeclineClick(userId: String) = respond(userId, accept = false)

    private fun respond(userId: String, accept: Boolean) = runForUser(userId) {
        roomRepository.respondToRequest(roomId, userId, accept)
    }

    override fun onRemoveClick(userId: String) = runForUser(userId) { roomRepository.kick(roomId, userId) }

    private fun runForUser(userId: String, action: suspend () -> Result<Unit>) {
        if (_viewData.value.busyUserId != null) return
        _viewData.update { it.copy(busyUserId = userId) }
        viewModelScope.launch {
            val result = action()
            if (result is Result.Error) showError(result.ex)
            refresh()
            _viewData.update { it.copy(busyUserId = null) }
        }
    }

    override fun onStartClick() {
        if (!_viewData.value.canStart) return
        _viewData.update { it.copy(isStarting = true) }
        viewModelScope.launch {
            val result = roomRepository.start(roomId)
            if (result is Result.Error) showError(result.ex)
            refresh()
            _viewData.update { it.copy(isStarting = false) }
        }
    }

    override fun onLeaveClick() {
        if (_viewData.value.isLeaving || done) return
        _viewData.update { it.copy(isLeaving = true) }
        viewModelScope.launch {
            refreshLock.withLock {
                roomRepository.leave(roomId)
                done = true
            }
            navigator.back()
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
        const val REFRESH_MILLIS = 3_000L
        const val KEEP_ALIVE_MILLIS = 20_000L
    }
}
