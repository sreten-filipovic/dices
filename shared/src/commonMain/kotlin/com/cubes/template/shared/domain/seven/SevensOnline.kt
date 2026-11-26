package com.cubes.template.shared.domain.seven

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.online.TurnGameOnline
import com.cubes.template.shared.domain.online.TurnRules

object SevensRules : TurnRules<SevenState> {
    override fun playerCount(state: SevenState) = state.players.size
    override fun currentPlayer(state: SevenState) = state.currentPlayer.takeIf { state.phase == SevenPhase.Playing }
    override fun isRoundOver(state: SevenState) = state.phase == SevenPhase.RoundOver
    override fun isMatchOver(state: SevenState) = state.phase == SevenPhase.MatchOver
    override fun diceForNextRoll(state: SevenState) = if (state.canRoll) (if (state.turn.hasRolled) state.turn.active.size else Sevens.DICE_COUNT) else 0
    override fun roll(state: SevenState, roller: DiceRoller) = Sevens.roll(state, roller)
    override fun canEndTurn(state: SevenState) = state.canBank
    override fun endTurn(state: SevenState) = Sevens.bank(state)
    override fun skipTurn(state: SevenState) = Sevens.skip(state)
    override fun nextRound(state: SevenState) = Sevens.nextRound(state)
    override fun withLeft(state: SevenState, left: Set<Int>) = state.copy(left = left)
    override fun endMatch(state: SevenState) = state.copy(phase = SevenPhase.MatchOver)
}

val SevensOnline = TurnGameOnline(SevensRules)
