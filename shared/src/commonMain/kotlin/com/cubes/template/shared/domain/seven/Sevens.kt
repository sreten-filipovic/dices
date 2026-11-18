package com.cubes.template.shared.domain.seven

import com.cubes.template.shared.domain.game.DiceRoller

/** The dice of the player whose turn it is. */
data class SevenTurn(
    /** Six dice in table order; empty until the first roll. Removed dice keep their last value. */
    val dice: List<Int> = emptyList(),
    /** Pairs of die indices taken out because they add up to seven, in the order they left the table. */
    val pairs: List<Set<Int>> = emptyList(),
    /** Rolls made so far in this turn. */
    val rolls: Int = 0,
) {
    val hasRolled: Boolean get() = rolls > 0

    /** Indices of all dice that are out of play. */
    val removed: Set<Int> get() = pairs.flatten().toSet()

    /** Indices of the dice still in play. */
    val active: List<Int> get() = dice.indices.filter { it !in removed }

    /** What the player scores by banking now: the sum of the dice still in play. */
    val score: Int get() = active.sumOf { dice[it] }
}

enum class SevenPhase { Playing, RoundOver, MatchOver }

/**
 * Full state of a Sevens match. Players are identified by their index in [players].
 * Mutated only through [Sevens].
 */
data class SevenState(
    val players: List<String>,
    val totalRounds: Int,
    val scores: List<Int>,
    val round: Int = 1,
    /** Index of the player who opens this round; moves to the next player every round. */
    val starter: Int = 0,
    /** Players who have already banked in this round. */
    val turnsTaken: Int = 0,
    /** Most rolls a turn may have in this round; the opening player lowers it by stopping early. */
    val rollLimit: Int = Sevens.MAX_ROLLS,
    val turn: SevenTurn = SevenTurn(),
    /** Points banked in this round per player; `null` until that player has played. */
    val roundScores: List<Int?>,
    /** The dice each player banked in this round, removed pairs included; `null` until they have played. */
    val roundTurns: List<SevenTurn?> = List(players.size) { null },
    val phase: SevenPhase = SevenPhase.Playing,
) {
    val currentPlayer: Int get() = (starter + turnsTaken) % players.size

    val canRoll: Boolean
        get() = phase == SevenPhase.Playing &&
            turn.rolls < rollLimit &&
            (!turn.hasRolled || turn.active.isNotEmpty())

    val canBank: Boolean get() = phase == SevenPhase.Playing && turn.hasRolled

    /** Players with the highest total; more than one when the match ends level. */
    val leaders: List<Int>
        get() {
            val max = scores.max()
            return scores.indices.filter { scores[it] == max }
        }
}

object Sevens {
    const val DICE_COUNT = 6
    const val MAX_ROLLS = 3
    private const val SEVEN = 7

    fun newGame(players: List<String>, totalRounds: Int): SevenState {
        require(players.size >= 2) { "Sevens needs at least two players" }
        require(totalRounds >= 1) { "A match has at least one round" }
        return SevenState(
            players = players,
            totalRounds = totalRounds,
            scores = List(players.size) { 0 },
            roundScores = List(players.size) { null },
        )
    }

    /** Rolls every die still in play, then takes out the pairs that add up to seven. */
    fun roll(state: SevenState, roller: DiceRoller): SevenState {
        if (!state.canRoll) return state
        val turn = state.turn
        val dice = List(DICE_COUNT) { index ->
            if (turn.hasRolled && index in turn.removed) turn.dice[index] else roller.roll()
        }
        val pairs = turn.pairs + sevenPairs(dice, turn.removed)
        return state.copy(turn = SevenTurn(dice = dice, pairs = pairs, rolls = turn.rolls + 1))
    }

    /**
     * Ends the current turn and scores the dice still in play. The opening player's number of rolls
     * becomes the limit for everyone else in the round.
     */
    fun bank(state: SevenState): SevenState {
        if (!state.canBank) return state
        val player = state.currentPlayer
        val points = state.turn.score
        val turnsTaken = state.turnsTaken + 1
        val roundFinished = turnsTaken == state.players.size
        return state.copy(
            scores = state.scores.mapIndexed { index, score -> if (index == player) score + points else score },
            roundScores = state.roundScores.mapIndexed { index, score -> if (index == player) points else score },
            roundTurns = state.roundTurns.mapIndexed { index, turn -> if (index == player) state.turn else turn },
            rollLimit = if (state.turnsTaken == 0) state.turn.rolls else state.rollLimit,
            turnsTaken = if (roundFinished) state.turnsTaken else turnsTaken,
            // The last player's dice stay on the table until the next round starts
            turn = if (roundFinished) state.turn else SevenTurn(),
            phase = when {
                !roundFinished -> SevenPhase.Playing
                state.round >= state.totalRounds -> SevenPhase.MatchOver
                else -> SevenPhase.RoundOver
            },
        )
    }

    /** Starts the next round with the next player opening it, again with up to [MAX_ROLLS] rolls. */
    fun nextRound(state: SevenState): SevenState {
        if (state.phase != SevenPhase.RoundOver) return state
        return state.copy(
            round = state.round + 1,
            starter = (state.starter + 1) % state.players.size,
            turnsTaken = 0,
            rollLimit = MAX_ROLLS,
            turn = SevenTurn(),
            roundScores = List(state.players.size) { null },
            roundTurns = List(state.players.size) { null },
            phase = SevenPhase.Playing,
        )
    }

    /**
     * The dice in play that pair up to seven (1+6, 2+5, 3+4), one set of two indices per pair. Every
     * die is used in at most one pair, so of three sixes and one one only a single six leaves the table.
     */
    internal fun sevenPairs(dice: List<Int>, alreadyRemoved: Set<Int> = emptySet()): List<Set<Int>> {
        val inPlay = dice.indices.filter { it !in alreadyRemoved }
        return (1..SEVEN / 2).flatMap { low ->
            val lows = inPlay.filter { dice[it] == low }
            val highs = inPlay.filter { dice[it] == SEVEN - low }
            lows.zip(highs).map { (a, b) -> setOf(a, b) }
        }
    }
}
