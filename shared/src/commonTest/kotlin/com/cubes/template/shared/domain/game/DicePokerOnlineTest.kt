package com.cubes.template.shared.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DicePokerOnlineTest {

    private var seq = 0

    private fun event(player: Int, action: PokerAction) = PokerEvent(++seq, player, action)

    private fun PokerMatch.then(player: Int, action: PokerAction) = DicePokerOnline.apply(this, event(player, action)).match

    private fun start(players: Int = 2, rounds: Int = 3) =
        DicePokerOnline.start(List(players) { "P${it + 1}" }, rounds)

    @Test
    fun serverDiceDriveTheTurn() {
        val match = start()
            .then(0, PokerAction.Roll(listOf(6, 6, 6, 2, 3)))
            .then(0, PokerAction.Reroll(setOf(3, 4), listOf(6, 5)))

        assertEquals(listOf(6, 6, 6, 6, 5), match.state.turnOf("p1").dice)
        assertEquals("p2", match.state.currentPlayer.id)
        assertEquals(RoundPhase.FirstRoll, match.state.phase)
        assertEquals(2, match.lastSeq)
    }

    @Test
    fun actionsOutOfTurnAreIgnored() {
        val match = start()
            .then(1, PokerAction.Roll(listOf(1, 2, 3, 4, 5)))
            .then(0, PokerAction.Keep)

        assertEquals(start().state, match.state)
        assertEquals(2, match.lastSeq)
    }

    @Test
    fun theNextRoundStartsOnItsOwnAfterTheLastTurn() {
        val result = DicePokerOnline.apply(
            start()
                .then(0, PokerAction.Roll(listOf(6, 6, 6, 6, 6)))
                .then(0, PokerAction.Keep)
                .then(1, PokerAction.Roll(listOf(1, 2, 4, 5, 6))),
            event(1, PokerAction.Keep),
        )

        assertIs<RoundPhase.RoundOver>(result.steps[result.steps.size - 2].state.phase)
        assertEquals(2, result.match.state.round)
        assertEquals(RoundPhase.FirstRoll, result.match.state.phase)
        assertEquals(1, result.match.state.scoreOf("p1"))
    }

    @Test
    fun aTimeoutPlaysTheTurnAndThreeMissedTurnsRemoveThePlayer() {
        val rolls = listOf(2, 3, 4, 5, 6, 1, 1, 1, 1, 1, 1, 1)
        val sixes = PokerAction.Roll(listOf(6, 6, 6, 6, 6))
        var match = start(players = 2, rounds = 5).then(1, PokerAction.Timeout(rolls))

        assertEquals(listOf(2, 3, 4, 5, 6), match.state.turnOf("p1").dice)
        assertEquals("p2", match.state.currentPlayer.id)
        assertEquals(listOf(1, 0), match.missedTurns)

        match = match.then(1, sixes).then(1, PokerAction.Keep)
        assertEquals(2, match.state.round)
        assertEquals("p2", match.state.currentPlayer.id)

        match = match.then(1, sixes).then(1, PokerAction.Keep).then(1, PokerAction.Timeout(rolls))
        assertEquals(listOf(2, 0), match.missedTurns)
        assertEquals(3, match.state.round)
        assertEquals("p1", match.state.currentPlayer.id)

        match = match.then(1, PokerAction.Timeout(rolls))
        assertTrue("p1" in match.state.leftIds)
        val phase = assertIs<RoundPhase.MatchOver>(match.state.phase)
        assertEquals("p2", phase.winnerId)
    }

    @Test
    fun whenAllButOneLeaveTheLastPlayerWins() {
        val match = start(players = 3)
            .then(1, PokerAction.Leave)
            .then(2, PokerAction.Leave)

        val phase = assertIs<RoundPhase.MatchOver>(match.state.phase)
        assertEquals("p1", phase.winnerId)
    }

    @Test
    fun leavingOnYourTurnPassesTheTurnOn() {
        val match = start(players = 3)
            .then(0, PokerAction.Roll(listOf(6, 6, 6, 6, 6)))
            .then(0, PokerAction.Leave)

        assertEquals("p2", match.state.currentPlayer.id)
        assertEquals(RoundPhase.FirstRoll, match.state.phase)
        assertEquals(listOf("p2", "p3"), match.state.turnOrder.map { it.id })
    }

    @Test
    fun aPlayerWhoLeftIsLeftOutOfTheNextRounds() {
        val match = start(players = 3)
            .then(2, PokerAction.Leave)
            .then(0, PokerAction.Roll(listOf(6, 6, 6, 6, 6)))
            .then(0, PokerAction.Keep)
            .then(1, PokerAction.Roll(listOf(1, 2, 4, 5, 6)))
            .then(1, PokerAction.Keep)

        assertEquals(2, match.state.round)
        assertEquals(listOf("p2", "p1"), match.state.turnOrder.map { it.id })
    }
}
