package com.cubes.template.shared.ui.main.game

import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.ui.common.views.DieStyle

object Game {

    data class ViewData(
        val state: GameState,
        val dieStyles: List<DieStyle> = emptyList(),
        val rollDialog: RollDialog? = null,
        val rollTicks: Map<String, List<Int>> = emptyMap(),
        val listener: GameViewListener,
    ) {
        fun dieStyleOf(playerId: String): DieStyle =
            dieStyles.getOrNull(state.players.indexOfFirst { it.id == playerId }) ?: DieStyle.WHITE

        fun rollTicksOf(playerId: String): List<Int> = rollTicks[playerId] ?: List(DICE) { 0 }
    }

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
