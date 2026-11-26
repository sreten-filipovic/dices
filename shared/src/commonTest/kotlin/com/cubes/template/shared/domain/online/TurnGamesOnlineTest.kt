package com.cubes.template.shared.domain.online

import com.cubes.template.shared.domain.jack.DiceJack
import com.cubes.template.shared.domain.jack.DiceJackOnline
import com.cubes.template.shared.domain.jack.DiceJackRules
import com.cubes.template.shared.domain.jack.JackPhase
import com.cubes.template.shared.domain.seven.SevenPhase
import com.cubes.template.shared.domain.seven.Sevens
import com.cubes.template.shared.domain.seven.SevensOnline
import com.cubes.template.shared.domain.seven.SevensRules
import com.cubes.template.shared.domain.straight.StraightPhase
import com.cubes.template.shared.domain.straight.StraightRoll
import com.cubes.template.shared.domain.straight.StraightRollOnline
import com.cubes.template.shared.domain.straight.StraightRollRules
import kotlin.test.Test
import kotlin.test.assertEquals

class TurnGamesOnlineTest {

    private var seq = 0

    private fun <S> TurnGameOnline<S>.play(match: TurnMatch<S>, vararg events: Pair<Int, TurnAction>): TurnMatch<S> =
        events.fold(match) { current, (player, action) -> apply(current, TurnEvent(++seq, player, action)).match }

    private fun names(count: Int) = List(count) { "P${it + 1}" }

    @Test
    fun diceJackRollsThreeDiceFirstAndThenOne() {
        val start = DiceJackOnline.start(DiceJack.newGame(names(2), 3))
        assertEquals(3, DiceJackRules.diceForNextRoll(start.state))

        val match = DiceJackOnline.play(
            start,
            0 to TurnAction.Roll(listOf(6, 5, 4)),
            0 to TurnAction.Roll(listOf(5)),
            0 to TurnAction.EndTurn,
        )
        assertEquals(listOf(6, 5, 4, 5), match.state.roundTurns[0]?.dice)
        assertEquals(1, match.state.currentPlayer)
    }

    @Test
    fun diceJackPlayerWhoLeftScoresNothingAndIsSkipped() {
        val match = DiceJackOnline.play(
            DiceJackOnline.start(DiceJack.newGame(names(3), 1)),
            1 to TurnAction.Leave,
            0 to TurnAction.Roll(listOf(6, 6, 5)),
            0 to TurnAction.EndTurn,
            2 to TurnAction.Roll(listOf(2, 2, 3)),
            2 to TurnAction.EndTurn,
        )
        assertEquals(JackPhase.MatchOver, match.state.phase)
        assertEquals(listOf(2, 0, 1), match.state.roundPoints)
        assertEquals(listOf(0), match.state.leaders)
    }

    @Test
    fun sevensRollsOnlyTheDiceStillOnTheTable() {
        val first = SevensOnline.play(
            SevensOnline.start(Sevens.newGame(names(2), 3)),
            0 to TurnAction.Roll(listOf(1, 6, 2, 2, 3, 3)),
        )
        assertEquals(4, SevensRules.diceForNextRoll(first.state))
    }

    @Test
    fun sevensSkippedOpenerDoesNotLimitTheOthers() {
        val match = SevensOnline.play(
            SevensOnline.start(Sevens.newGame(names(2), 3)),
            1 to TurnAction.Timeout,
        )
        assertEquals(1, match.state.currentPlayer)
        assertEquals(Sevens.MAX_ROLLS, match.state.rollLimit)
        assertEquals(SevenPhase.Playing, match.state.phase)
    }

    @Test
    fun straightRollCanOnlyFinishWhenTheTurnIsOverAndATimeoutBeforeThatScoresNothing() {
        val rolled = StraightRollOnline.play(
            StraightRollOnline.start(StraightRoll.newGame(names(2), 3)),
            0 to TurnAction.Roll(listOf(1, 1, 2, 3, 4, 5)),
            0 to TurnAction.EndTurn,
        )
        assertEquals(0, rolled.state.currentPlayer)
        assertEquals(1, StraightRollRules.diceForNextRoll(rolled.state))

        val timedOut = StraightRollOnline.play(rolled, 1 to TurnAction.Timeout)
        assertEquals(listOf(0, 0), timedOut.state.scores)
        assertEquals(1, timedOut.state.currentPlayer)

        val finished = StraightRollOnline.play(
            timedOut,
            1 to TurnAction.Roll(listOf(1, 2, 3, 4, 5, 6)),
            1 to TurnAction.EndTurn,
        )
        assertEquals(listOf(0, 10), finished.state.scores)
        assertEquals(StraightPhase.Playing, finished.state.phase)
        assertEquals(2, finished.state.round)
    }
}
