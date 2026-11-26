package com.cubes.template.shared.ui.main.online.risk

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.domain.online.TurnStep
import com.cubes.template.shared.domain.risk.RiskIt
import com.cubes.template.shared.domain.risk.RiskItRules
import com.cubes.template.shared.domain.risk.RiskPhase
import com.cubes.template.shared.domain.risk.RiskState
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.main.online.ResumeGameViewModel
import com.cubes.template.shared.ui.main.online.turn.OnlinePlay
import com.cubes.template.shared.ui.main.online.turn.OnlineTurnGameViewModel
import com.cubes.template.shared.ui.main.risk_it.RiskItUi
import com.cubes.template.shared.ui.main.risk_it.RiskItViewListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnlineRiskItViewModel(
    roomId: String,
    roomRepository: RoomRepository,
    stringProvider: StringProvider,
    popupManager: PopupManager,
    onBack: () -> Unit,
) : OnlineTurnGameViewModel<RiskState>(roomId, roomRepository, stringProvider, popupManager, RiskItRules, onBack),
    RiskItViewListener {

    private val _viewData = MutableStateFlow<RiskItUi.ViewData?>(null)
    val viewData: StateFlow<RiskItUi.ViewData?> = _viewData.asStateFlow()

    init {
        start()
    }

    override fun newGame(room: Room, names: List<String>): RiskState =
        RiskIt.newGame(names, room.settings.rounds ?: ResumeGameViewModel.DEFAULT_ROUNDS)

    override fun onLoaded(state: RiskState, play: OnlinePlay) {
        val inTurn = state.phase == RiskPhase.Playing && state.turn.hasRolled
        _viewData.value = RiskItUi.ViewData(
            state = state,
            dialogOpen = inTurn,
            tablePlayer = if (state.phase == RiskPhase.Playing) state.currentPlayer else 0,
            tableTurn = state.turn,
            online = play,
            listener = this,
        )
    }

    override suspend fun show(step: TurnStep<RiskState>, previous: RiskState) {
        val roller = step.rolledBy
        if (roller == null) {
            _viewData.update { it?.copy(state = step.state, dialogOpen = false, rolling = false) }
            return
        }
        _viewData.update {
            it?.copy(
                state = step.state,
                dialogOpen = true,
                tablePlayer = roller,
                tableTurn = step.state.turn,
                rollTick = it.rollTick + 1,
                rolling = true,
            )
        }
        delay(DieAnimation.totalMillis(diceCount = 1))
        _viewData.update { it?.copy(rolling = false) }
    }

    override fun updatePlay(change: (OnlinePlay) -> OnlinePlay) {
        _viewData.update { data -> data?.copy(online = data.online?.let(change)) }
    }

    override fun onRollClick() = submitRoll()

    override fun onStopClick() = submitEndTurn(STOP)

    override fun onNextRoundClick() = Unit

    override fun onNewMatchClick() = Unit

    override fun onHomeClick() = requestLeave()

    private companion object {
        const val STOP = "stop"
    }
}
