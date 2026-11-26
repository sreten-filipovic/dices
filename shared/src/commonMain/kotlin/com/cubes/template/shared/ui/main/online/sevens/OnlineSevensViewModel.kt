package com.cubes.template.shared.ui.main.online.sevens

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.domain.online.TurnStep
import com.cubes.template.shared.domain.seven.SevenPhase
import com.cubes.template.shared.domain.seven.SevenState
import com.cubes.template.shared.domain.seven.Sevens
import com.cubes.template.shared.domain.seven.SevensRules
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.main.online.ResumeGameViewModel
import com.cubes.template.shared.ui.main.online.turn.OnlinePlay
import com.cubes.template.shared.ui.main.online.turn.OnlineTurnGameViewModel
import com.cubes.template.shared.ui.main.sevens.SevensUi
import com.cubes.template.shared.ui.main.sevens.SevensViewListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnlineSevensViewModel(
    roomId: String,
    roomRepository: RoomRepository,
    stringProvider: StringProvider,
    popupManager: PopupManager,
    onBack: () -> Unit,
) : OnlineTurnGameViewModel<SevenState>(roomId, roomRepository, stringProvider, popupManager, SevensRules, onBack),
    SevensViewListener {

    private val _viewData = MutableStateFlow<SevensUi.ViewData?>(null)
    val viewData: StateFlow<SevensUi.ViewData?> = _viewData.asStateFlow()

    init {
        start()
    }

    override fun newGame(room: Room, names: List<String>): SevenState =
        Sevens.newGame(names, room.settings.rounds ?: ResumeGameViewModel.DEFAULT_ROUNDS)

    override fun updatePlay(change: (OnlinePlay) -> OnlinePlay) {
        _viewData.update { data -> data?.copy(online = data.online?.let(change)) }
    }

    override fun onLoaded(state: SevenState, play: OnlinePlay) {
        val inTurn = state.phase == SevenPhase.Playing && state.turn.hasRolled
        _viewData.value = SevensUi.ViewData(
            state = state,
            dialogOpen = inTurn,
            tablePlayer = if (state.phase == SevenPhase.Playing) state.currentPlayer else 0,
            tableTurn = state.turn,
            tableRollsLeft = state.rollLimit - state.turn.rolls,
            pairsShown = state.turn.pairs,
            online = play,
            listener = this,
        )
    }

    override suspend fun show(step: TurnStep<SevenState>, previous: SevenState) {
        val roller = step.rolledBy
        if (roller == null) {
            _viewData.update { it?.copy(state = step.state, dialogOpen = false, rolling = false) }
            return
        }
        val new = step.state
        val old = previous.turn
        val rolled = if (old.hasRolled) old.active.toSet() else (0 until Sevens.DICE_COUNT).toSet()
        _viewData.update { data ->
            data?.copy(
                state = new,
                dialogOpen = true,
                tablePlayer = roller,
                tableTurn = new.turn,
                tableRollsLeft = new.rollLimit - new.turn.rolls,
                pairsShown = if (old.hasRolled) data.pairsShown else emptyList(),
                rollTicks = data.rollTicks.mapIndexed { index, tick -> if (index in rolled) tick + 1 else tick },
                rolling = true,
            )
        }
        delay(DieAnimation.totalMillis(diceCount = Sevens.DICE_COUNT))
        _viewData.update { it?.copy(rolling = false, pairsShown = new.turn.pairs) }
    }

    override fun onRollClick() = submitRoll()

    override fun onBankClick() = submitEndTurn("bank")

    override fun onNextRoundClick() = Unit

    override fun onNewMatchClick() = Unit

    override fun onHomeClick() = requestLeave()
}
