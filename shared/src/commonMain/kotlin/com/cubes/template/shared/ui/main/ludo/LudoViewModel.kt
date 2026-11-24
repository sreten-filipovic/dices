package com.cubes.template.shared.ui.main.ludo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.ludo.Ludo
import com.cubes.template.shared.domain.ludo.LudoPhase
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.PawnColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LudoViewModel(
    private val playerNames: List<String>,
    dieStyles: List<DieStyle>,
    pawnColors: List<PawnColor>,
    private val diceRoller: DiceRoller,
    private val onBack: () -> Unit,
) : ViewModel(), LudoViewListener {

    private val _viewData = MutableStateFlow(
        LudoUi.ViewData(
            state = Ludo.newGame(playerNames),
            dieStyles = dieStyles,
            pawnColors = pawnColors,
            listener = this,
        )
    )
    val viewData: StateFlow<LudoUi.ViewData> = _viewData.asStateFlow()

    private var animationJob: Job? = null

    override fun onRollClick() {
        val data = _viewData.value
        if (!data.canRoll) return
        val old = data.state
        val new = Ludo.roll(old, diceRoller)
        val roll = new.lastRoll ?: return

        _viewData.value = data.copy(dieValue = roll, rollKey = data.rollKey + 1, rolling = true, extraRoll = false, finishedPlayer = null)
        animationJob = viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = 1))
            when {
                new.phase is LudoPhase.ChoosingPawn -> {
                    _viewData.update { it.copy(state = new, rolling = false) }
                    new.phase.movable.singleOrNull()?.let { pawn ->
                        delay(AUTO_MOVE_MILLIS)
                        onPawnClick(pawn)
                    }
                }
                new.currentPlayer == old.currentPlayer -> _viewData.update {
                    it.copy(state = new, rolling = false, extraRoll = roll == Ludo.EXIT_ROLL)
                }
                else -> {
                    _viewData.update { it.copy(rolling = false, noMoves = true) }
                    delay(NO_MOVES_MILLIS)
                    _viewData.update { it.copy(state = new, noMoves = false) }
                }
            }
        }
    }

    override fun onPawnClick(pawn: Int) {
        val data = _viewData.value
        if (pawn !in data.movable) return
        val old = data.state
        val new = Ludo.move(old, pawn)
        val move = new.lastMove ?: return

        _viewData.value = data.copy(state = new, moving = true)
        animationJob = viewModelScope.launch {
            val captureMillis = if (move.captured.isEmpty()) 0 else LudoGeometry.CAPTURE_MILLIS
            delay(LudoGeometry.steps(move) * LudoGeometry.STEP_MILLIS.toLong() + captureMillis + SETTLE_MILLIS)
            _viewData.update {
                it.copy(
                    moving = false,
                    extraRoll = new.phase == LudoPhase.WaitingForRoll && new.currentPlayer == old.currentPlayer,
                    finishedPlayer = new.ranking.lastOrNull().takeIf { new.ranking.size > old.ranking.size },
                )
            }
        }
    }

    override fun onPlayAgainClick() {
        animationJob?.cancel()
        _viewData.update {
            LudoUi.ViewData(
                state = Ludo.newGame(playerNames),
                dieStyles = it.dieStyles,
                pawnColors = it.pawnColors,
                listener = this,
            )
        }
    }

    override fun onBackClick() = onBack()

    private companion object {
        const val NO_MOVES_MILLIS = 1100L
        const val AUTO_MOVE_MILLIS = 450L
        const val SETTLE_MILLIS = 120L
    }
}
