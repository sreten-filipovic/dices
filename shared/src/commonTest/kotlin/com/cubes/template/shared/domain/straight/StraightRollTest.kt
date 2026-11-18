package com.cubes.template.shared.domain.straight

import com.cubes.template.shared.domain.game.DiceRoller
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StraightRollTest {

    private fun roller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirst() }
    }

    private fun StraightState.roll(vararg values: Int) = StraightRoll.roll(this, roller(*values))
    private fun StraightState.finish() = StraightRoll.finish(this)

    private fun newGame(players: Int = 2, rounds: Int = 2) =
        StraightRoll.newGame(List(players) { "P${it + 1}" }, rounds)

    @Test
    fun firstRollIsFreeAndEveryRerollCostsOnePoint() {
        val first = newGame().roll(1, 1, 2, 3, 4, 5)
        assertEquals(10, first.turn.points)

        val second = first.roll(1)
        assertEquals(listOf(1, 1, 2, 3, 4, 5), second.turn.dice)
        assertEquals(9, second.turn.points)

        val third = second.roll(6)
        assertEquals(8, third.turn.points)
        assertTrue(third.turn.isStraight)
    }

    @Test
    fun oneDieOfEveryValueIsKeptAndTheRestStaysOnTheTable() {
        val turn = newGame().roll(6, 6, 6, 1, 2, 2).turn

        assertEquals(setOf(0, 3, 4), turn.kept)
        assertEquals(setOf(1, 2, 5), turn.remaining)
        assertEquals(setOf(6, 1, 2), turn.keptValues)
    }

    @Test
    fun rerolledDieRepeatingAKeptValueStaysOnTheTable() {
        val turn = newGame().roll(6, 6, 6, 1, 2, 3).roll(1, 5).turn

        assertEquals(listOf(6, 1, 5, 1, 2, 3), turn.dice)
        assertEquals(setOf(0, 2, 3, 4, 5), turn.kept)
        assertEquals(setOf(1), turn.remaining)
        assertFalse(turn.isStraight)
    }

    @Test
    fun onlyDuplicatesAreRerolled() {
        val state = newGame().roll(6, 6, 6, 1, 2, 3).roll(4, 5)

        assertEquals(listOf(6, 4, 5, 1, 2, 3), state.turn.dice)
        assertTrue(state.turn.isStraight)
        assertEquals(9, state.turn.points)
    }

    @Test
    fun straightOnTheFirstRollKeepsAllPoints() {
        val state = newGame().roll(3, 1, 6, 2, 5, 4)

        assertTrue(state.turn.isOver)
        assertFalse(state.canRoll)
        assertEquals(state, state.roll(1))
        assertEquals(listOf(10, 0), state.finish().scores)
    }

    @Test
    fun turnCannotBeFinishedWithoutAStraightWhilePointsRemain() {
        val state = newGame().roll(1, 1, 2, 3, 4, 5)

        assertFalse(state.canFinish)
        assertEquals(state, state.finish())
    }

    @Test
    fun turnEndsWithZeroAfterTenRerolls() {
        var state = newGame().roll(1, 1, 2, 3, 4, 5)
        repeat(StraightRoll.START_POINTS) { state = state.roll(1) }

        assertEquals(0, state.turn.points)
        assertEquals(11, state.turn.rolls)
        assertTrue(state.turn.isOver)
        assertFalse(state.canRoll)
        assertEquals(listOf(0, 0), state.finish().scores)
    }

    @Test
    fun straightOnTheLastPaidRollScoresZero() {
        var state = newGame().roll(1, 1, 2, 3, 4, 5)
        repeat(StraightRoll.START_POINTS - 1) { state = state.roll(1) }
        state = state.roll(6)

        assertTrue(state.turn.isStraight)
        assertEquals(0, state.turn.points)
    }

    @Test
    fun roundAndMatchFlow() {
        val roundOver = newGame()
            .roll(1, 2, 3, 4, 5, 6).finish()
            .roll(1, 1, 2, 3, 4, 5).roll(6).finish()
        assertEquals(StraightPhase.RoundOver, roundOver.phase)
        assertEquals(listOf<Int?>(10, 9), roundOver.roundScores)

        val next = StraightRoll.nextRound(roundOver)
        assertEquals(2, next.round)
        assertEquals(1, next.currentPlayer)
        assertEquals(listOf<Int?>(null, null), next.roundScores)
        assertFalse(next.turn.hasRolled)
        assertEquals(10, next.turn.points)

        val matchOver = next
            .roll(1, 2, 3, 4, 5, 6).finish()
            .roll(1, 2, 3, 4, 5, 6).finish()
        assertEquals(StraightPhase.MatchOver, matchOver.phase)
        assertEquals(listOf(20, 19), matchOver.scores)
        assertEquals(listOf(0), matchOver.leaders)
        assertEquals(matchOver, StraightRoll.nextRound(matchOver))
    }
}
