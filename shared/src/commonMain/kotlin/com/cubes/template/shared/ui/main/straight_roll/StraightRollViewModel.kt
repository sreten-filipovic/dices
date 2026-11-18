package com.cubes.template.shared.ui.main.straight_roll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.straight.StraightRoll
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StraightRollViewModel(
    private val playerNames: List<String>,
    dieStyles: List<DieStyle>,
    private val totalRounds: Int,
    private val diceRoller: DiceRoller,
    private val onHome: () -> Unit,
) : ViewModel(), StraightRollViewListener {

    private val _viewData = MutableStateFlow(
        StraightRollUi.ViewData(
            state = StraightRoll.newGame(playerNames, totalRounds),
            dieStyles = dieStyles,
            listener = this,
        )
    )
    val viewData: StateFlow<StraightRollUi.ViewData> = _viewData.asStateFlow()

    private var animationJob: Job? = null

    override fun onRollClick() {
        val data = _viewData.value
        if (data.rolling) return
        val old = data.state
        val new = StraightRoll.roll(old, diceRoller)
        if (new === old) return

        val rolled = if (old.turn.hasRolled) old.turn.remaining else (0 until StraightRoll.DICE_COUNT).toSet()
        _viewData.value = data.copy(
            state = new,
            dialogOpen = true,
            tablePlayer = old.currentPlayer,
            tableTurn = new.turn,
            rollTicks = data.rollTicks.mapIndexed { index, tick -> if (index in rolled) tick + 1 else tick },
            rolling = true,
            onTable = rolled,
            moving = emptySet(),
            collected = if (old.turn.hasRolled) old.turn.keptValues else emptySet(),
        )
        animationJob = viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = StraightRoll.DICE_COUNT))
            val placed = rolled - new.turn.remaining
            if (placed.isNotEmpty()) {
                delay(StraightRollUi.PAUSE_BEFORE_MOVE_MILLIS)
                _viewData.update { it.copy(moving = placed) }
                delay(StraightRollUi.MOVE_MILLIS + StraightRollUi.MOVE_SETTLE_MILLIS)
            }
            _viewData.update {
                it.copy(
                    rolling = false,
                    onTable = new.turn.remaining,
                    moving = emptySet(),
                    collected = new.turn.keptValues,
                )
            }
        }
    }

    override fun onDoneClick() {
        val data = _viewData.value
        if (data.rolling || !data.state.canFinish) return
        _viewData.value = data.copy(state = StraightRoll.finish(data.state), dialogOpen = false)
    }

    override fun onNextRoundClick() {
        _viewData.update { it.copy(state = StraightRoll.nextRound(it.state)) }
    }

    override fun onNewMatchClick() {
        animationJob?.cancel()
        _viewData.update {
            it.copy(state = StraightRoll.newGame(playerNames, totalRounds), dialogOpen = false, rolling = false, moving = emptySet())
        }
    }

    override fun onHomeClick() = onHome()
}
