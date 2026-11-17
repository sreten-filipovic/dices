package com.cubes.template.shared.domain.duel

import com.cubes.template.shared.domain.game.DiceRoller
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiceDuelTest {

    private fun roller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirst() }
    }

    @Test
    fun higherSingleDieWins() {
        val roller = roller(3, 5)
        val state = DiceDuel.roll(DiceDuel.roll(DuelState(), roller), roller)

        assertTrue(state.isOver)
        assertEquals(listOf(listOf(3), listOf(5)), state.hands)
        assertEquals(1, state.winner)
        assertEquals(listOf(0, 1), state.wins)
    }

    @Test
    fun twoDiceCompareBySum() {
        val roller = roller(6, 1, 4, 2)
        val start = DiceDuel.setDiceCount(DuelState(), 2)
        val state = DiceDuel.roll(DiceDuel.roll(start, roller), roller)

        assertEquals(0, state.winner)
        assertEquals(listOf(1, 0), state.wins)
    }

    @Test
    fun equalSumsAreADraw() {
        val roller = roller(4, 4)
        val state = DiceDuel.roll(DiceDuel.roll(DuelState(), roller), roller)

        assertTrue(state.isOver)
        assertNull(state.winner)
        assertEquals(listOf(0, 0), state.wins)
    }

    @Test
    fun diceCountIsLockedOnceSomeoneRolled() {
        val afterFirstRoll = DiceDuel.roll(DuelState(), roller(2))

        assertFalse(afterFirstRoll.canChangeDiceCount)
        assertEquals(1, DiceDuel.setDiceCount(afterFirstRoll, 2).diceCount)
        assertEquals(1, DiceDuel.setDiceCount(DuelState(), 3).diceCount)
    }

    @Test
    fun nextDuelKeepsScoreAndSwapsStarter() {
        val roller = roller(6, 1)
        val finished = DiceDuel.roll(DiceDuel.roll(DuelState(), roller), roller)
        val next = DiceDuel.nextDuel(finished)

        assertEquals(listOf(1, 0), next.wins)
        assertEquals(1, next.currentPlayer)
        assertTrue(next.canChangeDiceCount)
        // An unfinished duel cannot be skipped
        val unfinished = DiceDuel.roll(next, roller(3))
        assertEquals(unfinished, DiceDuel.nextDuel(unfinished))
    }
}
