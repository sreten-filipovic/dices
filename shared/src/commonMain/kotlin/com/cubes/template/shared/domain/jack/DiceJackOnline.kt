package com.cubes.template.shared.domain.jack

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.online.TurnGameOnline
import com.cubes.template.shared.domain.online.TurnRules

object DiceJackRules : TurnRules<JackState> {
    override fun playerCount(state: JackState) = state.players.size
    override fun currentPlayer(state: JackState) = state.currentPlayer.takeIf { state.phase == JackPhase.Playing }
    override fun isRoundOver(state: JackState) = state.phase == JackPhase.RoundOver
    override fun isMatchOver(state: JackState) = state.phase == JackPhase.MatchOver
    override fun diceForNextRoll(state: JackState) = if (state.canRoll) (if (state.turn.hasRolled) 1 else DiceJack.FIRST_ROLL_DICE) else 0
    override fun roll(state: JackState, roller: DiceRoller) = DiceJack.roll(state, roller)
    override fun canEndTurn(state: JackState) = state.canStand
    override fun endTurn(state: JackState) = DiceJack.stand(state)
    override fun skipTurn(state: JackState) = DiceJack.skip(state)
    override fun nextRound(state: JackState) = DiceJack.nextRound(state)
    override fun withLeft(state: JackState, left: Set<Int>) = state.copy(left = left)
    override fun endMatch(state: JackState) = state.copy(phase = JackPhase.MatchOver)
}

val DiceJackOnline = TurnGameOnline(DiceJackRules)
