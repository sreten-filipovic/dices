package com.cubes.template.shared.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class GameEngineTest {

    /** Returns the given values in order. */
    private class FakeRoller(vararg values: Int) : DiceRoller {
        private val queue = ArrayDeque(values.toList())
        override fun roll(): Int = queue.removeFirst()
    }

    private fun GameState.act(action: GameAction, roller: DiceRoller = FakeRoller()) =
        GameEngine.reduce(this, action, roller)

    private fun GameState.rollFirst(vararg dice: Int) = act(GameAction.RollFirst, FakeRoller(*dice))

    @Test
    fun allPlayersRollFirstThenAllRollSecond() {
        var s = GameEngine.newGame(listOf("A", "B", "C"), targetWins = 3)
        assertEquals("p1", s.currentPlayer.id)

        s = s.rollFirst(1, 1, 2, 3, 4)
        assertEquals(RoundPhase.FirstRoll, s.phase)
        assertEquals("p2", s.currentPlayer.id)
        s = s.rollFirst(2, 2, 2, 3, 4)
        assertEquals("p3", s.currentPlayer.id)
        s = s.rollFirst(6, 5, 4, 1, 1)

        assertEquals(RoundPhase.SecondRoll, s.phase)
        assertEquals("p1", s.currentPlayer.id)

        // Second-phase actions in first phase are ignored and vice versa
        assertSame(s, s.act(GameAction.RollFirst))
        assertSame(s, s.act(GameAction.RerollSelected)) // nothing selected

        s = s.act(GameAction.ToggleSelect(3)).act(GameAction.ToggleSelect(4))
        assertEquals(setOf(3, 4), s.currentTurn.selected)
        s = s.act(GameAction.RerollSelected, FakeRoller(1, 1))
        assertEquals(listOf(1, 1, 2, 1, 1), s.turnOf("p1").dice)
        assertEquals("p2", s.currentPlayer.id)

        s = s.act(GameAction.Keep)
        s = s.act(GameAction.Keep)

        // p1 has four of a kind (1s) and wins
        assertEquals(RoundPhase.RoundOver(listOf("p1")), s.phase)
        assertEquals(1, s.scoreOf("p1"))
        assertEquals(0, s.scoreOf("p2"))
    }

    @Test
    fun tieGivesPointToAllBestPlayers() {
        var s = GameEngine.newGame(listOf("A", "B"), targetWins = 2)
        s = s.rollFirst(1, 2, 3, 4, 5).rollFirst(5, 4, 3, 2, 1)
        s = s.act(GameAction.Keep).act(GameAction.Keep)
        assertEquals(RoundPhase.RoundOver(listOf("p1", "p2")), s.phase)
        assertEquals(1, s.scoreOf("p1"))
        assertEquals(1, s.scoreOf("p2"))
    }

    @Test
    fun startingPlayerRotatesAndMatchEndsWithSingleLeader() {
        var s = GameEngine.newGame(listOf("A", "B"), targetWins = 1)

        // Round 1: tie -> both at 1, no single leader -> round continues
        s = s.rollFirst(6, 6, 1, 2, 3).rollFirst(6, 6, 3, 2, 1).act(GameAction.Keep).act(GameAction.Keep)
        assertIs<RoundPhase.RoundOver>(s.phase)

        s = s.act(GameAction.NextRound)
        assertEquals(2, s.round)
        assertEquals(RoundPhase.FirstRoll, s.phase)
        assertEquals("p2", s.currentPlayer.id)
        assertEquals(listOf("p2", "p1"), s.turnOrder.map { it.id })
        assertEquals(emptyList(), s.turnOf("p1").dice)

        // Round 2: p2 (starting) loses, p1 wins -> match over
        s = s.rollFirst(1, 2, 3, 4, 6).rollFirst(4, 4, 4, 1, 2).act(GameAction.Keep).act(GameAction.Keep)
        assertEquals(RoundPhase.MatchOver(winnerId = "p1", roundWinnerIds = listOf("p1")), s.phase)
        assertEquals(2, s.scoreOf("p1"))

        // NextRound is ignored after the match ended
        assertSame(s, s.act(GameAction.NextRound))
    }
}
