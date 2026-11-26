package com.cubes.template.shared.ui.main.online.jack

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.domain.online.TurnStep
import com.cubes.template.shared.domain.jack.DiceJack
import com.cubes.template.shared.domain.jack.DiceJackRules
import com.cubes.template.shared.domain.jack.JackPhase
import com.cubes.template.shared.domain.jack.JackState
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.main.online.ResumeGameViewModel
import com.cubes.template.shared.ui.main.online.turn.OnlinePlay
import com.cubes.template.shared.ui.main.online.turn.OnlineTurnGameViewModel
import com.cubes.template.shared.ui.main.dice_jack.DiceJackUi
import com.cubes.template.shared.ui.main.dice_jack.DiceJackViewListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnlineDiceJackViewModel(
    roomId: String,
    roomRepository: RoomRepository,
    stringProvider: StringProvider,
    popupManager: PopupManager,
    onBack: () -> Unit,
) : OnlineTurnGameViewModel<JackState>(roomId, roomRepository, stringProvider, popupManager, DiceJackRules, onBack),
    DiceJackViewListener {

    private val _viewData = MutableStateFlow<DiceJackUi.ViewData?>(null)
    val viewData: StateFlow<DiceJackUi.ViewData?> = _viewData.asStateFlow()

    init {
        start()
    }

    override fun newGame(room: Room, names: List<String>): JackState =
        DiceJack.newGame(names, room.settings.rounds ?: ResumeGameViewModel.DEFAULT_ROUNDS)

    override fun updatePlay(change: (OnlinePlay) -> OnlinePlay) {
        _viewData.update { data -> data?.copy(online = data.online?.let(change)) }
    }

    override fun onLoaded(state: JackState, play: OnlinePlay) {
        val inTurn = state.phase == JackPhase.Playing && state.turn.hasRolled
        _viewData.value = DiceJackUi.ViewData(
            state = state,
            dialogOpen = inTurn,
            tablePlayer = if (state.phase == JackPhase.Playing) state.currentPlayer else 0,
            tableTurn = state.turn,
            online = play,
            listener = this,
        )
    }

    override suspend fun show(step: TurnStep<JackState>, previous: JackState) {
        val roller = step.rolledBy
        if (roller == null) {
            _viewData.update { it?.copy(state = step.state, dialogOpen = false, rolling = false) }
            return
        }
        val new = step.state
        val rolled = previous.turn.dice.size until new.turn.dice.size
        _viewData.update { data ->
            data?.copy(
                state = new,
                dialogOpen = true,
                tablePlayer = roller,
                tableTurn = new.turn,
                rollTicks = data.rollTicks.mapIndexed { index, tick -> if (index in rolled) tick + 1 else tick },
                rolling = true,
            )
        }
        delay(DieAnimation.totalMillis(diceCount = rolled.count()))
        _viewData.update { it?.copy(rolling = false) }
    }

    override fun onRollClick() = submitRoll()

    override fun onStandClick() = submitEndTurn("stand")

    override fun onNextRoundClick() = Unit

    override fun onNewMatchClick() = Unit

    override fun onHomeClick() = requestLeave()
}
