package com.cubes.template.shared.ui.main.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.OnlineGame
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.game.DicePokerOnline
import com.cubes.template.shared.domain.game.RoundPhase
import com.cubes.template.shared.domain.ludo.LudoOnline
import com.cubes.template.shared.domain.ludo.LudoPhase
import com.cubes.template.shared.domain.jack.DiceJack
import com.cubes.template.shared.domain.jack.DiceJackOnline
import com.cubes.template.shared.domain.jack.JackPhase
import com.cubes.template.shared.domain.seven.Sevens
import com.cubes.template.shared.domain.seven.SevensOnline
import com.cubes.template.shared.domain.seven.SevenPhase
import com.cubes.template.shared.domain.straight.StraightRoll
import com.cubes.template.shared.domain.straight.StraightRollOnline
import com.cubes.template.shared.domain.straight.StraightPhase
import com.cubes.template.shared.domain.duel.DiceDuel
import com.cubes.template.shared.domain.duel.DiceDuelOnline
import com.cubes.template.shared.domain.risk.RiskIt
import com.cubes.template.shared.domain.risk.RiskItOnline
import com.cubes.template.shared.domain.risk.RiskPhase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_dice_poker
import com.cubes.template.shared.resources.game_ludo
import com.cubes.template.shared.resources.game_risk_it
import com.cubes.template.shared.resources.game_dice_duel
import com.cubes.template.shared.resources.game_dice_jack
import com.cubes.template.shared.resources.game_sevens
import com.cubes.template.shared.resources.game_straight_roll
import com.cubes.template.shared.resources.online_resume_confirm
import com.cubes.template.shared.resources.online_resume_later
import com.cubes.template.shared.resources.online_resume_message
import com.cubes.template.shared.resources.online_resume_title
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import com.cubes.template.shared.ui.main.online.ludo.toLudoEvent
import com.cubes.template.shared.ui.main.online.poker.toPokerEvent
import com.cubes.template.shared.ui.main.online.turn.toTurnEvent
import com.cubes.template.shared.ui.main.online.duel.OnlineDiceDuelViewModel
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
            val room = (roomRepository.room(roomId) as? Result.Success)?.data ?: return@launch
            val game = OnlineGame.fromKey(room.game) ?: return@launch
            if (isStillPlaying(room, game)) offerResume(roomId, game)
        }
    }

    private suspend fun isStillPlaying(room: Room, game: OnlineGame): Boolean {
        val events = (roomRepository.events(room.id, 0) as? Result.Success)?.data ?: return false
        val seats = room.seated.map { it.userId }
        val names = room.seated.map { it.username }
        val me = seats.indexOf(roomRepository.currentUserId)
        if (me < 0) return false
        return when (game) {
            OnlineGame.LUDO -> {
                val state = LudoOnline.replay(LudoOnline.start(names), events.map { it.toLudoEvent(seats) }).state
                state.phase !is LudoPhase.GameOver && state.isActive(me)
            }
            OnlineGame.DICE_POKER -> {
                val start = DicePokerOnline.start(names, room.settings.rounds ?: DEFAULT_ROUNDS)
                val state = DicePokerOnline.replay(start, events.map { it.toPokerEvent(seats) }).state
                state.phase !is RoundPhase.MatchOver && state.players[me].id !in state.leftIds
            }
            OnlineGame.RISK_IT -> {
                val start = RiskItOnline.start(RiskIt.newGame(names, room.settings.rounds ?: DEFAULT_ROUNDS))
                val match = RiskItOnline.replay(start, events.map { it.toTurnEvent(seats) })
                match.state.phase != RiskPhase.MatchOver && me !in match.left
            }
            OnlineGame.DICE_JACK -> {
                val start = DiceJackOnline.start(DiceJack.newGame(names, room.settings.rounds ?: DEFAULT_ROUNDS))
                val match = DiceJackOnline.replay(start, events.map { it.toTurnEvent(seats) })
                match.state.phase != JackPhase.MatchOver && me !in match.left
            }
            OnlineGame.SEVENS -> {
                val start = SevensOnline.start(Sevens.newGame(names, room.settings.rounds ?: DEFAULT_ROUNDS))
                val match = SevensOnline.replay(start, events.map { it.toTurnEvent(seats) })
                match.state.phase != SevenPhase.MatchOver && me !in match.left
            }
            OnlineGame.STRAIGHT_ROLL -> {
                val start = StraightRollOnline.start(StraightRoll.newGame(names, room.settings.rounds ?: DEFAULT_ROUNDS))
                val match = StraightRollOnline.replay(start, events.map { it.toTurnEvent(seats) })
                match.state.phase != StraightPhase.MatchOver && me !in match.left
            }
            OnlineGame.DICE_DUEL -> {
                val dice = room.settings.diceCount ?: OnlineDiceDuelViewModel.DEFAULT_DICE_COUNT
                val match = DiceDuelOnline.replay(
                    DiceDuelOnline.start(DiceDuel.newGame(names, dice)),
                    events.map { it.toTurnEvent(seats) },
                )
                !match.state.isOver && me !in match.left
            }
            else -> false
        }
    }

    private fun offerResume(roomId: String, game: OnlineGame) {
        val gameName = stringProvider.getString(
            when (game) {
                OnlineGame.DICE_POKER -> Res.string.game_dice_poker
                OnlineGame.RISK_IT -> Res.string.game_risk_it
                OnlineGame.DICE_DUEL -> Res.string.game_dice_duel
                OnlineGame.DICE_JACK -> Res.string.game_dice_jack
                OnlineGame.SEVENS -> Res.string.game_sevens
                OnlineGame.STRAIGHT_ROLL -> Res.string.game_straight_roll
                else -> Res.string.game_ludo
            }
        )
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.online_resume_title),
                message = stringProvider.getString(Res.string.online_resume_message, gameName),
                confirmText = stringProvider.getString(Res.string.online_resume_confirm),
                cancelText = stringProvider.getString(Res.string.online_resume_later),
                onConfirm = { navigator.showOnlineGame(roomId, game) },
            )
        )
    }

    companion object {
        const val DEFAULT_ROUNDS = 3
    }
}
