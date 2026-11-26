package com.cubes.template.shared.ui.main.dice_jack

import com.cubes.template.shared.domain.jack.DiceJack
import com.cubes.template.shared.domain.jack.JackPhase
import com.cubes.template.shared.domain.jack.JackState
import com.cubes.template.shared.ui.main.online.turn.OnlinePlay
import com.cubes.template.shared.domain.jack.JackTurn
import com.cubes.template.shared.ui.common.views.DieStyle

object DiceJackUi {
    data class ViewData(
        val state: JackState,
        val dieStyles: List<DieStyle> = emptyList(),
        val dialogOpen: Boolean = false,
        val tablePlayer: Int = 0,
        val tableTurn: JackTurn = JackTurn(),
        val rollTicks: List<Int> = List(DiceJack.DICE_COUNT) { 0 },
        val rolling: Boolean = false,
        val online: OnlinePlay? = null,
        val listener: DiceJackViewListener,
    ) {
        fun dieStyleOf(player: Int): DieStyle = dieStyles.getOrNull(player) ?: DieStyle.WHITE

        val isMyTurn: Boolean
            get() = online != null && state.phase == JackPhase.Playing && state.currentPlayer == online.myPlayer

        val canAct: Boolean
            get() = online == null || (isMyTurn && !online.submitting)

        val controlsTable: Boolean
            get() = online == null || (tablePlayer == online.myPlayer && !online.submitting)
    }
}

interface DiceJackViewListener {
    fun onRollClick()
    fun onStandClick()
    fun onNextRoundClick()
    fun onNewMatchClick()
    fun onHomeClick()
}
