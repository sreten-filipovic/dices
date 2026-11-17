package com.cubes.template.shared.ui.main.dice_duel

import com.cubes.template.shared.domain.duel.DuelState

/** Dice Duel: two players on one device, one roll each, the higher sum wins. */
object DiceDuelUi {
    data class ViewData(
        val state: DuelState = DuelState(),
        /** Per player; grows on every roll and drives that player's dice animation. */
        val rollKeys: List<Int> = listOf(0, 0),
        /** Player whose dice are tumbling right now; their result is revealed afterwards. */
        val rollingPlayer: Int? = null,
        /** Score shown on screen; trails [state] until the deciding roll has settled. */
        val wins: List<Int> = listOf(0, 0),
        val listener: DiceDuelViewListener,
    ) {
        val rolling: Boolean get() = rollingPlayer != null
        val showResult: Boolean get() = state.isOver && !rolling
    }
}

interface DiceDuelViewListener {
    fun onDiceCountChanged(diceCount: Int)
    fun onRollClick()
    fun onNextDuelClick()
    fun onBackClick()
}
