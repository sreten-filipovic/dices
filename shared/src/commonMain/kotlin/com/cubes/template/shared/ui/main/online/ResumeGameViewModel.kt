package com.cubes.template.shared.ui.main.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.ludo.LudoOnline
import com.cubes.template.shared.domain.ludo.LudoPhase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.online_resume_confirm
import com.cubes.template.shared.resources.online_resume_later
import com.cubes.template.shared.resources.online_resume_message
import com.cubes.template.shared.resources.online_resume_title
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import com.cubes.template.shared.ui.main.online.ludo.toLudoEvent
import kotlinx.coroutines.launch

class ResumeGameViewModel(
    private val roomRepository: RoomRepository,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val navigator: OnlineScreenNavigator,
) : ViewModel() {

    init {
        viewModelScope.launch {
            val roomId = roomRepository.activeGameId() ?: return@launch
            if (isStillPlaying(roomId)) offerResume(roomId)
        }
    }

    private suspend fun isStillPlaying(roomId: String): Boolean {
        val room = (roomRepository.room(roomId) as? Result.Success)?.data ?: return false
        val events = (roomRepository.events(roomId, 0) as? Result.Success)?.data ?: return false
        val seats = room.seated.map { it.userId }
        val me = seats.indexOf(roomRepository.currentUserId)
        if (me < 0) return false
        val ludoEvents = events.map { it.toLudoEvent(seats) }
        val state = LudoOnline.replay(LudoOnline.start(room.seated.map { it.username }), ludoEvents).state
        return state.phase !is LudoPhase.GameOver && state.isActive(me)
    }

    private fun offerResume(roomId: String) {
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.online_resume_title),
                message = stringProvider.getString(Res.string.online_resume_message),
                confirmText = stringProvider.getString(Res.string.online_resume_confirm),
                cancelText = stringProvider.getString(Res.string.online_resume_later),
                onConfirm = { navigator.showOnlineLudo(roomId) },
            )
        )
    }
}
