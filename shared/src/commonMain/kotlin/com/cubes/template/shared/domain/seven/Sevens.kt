package com.cubes.template.shared.domain.seven

import com.cubes.template.shared.domain.game.DiceRoller

data class SevenTurn(
    val dice: List<Int> = emptyList(),
    val pairs: List<Set<Int>> = emptyList(),
    val rolls: Int = 0,
) {
    val hasRolled: Boolean get() = rolls > 0

    val removed: Set<Int> get() = pairs.flatten().toSet()

    val active: List<Int> get() = dice.indices.filter { it !in removed }

    val score: Int get() = active.sumOf { dice[it] }
}

enum class SevenPhase { Playing, RoundOver, MatchOver }

data class SevenState(
    val players: List<String>,
    val totalRounds: Int,
    val scores: List<Int>,
    val round: Int = 1,
    val starter: Int = 0,
    val turnsTaken: Int = 0,
    val rollLimit: Int = Sevens.MAX_ROLLS,
    val turn: SevenTurn = SevenTurn(),
    val roundScores: List<Int?>,
    val roundTurns: List<SevenTurn?> = List(players.size) { null },
    val phase: SevenPhase = SevenPhase.Playing,
) {
    val currentPlayer: Int get() = (starter + turnsTaken) % players.size

    val canRoll: Boolean
        get() = phase == SevenPhase.Playing &&
            turn.rolls < rollLimit &&
            (!turn.hasRolled || turn.active.isNotEmpty())

    val canBank: Boolean get() = phase == SevenPhase.Playing && turn.hasRolled

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

    fun roll(state: SevenState, roller: DiceRoller): SevenState {
        if (!state.canRoll) return state
        val turn = state.turn
        val dice = List(DICE_COUNT) { index ->
            if (turn.hasRolled && index in turn.removed) turn.dice[index] else roller.roll()
        }
        val pairs = turn.pairs + sevenPairs(dice, turn.removed)
        return state.copy(turn = SevenTurn(dice = dice, pairs = pairs, rolls = turn.rolls + 1))
    }

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
            turn = if (roundFinished) state.turn else SevenTurn(),
            phase = when {
                !roundFinished -> SevenPhase.Playing
                state.round >= state.totalRounds -> SevenPhase.MatchOver
                else -> SevenPhase.RoundOver
            },
        )
    }

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

    internal fun sevenPairs(dice: List<Int>, alreadyRemoved: Set<Int> = emptySet()): List<Set<Int>> {
        val inPlay = dice.indices.filter { it !in alreadyRemoved }
        return (1..SEVEN / 2).flatMap { low ->
            val lows = inPlay.filter { dice[it] == low }
            val highs = inPlay.filter { dice[it] == SEVEN - low }
            lows.zip(highs).map { (a, b) -> setOf(a, b) }
        }
    }
}
