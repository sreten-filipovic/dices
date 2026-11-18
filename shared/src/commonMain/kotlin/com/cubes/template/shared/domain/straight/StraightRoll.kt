package com.cubes.template.shared.domain.straight

import com.cubes.template.shared.domain.game.DiceRoller

data class StraightTurn(
    val dice: List<Int> = emptyList(),
    val kept: Set<Int> = emptySet(),
    val points: Int = StraightRoll.START_POINTS,
    val rolls: Int = 0,
) {
    val hasRolled: Boolean get() = rolls > 0

    val isStraight: Boolean get() = kept.size == StraightRoll.DICE_COUNT

    val isOver: Boolean get() = hasRolled && (isStraight || points == 0)

    val remaining: Set<Int> get() = dice.indices.toSet() - kept

    val keptValues: Set<Int> get() = kept.map { dice[it] }.toSet()

    val canRoll: Boolean get() = !isOver
}

enum class StraightPhase { Playing, RoundOver, MatchOver }

data class StraightState(
    val players: List<String>,
    val totalRounds: Int,
    val scores: List<Int>,
    val round: Int = 1,
    val starter: Int = 0,
    val turnsTaken: Int = 0,
    val turn: StraightTurn = StraightTurn(),
    val roundScores: List<Int?> = List(players.size) { null },
    val roundTurns: List<StraightTurn?> = List(players.size) { null },
    val phase: StraightPhase = StraightPhase.Playing,
) {
    val currentPlayer: Int get() = (starter + turnsTaken) % players.size

    val canRoll: Boolean get() = phase == StraightPhase.Playing && turn.canRoll

    val canFinish: Boolean get() = phase == StraightPhase.Playing && turn.isOver

    val leaders: List<Int>
        get() {
            val max = scores.max()
            return scores.indices.filter { scores[it] == max }
        }
}

object StraightRoll {
    const val DICE_COUNT = 6
    const val START_POINTS = 10
    const val REROLL_COST = 1

    fun newGame(players: List<String>, totalRounds: Int): StraightState {
        require(players.size >= 2) { "Straight Roll needs at least two players" }
        require(totalRounds >= 1) { "A match has at least one round" }
        return StraightState(
            players = players,
            totalRounds = totalRounds,
            scores = List(players.size) { 0 },
        )
    }

    fun roll(state: StraightState, roller: DiceRoller): StraightState {
        if (!state.canRoll) return state
        val turn = state.turn
        val rerolled = turn.remaining
        val dice = List(DICE_COUNT) { index ->
            if (turn.hasRolled && index !in rerolled) turn.dice[index] else roller.roll()
        }
        return state.copy(
            turn = StraightTurn(
                dice = dice,
                kept = keep(dice, if (turn.hasRolled) turn.kept else emptySet()),
                points = if (turn.hasRolled) turn.points - REROLL_COST else turn.points,
                rolls = turn.rolls + 1,
            ),
        )
    }

    fun finish(state: StraightState): StraightState {
        if (!state.canFinish) return state
        val player = state.currentPlayer
        val points = state.turn.points
        val roundFinished = state.turnsTaken + 1 == state.players.size
        return state.copy(
            scores = state.scores.mapIndexed { index, score -> if (index == player) score + points else score },
            roundScores = state.roundScores.mapIndexed { index, score -> if (index == player) points else score },
            roundTurns = state.roundTurns.mapIndexed { index, turn -> if (index == player) state.turn else turn },
            turnsTaken = if (roundFinished) state.turnsTaken else state.turnsTaken + 1,
            turn = if (roundFinished) state.turn else StraightTurn(),
            phase = when {
                !roundFinished -> StraightPhase.Playing
                state.round >= state.totalRounds -> StraightPhase.MatchOver
                else -> StraightPhase.RoundOver
            },
        )
    }

    internal fun keep(dice: List<Int>, kept: Set<Int>): Set<Int> {
        val values = kept.map { dice[it] }.toMutableSet()
        return kept + dice.indices.filter { it !in kept && values.add(dice[it]) }
    }

    fun nextRound(state: StraightState): StraightState {
        if (state.phase != StraightPhase.RoundOver) return state
        return state.copy(
            round = state.round + 1,
            starter = (state.starter + 1) % state.players.size,
            turnsTaken = 0,
            turn = StraightTurn(),
            roundScores = List(state.players.size) { null },
            roundTurns = List(state.players.size) { null },
            phase = StraightPhase.Playing,
        )
    }
}
