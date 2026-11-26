package com.cubes.template.shared.ui.main.sevens

import com.cubes.template.shared.domain.seven.Sevens
import com.cubes.template.shared.domain.seven.SevenPhase
import com.cubes.template.shared.domain.seven.SevenState
import com.cubes.template.shared.ui.main.online.turn.OnlinePlay
import com.cubes.template.shared.domain.seven.SevenTurn
import com.cubes.template.shared.ui.common.views.DieStyle

object SevensUi {
    data class ViewData(
        val state: SevenState,
        val dieStyles: List<DieStyle> = emptyList(),
        val dialogOpen: Boolean = false,
        val tablePlayer: Int = 0,
        val tableTurn: SevenTurn = SevenTurn(),
        val tableRollsLeft: Int = 0,
        val rollTicks: List<Int> = List(Sevens.DICE_COUNT) { 0 },
        val rolling: Boolean = false,
        val pairsShown: List<Set<Int>> = emptyList(),
        val online: OnlinePlay? = null,
        val listener: SevensViewListener,
    ) {
        fun dieStyleOf(player: Int): DieStyle = dieStyles.getOrNull(player) ?: DieStyle.WHITE

        val isMyTurn: Boolean
            get() = online != null && state.phase == SevenPhase.Playing && state.currentPlayer == online.myPlayer

        val canAct: Boolean
            get() = online == null || (isMyTurn && !online.submitting)

        val controlsTable: Boolean
            get() = online == null || (tablePlayer == online.myPlayer && !online.submitting)
    }
}

interface SevensViewListener {
    fun onRollClick()
    fun onBankClick()
    fun onNextRoundClick()
    fun onNewMatchClick()
    fun onHomeClick()
}
