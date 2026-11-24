package com.cubes.template.shared.ui.main.ludo

import com.cubes.template.shared.domain.ludo.LudoPhase
import com.cubes.template.shared.domain.ludo.LudoState
import androidx.compose.ui.graphics.Color
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.PawnColor

object LudoUi {

    enum class Hint { ROLL, NEED_SIX, ROLL_AGAIN, PICK_PAWN, NO_MOVES, PLAYER_FINISHED, NONE }

    data class ViewData(
        val state: LudoState,
        val dieStyles: List<DieStyle> = emptyList(),
        val pawnColors: List<PawnColor> = PawnColor.defaultsFor(state.players.size),
        val dieValue: Int? = null,
        val rollKey: Int = 0,
        val rolling: Boolean = false,
        val moving: Boolean = false,
        val noMoves: Boolean = false,
        val extraRoll: Boolean = false,
        val finishedPlayer: Int? = null,
        val listener: LudoViewListener,
    ) {
        val busy: Boolean get() = rolling || moving || noMoves

        val canRoll: Boolean get() = !busy && state.phase == LudoPhase.WaitingForRoll

        val movable: Set<Int>
            get() = if (busy) emptySet() else (state.phase as? LudoPhase.ChoosingPawn)?.movable.orEmpty()

        fun colorOf(player: Int): Color = pawnColors.getOrElse(player) { PawnColor.entries[player] }.color

        val currentDieStyle: DieStyle get() = dieStyles.getOrNull(state.currentPlayer) ?: DieStyle.WHITE

        val hint: Hint
            get() = when {
                noMoves -> Hint.NO_MOVES
                rolling || moving -> Hint.NONE
                state.phase is LudoPhase.ChoosingPawn -> Hint.PICK_PAWN
                state.phase is LudoPhase.GameOver -> Hint.NONE
                finishedPlayer != null -> Hint.PLAYER_FINISHED
                extraRoll -> Hint.ROLL_AGAIN
                state.rollsLeft > 1 -> Hint.NEED_SIX
                else -> Hint.ROLL
            }
    }
}

interface LudoViewListener {
    fun onRollClick()
    fun onPawnClick(pawn: Int)
    fun onPlayAgainClick()
    fun onBackClick()
}
