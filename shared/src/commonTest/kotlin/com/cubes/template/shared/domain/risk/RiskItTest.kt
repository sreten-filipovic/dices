package com.cubes.template.shared.domain.risk

import com.cubes.template.shared.domain.game.DiceRoller
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RiskItTest {

    private fun roller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirst() }
    }

    private fun RiskState.roll(vararg values: Int) =
        values.fold(this) { state, value -> RiskIt.roll(state, roller(value)) }
    private fun RiskState.stop() = RiskIt.stop(this)

    private fun newGame(players: Int = 2, rounds: Int = 2) =
        RiskIt.newGame(List(players) { "P${it + 1}" }, rounds)

    @Test
    fun rollsAreAddedUp() {
        val state = newGame().roll(4, 6, 2)

        assertEquals(12, state.turn.score)
        assertEquals(7, state.turn.rollsLeft)
    }

    @Test
    fun aOneResetsTheScoreButTheTurnGoesOn() {
        val afterOne = newGame().roll(4, 6, 1)
        assertEquals(0, afterOne.turn.score)
        assertTrue(afterOne.turn.lostEverything)
        assertTrue(afterOne.canRoll)

        val recovered = afterOne.roll(5, 3)
        assertEquals(8, recovered.turn.score)
        assertFalse(recovered.turn.lostEverything)
    }

    @Test
    fun turnHasAtMostTenRolls() {
        val state = newGame().roll(2, 2, 2, 2, 2, 2, 2, 2, 2, 2)

        assertEquals(20, state.turn.score)
        assertFalse(state.canRoll)
        assertEquals(state, state.roll(6))
        assertTrue(state.canStop)
    }

    @Test
    fun cannotStopBeforeRolling() {
        val fresh = newGame()
        assertFalse(fresh.canStop)
        assertEquals(fresh, fresh.stop())
    }

    @Test
    fun stoppingBanksTheScoreOfTheTurn() {
        val state = newGame(players = 3)
            .roll(6, 6).stop()
            .roll(5, 1, 3).stop()
            .roll(4).stop()

        assertEquals(RiskPhase.RoundOver, state.phase)
        assertEquals(listOf(12, 3, 4), state.scores)
        assertEquals(listOf(12, 3, 4), state.roundTurns.map { it?.score })
    }

    @Test
    fun scoresAddUpOverTheRounds() {
        val state = RiskIt.nextRound(newGame().roll(6, 4).stop().roll(5, 1).stop())
            .roll(3).stop()
            .roll(2, 2).stop()

        assertEquals(RiskPhase.MatchOver, state.phase)
        assertEquals(listOf(14, 3), state.scores)
        assertEquals(listOf(0), state.leaders)
    }

    @Test
    fun levelMatchHasSeveralLeaders() {
        val state = newGame(rounds = 1).roll(6, 4).stop().roll(5, 5).stop()

        assertEquals(listOf(0, 1), state.leaders)
    }

    @Test
    fun nextRoundRotatesTheStarterAndClearsTheTurns() {
        val next = RiskIt.nextRound(newGame().roll(6).stop().roll(2).stop())

        assertEquals(2, next.round)
        assertEquals(1, next.currentPlayer)
        assertEquals(listOf<RiskTurn?>(null, null), next.roundTurns)
        assertEquals(listOf(6, 2), next.scores)
        assertFalse(next.turn.hasRolled)
    }

    @Test
    fun matchEndsAfterTheLastRound() {
        val state = newGame(rounds = 1).roll(6).stop().roll(2).stop()

        assertEquals(RiskPhase.MatchOver, state.phase)
        assertEquals(listOf(0), state.leaders)
        assertEquals(state, RiskIt.nextRound(state))
        assertEquals(state, state.roll(6))
    }
}
