package com.cubes.template.shared.ui.main.dice_duel

import com.cubes.template.shared.domain.duel.DuelState

/**
 * Dice Duel: hot-seat game where everyone rolls once and the highest sum wins. A roll happens in
 * the dice table dialog: it opens on "Roll" and closes on "Done".
 */
object DiceDuelUi {
    data class ViewData(
        val state: DuelState,
        /** True while the dice table dialog is on screen. */
        val dialogOpen: Boolean = false,
        /** Player whose dice are on the table; kept after closing so they stay visible while the dialog fades. */
        val tablePlayer: Int = 0,
        /** True while the dice are tumbling; the sum is revealed afterwards. */
        val rolling: Boolean = false,
        val listener: DiceDuelViewListener,
    ) {
        /** The outcome stays hidden until the player who rolled last has closed the dice table. */
        val showResult: Boolean get() = state.isStageOver && !dialogOpen
    }
}

interface DiceDuelViewListener {
    fun onRollClick()
    fun onDoneClick()
    fun onTiebreakClick()
    fun onNextDuelClick()
    fun onBackClick()
}
