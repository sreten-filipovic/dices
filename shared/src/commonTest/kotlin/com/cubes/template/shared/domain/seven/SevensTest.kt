package com.cubes.template.shared.domain.seven

import com.cubes.template.shared.domain.game.DiceRoller
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SevensTest {

    private fun roller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirst() }
    }

    private fun SevenState.roll(vararg values: Int) = Sevens.roll(this, roller(*values))
    private fun SevenState.bank() = Sevens.bank(this)

    private fun newGame(players: Int = 2, rounds: Int = 2) =
        Sevens.newGame(List(players) { "P${it + 1}" }, rounds)

    @Test
    fun pairsAddingUpToSevenAreRemoved() {
        val state = newGame().roll(6, 1, 5, 2, 4, 4)

        assertEquals(setOf(0, 1, 2, 3), state.turn.removed)
        assertEquals(8, state.turn.score)
    }

    @Test
    fun everyDieIsUsedInOnePairOnly() {
        assertEquals(listOf(setOf(0, 3)), Sevens.sevenPairs(listOf(6, 6, 6, 1, 2, 2)))
        assertEquals(emptyList(), Sevens.sevenPairs(listOf(1, 1, 2, 2, 3, 3)))
        assertEquals(
            listOf(setOf(4, 5), setOf(0, 1), setOf(2, 3)),
            Sevens.sevenPairs(listOf(3, 4, 3, 4, 1, 6)),
        )
    }

    @Test
    fun rerollOnlyTouchesDiceInPlayAndRemovesNewPairs() {
        val first = newGame().roll(6, 1, 2, 2, 3, 3)
        assertEquals(10, first.turn.score)

        val second = first.roll(5, 2, 6, 6)
        assertEquals(listOf(6, 1, 5, 2, 6, 6), second.turn.dice)
        assertEquals(listOf(setOf(0, 1), setOf(2, 3)), second.turn.pairs)
        assertEquals(12, second.turn.score)
        assertEquals(2, second.turn.rolls)
    }

    @Test
    fun turnHasAtMostThreeRolls() {
        val state = newGame().roll(1, 1, 1, 1, 1, 1).roll(1, 1, 1, 1, 1, 1).roll(2, 2, 2, 2, 2, 2)

        assertFalse(state.canRoll)
        assertEquals(state, state.roll(6, 6, 6, 6, 6, 6))
        assertEquals(12, state.turn.score)
    }

    @Test
    fun cannotBankBeforeRollingOrRollWithoutDice() {
        val fresh = newGame()
        assertFalse(fresh.canBank)
        assertEquals(fresh, fresh.bank())

        val empty = fresh.roll(6, 1, 5, 2, 4, 3)
        assertEquals(0, empty.turn.score)
        assertFalse(empty.canRoll)
        assertTrue(empty.canBank)
    }

    @Test
    fun openingPlayerSetsTheRollLimitForTheRound() {
        val afterOpener = newGame(players = 3)
            .roll(1, 1, 1, 1, 1, 1)
            .roll(6, 6, 6, 6, 5, 5)
            .bank()

        assertEquals(listOf(34, 0, 0), afterOpener.scores)
        assertEquals(1, afterOpener.currentPlayer)
        assertEquals(2, afterOpener.rollLimit)

        val second = afterOpener.roll(2, 2, 2, 2, 2, 2).roll(3, 3, 3, 3, 3, 3)
        assertFalse(second.canRoll)

        val third = second.bank().roll(4, 4, 4, 4, 4, 4).bank()
        assertEquals(2, third.rollLimit)
        assertEquals(listOf(34, 18, 24), third.scores)
        assertEquals(SevenPhase.RoundOver, third.phase)
    }

    @Test
    fun nextRoundRotatesTheStarterAndRestoresThreeRolls() {
        val roundOver = newGame()
            .roll(1, 1, 1, 1, 1, 1).bank()
            .roll(2, 2, 2, 2, 2, 2).bank()
        assertEquals(1, roundOver.rollLimit)
        assertEquals(listOf<Int?>(6, 12), roundOver.roundScores)
        assertEquals(listOf(1, 1, 1, 1, 1, 1), roundOver.roundTurns[0]?.dice)
        assertEquals(listOf(2, 2, 2, 2, 2, 2), roundOver.roundTurns[1]?.dice)

        val next = Sevens.nextRound(roundOver)
        assertEquals(2, next.round)
        assertEquals(1, next.currentPlayer)
        assertEquals(Sevens.MAX_ROLLS, next.rollLimit)
        assertEquals(listOf<Int?>(null, null), next.roundScores)
        assertEquals(listOf<SevenTurn?>(null, null), next.roundTurns)
        assertEquals(listOf(6, 12), next.scores)
        assertFalse(next.turn.hasRolled)
    }

    @Test
    fun matchEndsAfterTheLastRound() {
        val state = newGame(rounds = 1)
            .roll(5, 5, 5, 5, 5, 5).bank()
            .roll(2, 2, 2, 2, 2, 2).bank()

        assertEquals(SevenPhase.MatchOver, state.phase)
        assertEquals(listOf(0), state.leaders)
        assertEquals(state, Sevens.nextRound(state))
        assertEquals(state, state.roll(1, 1, 1, 1, 1, 1))
    }

    @Test
    fun levelMatchHasSeveralLeaders() {
        val state = newGame(rounds = 1)
            .roll(3, 3, 3, 3, 3, 3).bank()
            .roll(3, 3, 3, 3, 3, 3).bank()

        assertEquals(listOf(0, 1), state.leaders)
    }
}
