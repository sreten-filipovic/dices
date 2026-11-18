package com.cubes.template.shared.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GameEngineTest {

    private class FakeRoller(vararg values: Int) : DiceRoller {
        private val queue = ArrayDeque(values.toList())
        override fun roll(): Int = queue.removeFirst()
    }

    private fun GameState.act(action: GameAction, roller: DiceRoller = FakeRoller()) =
        GameEngine.reduce(this, action, roller)

    private fun GameState.rollFirst(vararg dice: Int) = act(GameAction.RollFirst, FakeRoller(*dice))

    private fun GameState.play(vararg dice: Int) = rollFirst(*dice).act(GameAction.Keep)

    private val fiveOfAKind = intArrayOf(6, 6, 6, 6, 6)
    private val threeOfAKind = intArrayOf(6, 6, 6, 1, 2)
    private val pairOfTwos = intArrayOf(2, 2, 1, 3, 4)
    private val nothing = intArrayOf(1, 2, 3, 4, 6)

    @Test
    fun eachPlayerPlaysBothRollsBeforeTheNextPlayer() {
        var s = GameEngine.newGame(listOf("A", "B", "C"), totalRounds = 3)
        assertEquals("p1", s.currentPlayer.id)
        assertEquals(RoundPhase.FirstRoll, s.phase)

        s = s.rollFirst(1, 1, 2, 3, 4)
        assertEquals(RoundPhase.SecondRoll, s.phase)
        assertEquals("p1", s.currentPlayer.id)

        assertSame(s, s.act(GameAction.RollFirst))
        assertSame(s, s.act(GameAction.RerollSelected))

        s = s.act(GameAction.ToggleSelect(3)).act(GameAction.ToggleSelect(4))
        assertEquals(setOf(3, 4), s.currentTurn.selected)
        s = s.act(GameAction.RerollSelected, FakeRoller(1, 1))
        assertEquals(listOf(1, 1, 2, 1, 1), s.turnOf("p1").dice)
        assertEquals(true, s.turnOf("p1").rerolled)

        assertEquals("p2", s.currentPlayer.id)
        assertEquals(RoundPhase.FirstRoll, s.phase)
        s = s.play(*pairOfTwos)
        assertEquals("p3", s.currentPlayer.id)
        s = s.play(*nothing)

        assertEquals(RoundPhase.RoundOver(listOf("p1")), s.phase)
    }

    @Test
    fun placesAreWorthNMinusOneDownToZero() {
        var s = GameEngine.newGame(listOf("A", "B", "C", "D"), totalRounds = 3)
        s = s.play(*pairOfTwos).play(*fiveOfAKind).play(*nothing).play(*threeOfAKind)

        assertEquals(mapOf("p2" to 3, "p4" to 2, "p1" to 1, "p3" to 0), s.lastRoundPoints)
        assertEquals(3, s.scoreOf("p2"))
        assertEquals(0, s.scoreOf("p3"))
    }

    @Test
    fun sharedPlaceGetsTheSamePoints() {
        var s = GameEngine.newGame(listOf("A", "B", "C", "D"), totalRounds = 3)
        s = s.play(*pairOfTwos)
            .play(*threeOfAKind)
            .play(2, 2, 3, 1, 4)
            .play(*nothing)

        val ranking = GameEngine.roundRanking(s)
        assertEquals(listOf("p2", "p1", "p3", "p4"), ranking.map { it.playerId })
        assertEquals(listOf(1, 2, 2, 4), ranking.map { it.place })
        assertEquals(mapOf("p2" to 3, "p1" to 2, "p3" to 2, "p4" to 0), s.lastRoundPoints)
    }

    @Test
    fun matchEndsAfterTheChosenNumberOfRounds() {
        var s = GameEngine.newGame(listOf("A", "B"), totalRounds = 3)

        s = s.play(*fiveOfAKind).play(*nothing)
        assertIs<RoundPhase.RoundOver>(s.phase)
        s = s.act(GameAction.NextRound)
        assertEquals(2, s.round)
        assertEquals(listOf("p2", "p1"), s.turnOrder.map { it.id })

        s = s.play(*nothing).play(*fiveOfAKind)
        assertIs<RoundPhase.RoundOver>(s.phase)
        s = s.act(GameAction.NextRound)

        s = s.play(*fiveOfAKind).play(*nothing)
        assertEquals(RoundPhase.MatchOver(winnerId = "p1", roundWinnerIds = listOf("p1")), s.phase)
        assertEquals(3, s.scoreOf("p1"))
        assertEquals(0, s.scoreOf("p2"))
        assertSame(s, s.act(GameAction.NextRound))
    }

    @Test
    fun tieAfterLastRoundIsDecidedByTiebreakBetweenLeadersOnly() {
        var s = GameEngine.newGame(listOf("A", "B", "C"), totalRounds = 1)

        s = s.play(*threeOfAKind).play(*threeOfAKind).play(*nothing)
        assertEquals(RoundPhase.RoundOver(listOf("p1", "p2")), s.phase)
        assertEquals(listOf("p1", "p2"), s.leaderIds)

        s = s.act(GameAction.NextRound)
        assertTrue(s.isTiebreak)
        assertEquals(listOf("p2", "p1"), s.turnOrder.map { it.id })
        assertTrue(!s.isParticipant("p3"))

        s = s.play(*pairOfTwos).play(*pairOfTwos)
        assertIs<RoundPhase.RoundOver>(s.phase)
        s = s.act(GameAction.NextRound)
        assertEquals(3, s.round)

        s = s.play(*nothing).play(*fiveOfAKind)
        assertEquals(RoundPhase.MatchOver(winnerId = "p2", roundWinnerIds = listOf("p2")), s.phase)
        assertEquals(mapOf("p1" to 3, "p2" to 4, "p3" to 0), s.scores)
    }
}
