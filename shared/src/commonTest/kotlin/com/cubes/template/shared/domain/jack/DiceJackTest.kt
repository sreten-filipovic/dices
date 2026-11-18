package com.cubes.template.shared.domain.jack

import com.cubes.template.shared.domain.game.DiceRoller
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiceJackTest {

    private fun roller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirst() }
    }

    private fun JackState.roll(vararg values: Int) = DiceJack.roll(this, roller(*values))
    private fun JackState.stand() = DiceJack.stand(this)

    private fun newGame(players: Int = 2, rounds: Int = 2) =
        DiceJack.newGame(List(players) { "P${it + 1}" }, rounds)

    @Test
    fun firstRollUsesThreeDiceAndEveryNextRollOne() {
        val first = newGame().roll(4, 5, 2)
        assertEquals(listOf(4, 5, 2), first.turn.dice)
        assertEquals(3, first.turn.diceLeft)

        val second = first.roll(6)
        assertEquals(listOf(4, 5, 2, 6), second.turn.dice)
        assertEquals(17, second.turn.score)
        assertEquals(2, second.turn.diceLeft)
    }

    @Test
    fun tripleOnTheFirstRollCountsAsTwentyOneAndEndsTheRolling() {
        val state = newGame().roll(2, 2, 2)

        assertTrue(state.turn.isTriple)
        assertEquals(21, state.turn.score)
        assertFalse(state.canRoll)
        assertEquals(state, state.roll(1))
    }

    @Test
    fun threeEqualDiceAfterTheFirstRollAreNotATriple() {
        val state = newGame().roll(2, 2, 3).roll(2)

        assertFalse(state.turn.isTriple)
        assertEquals(9, state.turn.score)
        assertTrue(state.canRoll)
    }

    @Test
    fun rollingStopsAtTwentyOneOnBustAndWhenTheDiceRunOut() {
        val exact = newGame().roll(6, 6, 5).roll(4)
        assertEquals(21, exact.turn.score)
        assertFalse(exact.turn.isBust)
        assertFalse(exact.canRoll)

        val bust = newGame().roll(6, 6, 5).roll(6)
        assertEquals(23, bust.turn.score)
        assertTrue(bust.turn.isBust)
        assertFalse(bust.canRoll)

        val allDice = newGame().roll(1, 1, 2).roll(1).roll(1).roll(1)
        assertEquals(7, allDice.turn.score)
        assertEquals(0, allDice.turn.diceLeft)
        assertFalse(allDice.canRoll)
    }

    @Test
    fun cannotStandBeforeRolling() {
        val fresh = newGame()
        assertFalse(fresh.canStand)
        assertEquals(fresh, fresh.stand())
    }

    @Test
    fun roundIsScoredByPlaceAndBustEarnsNothing() {
        val state = newGame(players = 4)
            .roll(6, 6, 5).stand()
            .roll(6, 6, 5).roll(6).stand()
            .roll(3, 3, 3).stand()
            .roll(6, 6, 5).stand()

        assertEquals(JackPhase.RoundOver, state.phase)
        assertEquals(listOf<Int?>(2, 0, 3, 2), state.roundPoints)
        assertEquals(listOf(2, 0, 3, 2), state.scores)
    }

    @Test
    fun everyoneBustMeansNobodyScores() {
        assertEquals(
            listOf(0, 0),
            DiceJack.roundPoints(listOf(JackTurn(listOf(6, 6, 5, 6)), JackTurn(listOf(6, 5, 5, 6)))),
        )
    }

    @Test
    fun nextRoundRotatesTheStarterAndClearsTheTable() {
        val roundOver = newGame()
            .roll(4, 5, 6).stand()
            .roll(1, 2, 3).stand()
        assertEquals(listOf(1, 0), roundOver.scores)

        val next = DiceJack.nextRound(roundOver)
        assertEquals(2, next.round)
        assertEquals(1, next.currentPlayer)
        assertEquals(listOf<JackTurn?>(null, null), next.roundTurns)
        assertEquals(listOf<Int?>(null, null), next.roundPoints)
        assertEquals(listOf(1, 0), next.scores)
        assertFalse(next.turn.hasRolled)
    }

    @Test
    fun matchEndsAfterTheLastRound() {
        val state = newGame(rounds = 1)
            .roll(4, 5, 6).stand()
            .roll(1, 2, 3).stand()

        assertEquals(JackPhase.MatchOver, state.phase)
        assertEquals(listOf(0), state.leaders)
        assertEquals(state, DiceJack.nextRound(state))
        assertEquals(state, state.roll(1, 1, 1))
    }

    @Test
    fun levelMatchHasSeveralLeaders() {
        val level = newGame(rounds = 1)
            .roll(4, 5, 6).stand()
            .roll(6, 5, 4).stand()
        assertEquals(listOf(0, 1), level.leaders)
    }
}
