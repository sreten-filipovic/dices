package com.cubes.template.shared.ui.main.risk_it

import com.cubes.template.shared.domain.risk.RiskState
import com.cubes.template.shared.domain.risk.RiskTurn
import com.cubes.template.shared.ui.common.views.DieStyle

object RiskItUi {
    data class ViewData(
        val state: RiskState,
        val dieStyles: List<DieStyle> = emptyList(),
        val dialogOpen: Boolean = false,
        val tablePlayer: Int = 0,
        val tableTurn: RiskTurn = RiskTurn(),
        val rollTick: Int = 0,
        val rolling: Boolean = false,
        val listener: RiskItViewListener,
    ) {
        fun dieStyleOf(player: Int): DieStyle = dieStyles.getOrNull(player) ?: DieStyle.WHITE
    }
}

interface RiskItViewListener {
    fun onRollClick()
    fun onStopClick()
    fun onNextRoundClick()
    fun onNewMatchClick()
    fun onHomeClick()
}
