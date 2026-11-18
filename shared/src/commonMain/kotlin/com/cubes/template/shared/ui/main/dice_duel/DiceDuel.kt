package com.cubes.template.shared.ui.main.dice_duel

import com.cubes.template.shared.domain.duel.DuelState
import com.cubes.template.shared.ui.common.views.DieStyle

object DiceDuelUi {
    data class ViewData(
        val state: DuelState,
        val dieStyles: List<DieStyle> = emptyList(),
        val dialogOpen: Boolean = false,
        val tablePlayer: Int = 0,
        val rolling: Boolean = false,
        val listener: DiceDuelViewListener,
    ) {
        val showResult: Boolean get() = state.isStageOver && !dialogOpen

        fun dieStyleOf(player: Int): DieStyle = dieStyles.getOrNull(player) ?: DieStyle.WHITE
    }
}

interface DiceDuelViewListener {
    fun onRollClick()
    fun onDoneClick()
    fun onTiebreakClick()
    fun onNextDuelClick()
    fun onBackClick()
}
