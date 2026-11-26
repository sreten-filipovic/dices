package com.cubes.template.shared.domain.ludo

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.ludo.Ludo.HOME
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LudoTest {

    private fun roller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirst() }
    }

    private fun LudoState.roll(value: Int) = Ludo.roll(this, roller(value))
    private fun LudoState.move(pawn: Int) = Ludo.move(this, pawn)

    private fun newGame(players: Int = 2) = Ludo.newGame(List(players) { "P${it + 1}" })

    private fun LudoState.withPawns(vararg pawns: List<Int>, current: Int = 0, rollsLeft: Int = 1) =
        copy(pawns = pawns.toList(), currentPlayer = current, rollsLeft = rollsLeft)

    @Test
    fun twoPlayersSitOppositeEachOther() {
        assertEquals(listOf(LudoSeat.TOP_LEFT, LudoSeat.BOTTOM_RIGHT), newGame(2).players.map { it.seat })
        assertEquals(LudoSeat.entries, newGame(4).players.map { it.seat })
    }

    @Test
    fun withAllPawnsHomeAPlayerGetsThreeTriesForASix() {
        var state = newGame()
        assertEquals(3, state.rollsLeft)

        state = state.roll(2)
        assertEquals(0, state.currentPlayer)
        assertEquals(2, state.rollsLeft)

        state = state.roll(5).roll(1)
        assertEquals(1, state.currentPlayer)
        assertEquals(3, state.rollsLeft)
    }

    @Test
    fun aSixBringsAPawnOutAndGivesAnotherRoll() {
        var state = newGame().roll(6)
        val phase = assertIs<LudoPhase.ChoosingPawn>(state.phase)
        assertEquals(setOf(0, 1, 2, 3), phase.movable)

        state = state.move(2)
        assertEquals(listOf(HOME, HOME, 0, HOME), state.pawns[0])
        assertEquals(0, state.currentPlayer)
        assertEquals(LudoPhase.WaitingForRoll, state.phase)
        assertEquals(1, state.rollsLeft)
    }

    @Test
    fun aRollThatIsNotASixPassesTheTurnAfterMoving() {
        val state = newGame()
            .withPawns(listOf(0, HOME, HOME, HOME), listOf(HOME, HOME, HOME, HOME))
            .roll(4)
            .move(0)

        assertEquals(4, state.pawns[0][0])
        assertEquals(1, state.currentPlayer)
        assertEquals(3, state.rollsLeft)
    }

    @Test
    fun landingOnAnOpponentSendsItHome() {
        val blueCell = Ludo.cellOf(LudoSeat.TOP_LEFT, 23)
        val redProgress = 3
        assertEquals(blueCell, Ludo.cellOf(LudoSeat.BOTTOM_RIGHT, redProgress))

        val state = newGame()
            .withPawns(listOf(20, HOME, HOME, HOME), listOf(redProgress, HOME, HOME, HOME))
            .roll(3)
            .move(0)

        assertEquals(23, state.pawns[0][0])
        assertEquals(HOME, state.pawns[1][0])
        assertEquals(listOf(1 to 0), state.lastMove?.captured)
    }

    @Test
    fun aPawnCannotLandOnAnotherPawnOfTheSameColor() {
        val state = newGame().withPawns(listOf(0, 4, HOME, HOME), listOf(HOME, HOME, HOME, HOME))

        assertEquals(setOf(1), Ludo.movablePawns(state, 4))
        assertEquals(setOf(0, 1), Ludo.movablePawns(state, 6))
    }

    @Test
    fun theFinishNeedsAnExactRollAndPawnsCannotJumpEachOtherThere() {
        val state = newGame().withPawns(listOf(38, 42, HOME, HOME), listOf(HOME, HOME, HOME, HOME))

        assertEquals(setOf(0), Ludo.movablePawns(state, 3))
        assertEquals(emptySet(), Ludo.movablePawns(state, 4))
        assertEquals(emptySet(), Ludo.movablePawns(state, 5))
        assertEquals(setOf(0, 1), Ludo.movablePawns(state, 1))
    }

    @Test
    fun withNoMovePossibleTheTurnPasses() {
        val state = newGame()
            .withPawns(listOf(38, HOME, HOME, HOME), listOf(HOME, HOME, HOME, HOME))
            .roll(6)

        assertEquals(0, state.currentPlayer)

        val passed = newGame()
            .withPawns(listOf(41, HOME, HOME, HOME), listOf(HOME, HOME, HOME, HOME))
            .roll(5)
        assertEquals(1, passed.currentPlayer)
    }

    @Test
    fun pawnsPackedInTheFinishStillGetThreeTries() {
        val state = newGame()
            .withPawns(listOf(HOME, HOME, HOME, HOME), listOf(43, 42, HOME, HOME), current = 1, rollsLeft = 3)
            .roll(2)

        assertEquals(1, state.currentPlayer)
        assertEquals(2, state.rollsLeft)
    }

    @Test
    fun withTwoPlayersTheFirstToFinishEndsTheGame() {
        val state = newGame()
            .withPawns(listOf(43, 42, 41, 37), listOf(HOME, HOME, HOME, HOME))
            .roll(3)
            .move(3)

        assertEquals(LudoPhase.GameOver(listOf(0, 1)), state.phase)
        assertTrue(state.pawns[0].all { it >= Ludo.TRACK_LENGTH })
    }

    @Test
    fun theGameGoesOnUntilOnlyOnePlayerIsLeft() {
        val home = listOf(HOME, HOME, HOME, HOME)
        var state = newGame(players = 3)
            .withPawns(listOf(43, 42, 41, 37), home, listOf(43, 42, 41, 38))
            .roll(3)
            .move(3)

        assertEquals(listOf(0), state.ranking)
        assertEquals(LudoPhase.WaitingForRoll, state.phase)
        assertEquals(1, state.currentPlayer)

        state = state.roll(1).roll(1).roll(1)
        assertEquals(2, state.currentPlayer)

        state = state.roll(2).move(3)
        assertEquals(LudoPhase.GameOver(listOf(0, 2, 1)), state.phase)
    }

    @Test
    fun finishedPlayersAreSkipped() {
        val home = listOf(HOME, HOME, HOME, HOME)
        val state = newGame(players = 3)
            .withPawns(listOf(43, 42, 41, 40), home, home, current = 2, rollsLeft = 1)
            .copy(ranking = listOf(0))
            .roll(2)

        assertEquals(1, state.currentPlayer)
    }

    @Test
    fun threeSixesInARowEndTheTurnWithoutMoving() {
        var state = newGame().withPawns(listOf(5, HOME, HOME, HOME), listOf(HOME, HOME, HOME, HOME))

        state = state.roll(6).move(0)
        assertEquals(1, state.sixesInRow)
        state = state.roll(6).move(0)
        assertEquals(2, state.sixesInRow)
        assertEquals(0, state.currentPlayer)

        state = state.roll(6)
        assertEquals(1, state.currentPlayer)
        assertEquals(LudoPhase.WaitingForRoll, state.phase)
        assertEquals(listOf(17, HOME, HOME, HOME), state.pawns[0])
        assertEquals(0, state.sixesInRow)
    }

    @Test
    fun aRollThatIsNotASixResetsTheSixCount() {
        var state = newGame().withPawns(listOf(5, HOME, HOME, HOME), listOf(HOME, HOME, HOME, HOME))

        state = state.roll(6).move(0).roll(6).move(0)
        assertEquals(2, state.sixesInRow)

        state = state.roll(2).move(0)
        assertEquals(1, state.currentPlayer)
        assertEquals(0, state.sixesInRow)
    }

    @Test
    fun threeSixesWithAllPawnsHomeAlsoEndTheTurn() {
        var state = newGame().roll(6).move(0).roll(6).move(0)
        assertEquals(0, state.currentPlayer)

        state = state.roll(6)
        assertEquals(1, state.currentPlayer)
        assertEquals(listOf(6, HOME, HOME, HOME), state.pawns[0])
    }
}
