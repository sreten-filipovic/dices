package com.cubes.template.shared.domain.risk

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.online.TurnGameOnline
import com.cubes.template.shared.domain.online.TurnRules

object RiskItRules : TurnRules<RiskState> {
    override fun playerCount(state: RiskState) = state.players.size
    override fun currentPlayer(state: RiskState) = state.currentPlayer.takeIf { state.phase == RiskPhase.Playing }
    override fun isRoundOver(state: RiskState) = state.phase == RiskPhase.RoundOver
    override fun isMatchOver(state: RiskState) = state.phase == RiskPhase.MatchOver
    override fun diceForNextRoll(state: RiskState) = if (state.canRoll) 1 else 0
    override fun roll(state: RiskState, roller: DiceRoller) = RiskIt.roll(state, roller)
    override fun canEndTurn(state: RiskState) = state.canStop
    override fun endTurn(state: RiskState) = RiskIt.stop(state)
    override fun skipTurn(state: RiskState) = RiskIt.skip(state)
    override fun nextRound(state: RiskState) = RiskIt.nextRound(state)
    override fun withLeft(state: RiskState, left: Set<Int>) = state.copy(left = left)
    override fun endMatch(state: RiskState) = state.copy(phase = RiskPhase.MatchOver)
}

val RiskItOnline = TurnGameOnline(RiskItRules)
