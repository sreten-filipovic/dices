package com.cubes.template.shared.domain.risk

import com.cubes.template.shared.domain.game.DiceRoller

data class RiskTurn(
    val rolls: List<Int> = emptyList(),
) {
    val hasRolled: Boolean get() = rolls.isNotEmpty()

    val score: Int get() = rolls.takeLastWhile { it != RiskIt.RESET_VALUE }.sum()

    val rollsLeft: Int get() = RiskIt.MAX_ROLLS - rolls.size

    val canRoll: Boolean get() = rollsLeft > 0

    val lostEverything: Boolean get() = rolls.lastOrNull() == RiskIt.RESET_VALUE
}

enum class RiskPhase { Playing, RoundOver, MatchOver }

data class RiskState(
    val players: List<String>,
    val totalRounds: Int,
    val scores: List<Int>,
    val round: Int = 1,
    val starter: Int = 0,
    val turnsTaken: Int = 0,
    val turn: RiskTurn = RiskTurn(),
    val roundTurns: List<RiskTurn?> = List(players.size) { null },
    val phase: RiskPhase = RiskPhase.Playing,
    val left: Set<Int> = emptySet(),
) {
    val currentPlayer: Int get() = (starter + turnsTaken) % players.size

    val canRoll: Boolean get() = phase == RiskPhase.Playing && turn.canRoll

    val canStop: Boolean get() = phase == RiskPhase.Playing && turn.hasRolled

    val leaders: List<Int>
        get() {
            val active = scores.indices.filter { it !in left }
            val max = active.maxOfOrNull { scores[it] } ?: 0
            return active.filter { scores[it] == max }
        }
}

object RiskIt {
    const val MAX_ROLLS = 10
    const val RESET_VALUE = 1

    fun newGame(players: List<String>, totalRounds: Int): RiskState {
        require(players.size >= 2) { "Risk It needs at least two players" }
        require(totalRounds >= 1) { "A match has at least one round" }
        return RiskState(
            players = players,
            totalRounds = totalRounds,
            scores = List(players.size) { 0 },
        )
    }

    fun roll(state: RiskState, roller: DiceRoller): RiskState {
        if (!state.canRoll) return state
        return state.copy(turn = RiskTurn(rolls = state.turn.rolls + roller.roll()))
    }

    fun stop(state: RiskState): RiskState {
        if (!state.canStop) return state
        return endTurn(state, state.turn)
    }

    fun skip(state: RiskState): RiskState {
        if (state.phase != RiskPhase.Playing) return state
        return endTurn(state, RiskTurn())
    }

    private fun endTurn(state: RiskState, finished: RiskTurn): RiskState {
        val player = state.currentPlayer
        val roundFinished = state.turnsTaken + 1 == state.players.size
        return state.copy(
            scores = state.scores.mapIndexed { index, score -> if (index == player) score + finished.score else score },
            roundTurns = state.roundTurns.mapIndexed { index, turn -> if (index == player) finished else turn },
            turnsTaken = if (roundFinished) state.turnsTaken else state.turnsTaken + 1,
            turn = if (roundFinished) finished else RiskTurn(),
            phase = when {
                !roundFinished -> RiskPhase.Playing
                state.round >= state.totalRounds -> RiskPhase.MatchOver
                else -> RiskPhase.RoundOver
            },
        )
    }

    fun nextRound(state: RiskState): RiskState {
        if (state.phase != RiskPhase.RoundOver) return state
        return state.copy(
            round = state.round + 1,
            starter = (state.starter + 1) % state.players.size,
            turnsTaken = 0,
            turn = RiskTurn(),
            roundTurns = List(state.players.size) { null },
            phase = RiskPhase.Playing,
        )
    }
}
