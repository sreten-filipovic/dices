package com.cubes.template.shared.ui.main.game

import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.domain.game.Player

object Game {

    data class ViewData(
        val state: GameState,
        /** Shown between turns so the device can be handed to the next player. */
        val passDevice: PassDevice? = null,
        val listener: GameViewListener,
    )

    data class PassDevice(
        val previousPlayer: Player,
        val previousDice: List<Int>,
        val nextPlayer: Player,
    )
}

interface GameViewListener {
    fun onRollClick()
    fun onDieClick(index: Int)
    fun onRerollClick()
    fun onKeepClick()
    fun onReadyClick()
    fun onNextRoundClick()
    fun onNewMatchClick()
    fun onHomeClick()
}

interface GameScreenNavigator {
    fun showHome()
}
