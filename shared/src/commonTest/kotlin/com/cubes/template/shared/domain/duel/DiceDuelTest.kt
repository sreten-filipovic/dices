package com.cubes.template.shared.domain.duel

import com.cubes.template.shared.domain.game.DiceRoller
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiceDuelTest {

    private fun roller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirst() }
    }

    private fun newGame(players: Int = 2, diceCount: Int = 1) =
        DiceDuel.newGame(List(players) { "P${it + 1}" }, diceCount)

    private fun DuelState.playOut(vararg values: Int): DuelState {
        val roller = roller(*values)
        var state = this
        while (!state.isStageOver) state = DiceDuel.roll(state, roller)
        return state
    }

    @Test
    fun higherSingleDieWins() {
        val state = newGame().playOut(3, 5)

        assertTrue(state.isOver)
        assertEquals(listOf(listOf(3), listOf(5)), state.hands)
        assertEquals(1, state.winner)
        assertEquals(listOf(0, 1), state.wins)
    }

    @Test
    fun severalDiceCompareBySum() {
        val state = newGame(diceCount = 3).playOut(6, 1, 1, 4, 2, 3)

        assertEquals(8, state.sumOf(0))
        assertEquals(9, state.sumOf(1))
        assertEquals(1, state.winner)
        assertEquals(listOf(0, 1), state.wins)
    }

    @Test
    fun equalSumsAreDecidedByATiebreak() {
        val tied = newGame().playOut(4, 4)

        assertTrue(tied.isStageOver)
        assertFalse(tied.isOver)
        assertTrue(tied.needsTiebreak)
        assertNull(tied.winner)
        assertEquals(listOf(0, 0), tied.wins)
        assertEquals(tied, DiceDuel.nextDuel(tied))

        val rematch = DiceDuel.tiebreak(tied)
        assertEquals(listOf(null, null), rematch.hands)
        assertEquals(0, rematch.currentPlayer)

        val stillTied = rematch.playOut(2, 2)
        assertTrue(stillTied.needsTiebreak)

        val decided = DiceDuel.tiebreak(stillTied).playOut(1, 3)
        assertTrue(decided.isOver)
        assertEquals(1, decided.winner)
        assertEquals(listOf(0, 1), decided.wins)
        assertEquals(listOf(4, 2, 3), decided.sumsOf(1))
    }

    @Test
    fun onlyTheTiedLeadersRollAgain() {
        val tied = newGame(players = 4).playOut(5, 2, 5, 3)
        assertEquals(listOf(0, 2), tied.leaders)
        assertEquals(listOf(0, 0, 0, 0), tied.wins)

        val rematch = DiceDuel.tiebreak(tied)
        assertTrue(rematch.isTiebreak)
        assertEquals(listOf(0, 2), rematch.participants)
        assertEquals(listOf(null, listOf(2), null, listOf(3)), rematch.hands)

        val afterFirst = DiceDuel.roll(rematch, roller(1))
        assertEquals(2, afterFirst.currentPlayer)

        val decided = DiceDuel.roll(afterFirst, roller(2))
        assertEquals(2, decided.winner)
        assertEquals(listOf(0, 0, 1, 0), decided.wins)
        assertEquals(listOf(2, 4, 1, 3), decided.players.indices.map(decided::placeOf))

        val next = DiceDuel.nextDuel(decided)
        assertFalse(next.isTiebreak)
        assertEquals(listOf<List<Int>?>(null, null, null, null), next.hands)
        assertEquals(1, next.currentPlayer)
    }

    @Test
    fun tiebreakIsOnlyForASharedFirstPlace() {
        val state = newGame(players = 3).playOut(6, 2, 2)

        assertEquals(0, state.winner)
        assertEquals(listOf(1, 2, 2), state.players.indices.map(state::placeOf))
        assertEquals(state, DiceDuel.tiebreak(state))
    }

    @Test
    fun playersRollInTurnAndTheDuelEndsWhenAllHaveRolled() {
        var state = newGame(players = 4)
        val roller = roller(2, 6, 1, 4)
        listOf(0, 1, 2, 3).forEach { expected ->
            assertFalse(state.isStageOver)
            assertEquals(expected, state.currentPlayer)
            assertEquals(emptyList(), state.leaders)
            state = DiceDuel.roll(state, roller)
        }

        assertNull(state.currentPlayer)
        assertEquals(1, state.winner)
        assertEquals(listOf(0, 1, 0, 0), state.wins)
        assertEquals(state, DiceDuel.roll(state, roller(6)))
    }

    @Test
    fun nextDuelKeepsScoreAndMovesTheStarter() {
        val finished = newGame(players = 3).playOut(6, 1, 2)
        val next = DiceDuel.nextDuel(finished)

        assertEquals(listOf(1, 0, 0), next.wins)
        assertEquals(1, next.currentPlayer)
        val almostDone = DiceDuel.roll(DiceDuel.roll(next, roller(3)), roller(3))
        assertEquals(0, almostDone.currentPlayer)
        assertEquals(almostDone, DiceDuel.nextDuel(almostDone))
    }

    @Test
    fun setupIsValidated() {
        assertFailsWith<IllegalArgumentException> { DiceDuel.newGame(listOf("Solo"), diceCount = 1) }
        assertFailsWith<IllegalArgumentException> { newGame(diceCount = 4) }
    }
}
