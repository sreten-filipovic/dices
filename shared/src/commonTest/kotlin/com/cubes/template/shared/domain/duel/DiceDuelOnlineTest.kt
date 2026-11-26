package com.cubes.template.shared.domain.duel

import com.cubes.template.shared.domain.online.TurnAction
import com.cubes.template.shared.domain.online.TurnEvent
import com.cubes.template.shared.domain.online.TurnMatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiceDuelOnlineTest {

    private var seq = 0

    private fun TurnMatch<DuelState>.then(player: Int, action: TurnAction) =
        DiceDuelOnline.apply(this, TurnEvent(++seq, player, action)).match

    private fun start(players: Int = 2, dice: Int = 2) =
        DiceDuelOnline.start(DiceDuel.newGame(List(players) { "P${it + 1}" }, dice))

    @Test
    fun theHigherSumWinsTheDuel() {
        val match = start()
            .then(0, TurnAction.Roll(listOf(6, 5)))
            .then(1, TurnAction.Roll(listOf(3, 3)))

        assertTrue(DiceDuelRules.isMatchOver(match.state))
        assertEquals(0, match.state.winner)
        assertEquals(listOf(1, 0), match.state.wins)
    }

    @Test
    fun aRollWithTooFewDiceOrOutOfTurnIsIgnored() {
        val match = start()
            .then(1, TurnAction.Roll(listOf(6, 6)))
            .then(0, TurnAction.Roll(listOf(6)))

        assertEquals(start().state, match.state)
    }

    @Test
    fun aTieStartsATiebreakOnItsOwn() {
        val match = start(players = 3)
            .then(0, TurnAction.Roll(listOf(6, 4)))
            .then(1, TurnAction.Roll(listOf(2, 2)))
            .then(2, TurnAction.Roll(listOf(5, 5)))

        assertEquals(listOf(0, 2), match.state.participants)
        assertEquals(0, match.state.currentPlayer)
        assertNull(match.state.winner)

        val decided = match
            .then(0, TurnAction.Roll(listOf(1, 1)))
            .then(2, TurnAction.Roll(listOf(3, 1)))
        assertEquals(2, decided.state.winner)
    }

    @Test
    fun aTimeoutScoresZero() {
        val match = start()
            .then(0, TurnAction.Timeout)
            .then(1, TurnAction.Roll(listOf(1, 1)))

        assertEquals(1, match.state.winner)
        assertEquals(listOf(1, 0), match.missedTurns)
    }

    @Test
    fun whenTheOpponentLeavesTheLastPlayerWins() {
        val match = start(players = 3)
            .then(0, TurnAction.Roll(listOf(2, 2)))
            .then(1, TurnAction.Leave)
            .then(2, TurnAction.Leave)

        assertTrue(match.state.isOver)
        assertEquals(0, match.state.winner)
    }

    @Test
    fun aPlayerWhoLeftCannotWinOnTheirEarlierRoll() {
        val match = start(players = 3)
            .then(0, TurnAction.Roll(listOf(6, 6)))
            .then(0, TurnAction.Leave)
            .then(1, TurnAction.Roll(listOf(2, 1)))
            .then(2, TurnAction.Roll(listOf(1, 1)))

        assertEquals(1, match.state.winner)
    }

    @Test
    fun aRematchStartsANewDuelAndKeepsTheWins() {
        val over = start()
            .then(0, TurnAction.Roll(listOf(6, 5)))
            .then(1, TurnAction.Roll(listOf(3, 3)))
        assertTrue(!DiceDuelRules.isFinished(over.state))

        val rematch = over.then(1, TurnAction.Rematch)
        assertTrue(!rematch.state.isOver)
        assertEquals(1, rematch.state.currentPlayer)
        assertEquals(listOf(1, 0), rematch.state.wins)
        assertEquals(listOf(null, null), rematch.state.hands)

        val again = rematch.then(0, TurnAction.Rematch)
        assertEquals(rematch.state, again.state)
    }

    @Test
    fun leavingBetweenDuelsKeepsTheResultAndTheOthersPlayOn() {
        val over = start(players = 3)
            .then(0, TurnAction.Roll(listOf(6, 6)))
            .then(1, TurnAction.Roll(listOf(2, 2)))
            .then(2, TurnAction.Roll(listOf(2, 2)))
            .then(0, TurnAction.Leave)

        assertEquals(0, over.state.winner)
        assertTrue(over.state.isOver)
        assertTrue(!DiceDuelRules.isFinished(over.state))

        val next = over.then(0, TurnAction.Rematch).then(1, TurnAction.Rematch)
        assertEquals(1, next.state.currentPlayer)
        assertEquals(listOf(1, 0, 0), next.state.wins)

        val decided = next
            .then(1, TurnAction.Roll(listOf(1, 1)))
            .then(2, TurnAction.Roll(listOf(4, 1)))
        assertEquals(2, decided.state.winner)
    }

    @Test
    fun whenOnlyOnePlayerStaysTheRoomIsFinishedWithoutAnExtraWin() {
        val over = start()
            .then(0, TurnAction.Roll(listOf(6, 6)))
            .then(1, TurnAction.Roll(listOf(1, 1)))
            .then(1, TurnAction.Leave)

        assertTrue(DiceDuelRules.isFinished(over.state))
        assertEquals(0, over.state.winner)
        assertEquals(listOf(1, 0), over.state.wins)
        assertEquals(over.state, over.then(0, TurnAction.Rematch).state)
    }
}
