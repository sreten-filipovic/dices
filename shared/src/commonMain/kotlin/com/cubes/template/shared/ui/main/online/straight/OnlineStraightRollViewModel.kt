package com.cubes.template.shared.ui.main.online.straight

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.domain.online.TurnStep
import com.cubes.template.shared.domain.straight.StraightPhase
import com.cubes.template.shared.domain.straight.StraightRoll
import com.cubes.template.shared.domain.straight.StraightRollRules
import com.cubes.template.shared.domain.straight.StraightState
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.main.online.ResumeGameViewModel
import com.cubes.template.shared.ui.main.online.turn.OnlinePlay
import com.cubes.template.shared.ui.main.online.turn.OnlineTurnGameViewModel
import com.cubes.template.shared.ui.main.straight_roll.StraightRollUi
import com.cubes.template.shared.ui.main.straight_roll.StraightRollViewListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnlineStraightRollViewModel(
    roomId: String,
    roomRepository: RoomRepository,
    stringProvider: StringProvider,
    popupManager: PopupManager,
    onBack: () -> Unit,
) : OnlineTurnGameViewModel<StraightState>(roomId, roomRepository, stringProvider, popupManager, StraightRollRules, onBack),
    StraightRollViewListener {

    private val _viewData = MutableStateFlow<StraightRollUi.ViewData?>(null)
    val viewData: StateFlow<StraightRollUi.ViewData?> = _viewData.asStateFlow()

    init {
        start()
    }

    override fun newGame(room: Room, names: List<String>): StraightState =
        StraightRoll.newGame(names, room.settings.rounds ?: ResumeGameViewModel.DEFAULT_ROUNDS)

    override fun updatePlay(change: (OnlinePlay) -> OnlinePlay) {
        _viewData.update { data -> data?.copy(online = data.online?.let(change)) }
    }

    override fun onLoaded(state: StraightState, play: OnlinePlay) {
        val inTurn = state.phase == StraightPhase.Playing && state.turn.hasRolled
        _viewData.value = StraightRollUi.ViewData(
            state = state,
            dialogOpen = inTurn,
            tablePlayer = if (state.phase == StraightPhase.Playing) state.currentPlayer else 0,
            tableTurn = state.turn,
            onTable = state.turn.remaining,
            collected = state.turn.keptValues,
            online = play,
            listener = this,
        )
    }

    override suspend fun show(step: TurnStep<StraightState>, previous: StraightState) {
        val roller = step.rolledBy
        if (roller == null) {
            _viewData.update { it?.copy(state = step.state, dialogOpen = false, rolling = false) }
            return
        }
        val new = step.state
        val old = previous.turn
        val rolled = if (old.hasRolled) old.remaining else (0 until StraightRoll.DICE_COUNT).toSet()
        _viewData.update { data ->
            data?.copy(
                state = new,
                dialogOpen = true,
                tablePlayer = roller,
                tableTurn = new.turn,
                rollTicks = data.rollTicks.mapIndexed { index, tick -> if (index in rolled) tick + 1 else tick },
                rolling = true,
                onTable = rolled,
                moving = emptySet(),
                collected = if (old.hasRolled) old.keptValues else emptySet(),
            )
        }
        delay(DieAnimation.totalMillis(diceCount = StraightRoll.DICE_COUNT))
        val placed = rolled - new.turn.remaining
        if (placed.isNotEmpty()) {
            delay(StraightRollUi.PAUSE_BEFORE_MOVE_MILLIS)
            _viewData.update { it?.copy(moving = placed) }
            delay(StraightRollUi.MOVE_MILLIS + StraightRollUi.MOVE_SETTLE_MILLIS)
        }
        _viewData.update {
            it?.copy(rolling = false, onTable = new.turn.remaining, moving = emptySet(), collected = new.turn.keptValues)
        }
    }

    override fun onRollClick() = submitRoll()

    override fun onDoneClick() = submitEndTurn("finish")

    override fun onNextRoundClick() = Unit

    override fun onNewMatchClick() = Unit

    override fun onHomeClick() = requestLeave()
}
