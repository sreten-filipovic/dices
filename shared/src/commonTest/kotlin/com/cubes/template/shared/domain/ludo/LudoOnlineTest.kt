package com.cubes.template.shared.domain.ludo

import com.cubes.template.shared.domain.ludo.Ludo.HOME
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LudoOnlineTest {

    private var seq = 0

    private fun event(player: Int, action: LudoAction) = LudoEvent(++seq, player, action)

    private fun LudoMatch.then(player: Int, action: LudoAction) = LudoOnline.apply(this, event(player, action)).match

    private fun start(players: Int = 2) = LudoOnline.start(List(players) { "P${it + 1}" })

    @Test
    fun serverRollsDriveTheGame() {
        val match = start()
            .then(0, LudoAction.Roll(6))
            .then(0, LudoAction.Move(0))

        assertEquals(listOf(0, HOME, HOME, HOME), match.state.pawns[0])
        assertEquals(0, match.state.currentPlayer)
        assertEquals(2, match.lastSeq)
    }

    @Test
    fun actionsOutOfTurnAreIgnoredButStillAdvanceTheSequence() {
        val match = start()
            .then(1, LudoAction.Roll(6))
            .then(0, LudoAction.Move(0))

        assertEquals(start().state, match.state)
        assertEquals(2, match.lastSeq)
    }

    @Test
    fun replayMatchesStepByStepApplication() {
        val events = listOf(
            LudoEvent(1, 0, LudoAction.Roll(6)),
            LudoEvent(2, 0, LudoAction.Move(0)),
            LudoEvent(3, 0, LudoAction.Roll(4)),
            LudoEvent(4, 0, LudoAction.Move(0)),
            LudoEvent(5, 1, LudoAction.Roll(2)),
        )
        val replayed = LudoOnline.replay(start(), events)
        val stepped = events.fold(start()) { match, e -> LudoOnline.apply(match, e).match }

        assertEquals(stepped, replayed)
        assertEquals(listOf(4, HOME, HOME, HOME), replayed.state.pawns[0])
    }

    @Test
    fun timeoutPlaysTheWholeTurnForTheAbsentPlayer() {
        val result = LudoOnline.apply(start(), event(0, LudoAction.Timeout(listOf(6, 3, 2, 2, 2))))

        assertEquals(listOf(3, HOME, HOME, HOME), result.match.state.pawns[0])
        assertEquals(1, result.match.state.currentPlayer)
        assertEquals(listOf(1, 0), result.match.missedTurns)
        assertTrue(result.steps.size >= 4)
    }

    @Test
    fun playingAgainResetsMissedTurns() {
        val match = start()
            .then(1, LudoAction.Timeout(listOf(1, 1, 1)))
            .then(1, LudoAction.Roll(2))

        assertEquals(listOf(1, 0), match.missedTurns)
    }

    @Test
    fun threeMissedTurnsForfeitTheGame() {
        var match = start(3)
        repeat(3) {
            match = match
                .then(1, LudoAction.Timeout(listOf(1, 1, 1)))
                .then(1, LudoAction.Roll(1)).then(1, LudoAction.Roll(1)).then(1, LudoAction.Roll(1))
                .then(2, LudoAction.Roll(1)).then(2, LudoAction.Roll(1)).then(2, LudoAction.Roll(1))
        }

        assertEquals(listOf(0), match.state.forfeited)
        assertTrue(match.state.players.indices.filter(match.state::isActive) == listOf(1, 2))
    }

    @Test
    fun leavingHandsTheTurnOnAndEndsTwoPlayerGame() {
        val three = start(3).then(0, LudoAction.Leave)
        assertEquals(1, three.state.currentPlayer)
        assertEquals(listOf(0), three.state.forfeited)

        val two = start(2).then(1, LudoAction.Leave)
        val over = assertIs<LudoPhase.GameOver>(two.state.phase)
        assertEquals(listOf(0, 1), over.ranking)
    }

    @Test
    fun forfeitedPlayersAreRankedLastInReverseOrder() {
        val match = start(4)
            .then(1, LudoAction.Leave)
            .then(2, LudoAction.Leave)
            .then(3, LudoAction.Leave)

        val over = assertIs<LudoPhase.GameOver>(match.state.phase)
        assertEquals(listOf(0, 3, 2, 1), over.ranking)
    }
}
