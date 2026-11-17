package com.cubes.template.shared.ui.main.game

import com.cubes.template.shared.domain.game.GameState

object Game {

    data class ViewData(
        val state: GameState,
        /** Dice table dialog for the player who is rolling; null when the main screen is in focus. */
        val rollDialog: RollDialog? = null,
        /** Per player, per die counter that grows whenever that die is rolled (drives the animation). */
        val rollTicks: Map<String, List<Int>> = emptyMap(),
        val listener: GameViewListener,
    ) {
        fun rollTicksOf(playerId: String): List<Int> = rollTicks[playerId] ?: List(DICE) { 0 }
    }

    data class RollDialog(
        val playerId: String,
        val stage: Stage,
        /** True while the dice are tumbling; buttons and results appear when it turns false. */
        val animating: Boolean,
    )

    enum class Stage {
        /** After the first roll: choose dice to reroll, or keep. */
        FIRST_ROLL,

        /** After the second roll: the turn is over, the player closes the dialog. */
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
