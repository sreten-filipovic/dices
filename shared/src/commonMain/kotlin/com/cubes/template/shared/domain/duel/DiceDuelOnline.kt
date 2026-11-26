package com.cubes.template.shared.domain.duel

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.online.TurnGameOnline
import com.cubes.template.shared.domain.online.TurnRules

object DiceDuelRules : TurnRules<DuelState> {
    override fun playerCount(state: DuelState) = state.players.size
    override fun currentPlayer(state: DuelState) = state.currentPlayer.takeIf { !state.isOver }
    override fun isRoundOver(state: DuelState) = state.isStageOver && state.needsTiebreak
    override fun isMatchOver(state: DuelState) = state.isOver
    override fun diceForNextRoll(state: DuelState) = if (currentPlayer(state) != null) state.diceCount else 0
    override fun roll(state: DuelState, roller: DiceRoller) = DiceDuel.roll(state, roller)
    override fun canEndTurn(state: DuelState) = false
    override fun endTurn(state: DuelState) = state
    override fun skipTurn(state: DuelState) = DiceDuel.skip(state)
    override fun nextRound(state: DuelState) = DiceDuel.tiebreak(state)
    override fun withLeft(state: DuelState, left: Set<Int>) = DiceDuel.withLeft(state, left)
    override fun endMatch(state: DuelState) = DiceDuel.forfeit(state)
    override fun isFinished(state: DuelState) = state.ended
    override fun rematch(state: DuelState) = DiceDuel.rematch(state)
}

val DiceDuelOnline = TurnGameOnline(DiceDuelRules)
