package com.cubes.template.shared.ui.main.sevens

import com.cubes.template.shared.domain.seven.Sevens
import com.cubes.template.shared.domain.seven.SevenState
import com.cubes.template.shared.domain.seven.SevenTurn

/**
 * Sevens: hot-seat game where pairs adding up to seven leave the table and the rest is scored.
 * A player's whole turn happens in the dice table dialog: it opens on the first roll and closes on "Bank".
 */
object SevensUi {
    data class ViewData(
        val state: SevenState,
        /** True while the dice table dialog is on screen. */
        val dialogOpen: Boolean = false,
        /** Player whose dice are on the table. */
        val tablePlayer: Int = 0,
        /** Dice on the table; kept after banking so they stay visible while the dialog closes. */
        val tableTurn: SevenTurn = SevenTurn(),
        /** Rolls the player at the table still has; frozen at the last roll for the same reason. */
        val tableRollsLeft: Int = 0,
        /** Per die counter that grows whenever that die is rolled (drives the animation). */
        val rollTicks: List<Int> = List(Sevens.DICE_COUNT) { 0 },
        /** True while the dice are tumbling; the outcome of the roll is revealed afterwards. */
        val rolling: Boolean = false,
        /** Removed pairs marked on screen, each in its own color; trails [state] until the roll has settled. */
        val pairsShown: List<Set<Int>> = emptyList(),
        val listener: SevensViewListener,
    )
}

interface SevensViewListener {
    fun onRollClick()
    fun onBankClick()
    fun onNextRoundClick()
    fun onNewMatchClick()
    fun onHomeClick()
}
