package com.cubes.template.shared.ui.main.game

import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.domain.game.RoundPhase
import com.cubes.template.shared.ui.common.views.DieStyle

object Game {

    data class ViewData(
        val state: GameState,
        val dieStyles: List<DieStyle> = emptyList(),
        val rollDialog: RollDialog? = null,
        val rollTicks: Map<String, List<Int>> = emptyMap(),
        val online: Online? = null,
        val listener: GameViewListener,
    ) {
        fun controls(playerId: String): Boolean =
            online == null || (online.myPlayerId == playerId && !online.submitting)

        val isPlaying: Boolean get() = state.phase == RoundPhase.FirstRoll || state.phase == RoundPhase.SecondRoll

        val canRoll: Boolean get() = state.phase == RoundPhase.FirstRoll && controls(state.currentPlayer.id)

        val isMyTurn: Boolean get() = online != null && isPlaying && online.myPlayerId == state.currentPlayer.id

        fun dieStyleOf(playerId: String): DieStyle =
            dieStyles.getOrNull(state.players.indexOfFirst { it.id == playerId }) ?: DieStyle.WHITE

        fun rollTicksOf(playerId: String): List<Int> = rollTicks[playerId] ?: List(DICE) { 0 }
    }

    data class Online(
        val myPlayerId: String,
        val secondsLeft: Int? = null,
        val submitting: Boolean = false,
    )

    data class RollDialog(
        val playerId: String,
        val stage: Stage,
        val animating: Boolean,
    )

    enum class Stage {
        FIRST_ROLL,

        SECOND_ROLL,
    }

    private const val DICE = 5
}

interface GameViewListener {
    fun onRollClick()
    fun onDieClick(index: Int)
    fun onRerollClick()
    fun onKeepClick()
    fun onDoneClick()
    fun onNextRoundClick()
    fun onNewMatchClick()
    fun onHomeClick()
}

interface GameScreenNavigator {
    fun showHome()
}
