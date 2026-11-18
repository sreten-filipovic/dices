package com.cubes.template.shared.ui.main.straight_roll

import com.cubes.template.shared.domain.straight.StraightRoll
import com.cubes.template.shared.domain.straight.StraightState
import com.cubes.template.shared.domain.straight.StraightTurn
import com.cubes.template.shared.ui.common.views.DieStyle

object StraightRollUi {
    const val PAUSE_BEFORE_MOVE_MILLIS = 250L
    const val MOVE_MILLIS = 450
    const val MOVE_SETTLE_MILLIS = 60L

    data class ViewData(
        val state: StraightState,
        val dieStyles: List<DieStyle> = emptyList(),
        val dialogOpen: Boolean = false,
        val tablePlayer: Int = 0,
        val tableTurn: StraightTurn = StraightTurn(),
        val rollTicks: List<Int> = List(StraightRoll.DICE_COUNT) { 0 },
        val rolling: Boolean = false,
        val onTable: Set<Int> = emptySet(),
        val moving: Set<Int> = emptySet(),
        val collected: Set<Int> = emptySet(),
        val listener: StraightRollViewListener,
    ) {
        fun dieStyleOf(player: Int): DieStyle = dieStyles.getOrNull(player) ?: DieStyle.WHITE
    }
}

interface StraightRollViewListener {
    fun onRollClick()
    fun onDoneClick()
    fun onNextRoundClick()
    fun onNewMatchClick()
    fun onHomeClick()
}
