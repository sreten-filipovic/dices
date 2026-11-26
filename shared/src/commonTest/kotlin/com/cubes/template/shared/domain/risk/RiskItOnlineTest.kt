package com.cubes.template.shared.domain.risk

import com.cubes.template.shared.domain.online.TurnAction
import com.cubes.template.shared.domain.online.TurnEvent
import com.cubes.template.shared.domain.online.TurnMatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RiskItOnlineTest {

    private var seq = 0

    private fun TurnMatch<RiskState>.then(player: Int, action: TurnAction) =
        RiskItOnline.apply(this, TurnEvent(++seq, player, action)).match

    private fun start(players: Int = 2, rounds: Int = 3) =
        RiskItOnline.start(RiskIt.newGame(List(players) { "P${it + 1}" }, rounds))

    @Test
    fun serverRollsAndStopDriveTheTurn() {
        val match = start()
            .then(0, TurnAction.Roll(listOf(5)))
            .then(0, TurnAction.Roll(listOf(4)))
            .then(0, TurnAction.EndTurn)

        assertEquals(listOf(9, 0), match.state.scores)
        assertEquals(1, match.state.currentPlayer)
        assertEquals(3, match.lastSeq)
    }

    @Test
    fun actionsOutOfTurnAreIgnored() {
        val match = start()
            .then(1, TurnAction.Roll(listOf(6)))
            .then(0, TurnAction.EndTurn)

        assertEquals(start().state, match.state)
    }

    @Test
    fun theNextRoundStartsOnItsOwn() {
        val match = start()
            .then(0, TurnAction.Roll(listOf(6)))
            .then(0, TurnAction.EndTurn)
            .then(1, TurnAction.Roll(listOf(3)))
            .then(1, TurnAction.EndTurn)

        assertEquals(2, match.state.round)
        assertEquals(RiskPhase.Playing, match.state.phase)
        assertEquals(1, match.state.currentPlayer)
    }

    @Test
    fun aTimeoutKeepsWhatWasRolledOrScoresNothing() {
        val match = start(players = 3)
            .then(0, TurnAction.Roll(listOf(6)))
            .then(2, TurnAction.Timeout)
            .then(2, TurnAction.Timeout)

        assertEquals(listOf(6, 0, 0), match.state.scores)
        assertEquals(listOf(1, 1, 0), match.missedTurns)
        assertEquals(2, match.state.currentPlayer)
    }

    @Test
    fun aPlayerWhoLeftIsSkippedAndTheLastOneWins() {
        var match = start(players = 3)
            .then(1, TurnAction.Leave)
            .then(0, TurnAction.Roll(listOf(2)))
            .then(0, TurnAction.EndTurn)

        assertEquals(2, match.state.currentPlayer)

        match = match.then(2, TurnAction.Leave)
        assertEquals(RiskPhase.MatchOver, match.state.phase)
        assertEquals(listOf(0), match.state.leaders)
        assertTrue(1 in match.left && 2 in match.left)
    }
}
