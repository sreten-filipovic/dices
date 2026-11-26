package com.cubes.template.shared.domain.straight

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.online.TurnGameOnline
import com.cubes.template.shared.domain.online.TurnRules

object StraightRollRules : TurnRules<StraightState> {
    override fun playerCount(state: StraightState) = state.players.size
    override fun currentPlayer(state: StraightState) = state.currentPlayer.takeIf { state.phase == StraightPhase.Playing }
    override fun isRoundOver(state: StraightState) = state.phase == StraightPhase.RoundOver
    override fun isMatchOver(state: StraightState) = state.phase == StraightPhase.MatchOver
    override fun diceForNextRoll(state: StraightState) = if (state.canRoll) (if (state.turn.hasRolled) state.turn.remaining.size else StraightRoll.DICE_COUNT) else 0
    override fun roll(state: StraightState, roller: DiceRoller) = StraightRoll.roll(state, roller)
    override fun canEndTurn(state: StraightState) = state.canFinish
    override fun endTurn(state: StraightState) = StraightRoll.finish(state)
    override fun skipTurn(state: StraightState) = StraightRoll.skip(state)
    override fun nextRound(state: StraightState) = StraightRoll.nextRound(state)
    override fun withLeft(state: StraightState, left: Set<Int>) = state.copy(left = left)
    override fun endMatch(state: StraightState) = state.copy(phase = StraightPhase.MatchOver)
}

val StraightRollOnline = TurnGameOnline(StraightRollRules)
