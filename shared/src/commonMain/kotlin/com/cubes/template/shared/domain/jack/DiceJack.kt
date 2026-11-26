package com.cubes.template.shared.domain.jack

import com.cubes.template.shared.domain.game.DiceRoller

data class JackTurn(
    val dice: List<Int> = emptyList(),
) {
    val hasRolled: Boolean get() = dice.isNotEmpty()

    val isTriple: Boolean get() = dice.size == DiceJack.FIRST_ROLL_DICE && dice.distinct().size == 1

    val score: Int get() = if (isTriple) DiceJack.TARGET else dice.sum()

    val isBust: Boolean get() = score > DiceJack.TARGET

    val diceLeft: Int get() = DiceJack.DICE_COUNT - dice.size

    val canRoll: Boolean get() = !hasRolled || (diceLeft > 0 && score < DiceJack.TARGET)
}

enum class JackPhase { Playing, RoundOver, MatchOver }

data class JackState(
    val players: List<String>,
    val totalRounds: Int,
    val scores: List<Int>,
    val round: Int = 1,
    val starter: Int = 0,
    val turnsTaken: Int = 0,
    val turn: JackTurn = JackTurn(),
    val roundTurns: List<JackTurn?> = List(players.size) { null },
    val roundPoints: List<Int?> = List(players.size) { null },
    val phase: JackPhase = JackPhase.Playing,
    val left: Set<Int> = emptySet(),
) {
    val currentPlayer: Int get() = (starter + turnsTaken) % players.size

    val canRoll: Boolean get() = phase == JackPhase.Playing && turn.canRoll

    val canStand: Boolean get() = phase == JackPhase.Playing && turn.hasRolled

    val leaders: List<Int>
        get() {
            val active = scores.indices.filter { it !in left }
            val max = active.maxOfOrNull { scores[it] } ?: 0
            return active.filter { scores[it] == max }
        }
}

object DiceJack {
    const val DICE_COUNT = 6
    const val FIRST_ROLL_DICE = 3
    const val TARGET = 21

    fun newGame(players: List<String>, totalRounds: Int): JackState {
        require(players.size >= 2) { "Dice Jack needs at least two players" }
        require(totalRounds >= 1) { "A match has at least one round" }
        return JackState(
            players = players,
            totalRounds = totalRounds,
            scores = List(players.size) { 0 },
        )
    }

    fun roll(state: JackState, roller: DiceRoller): JackState {
        if (!state.canRoll) return state
        val count = if (state.turn.hasRolled) 1 else FIRST_ROLL_DICE
        return state.copy(turn = JackTurn(dice = state.turn.dice + List(count) { roller.roll() }))
    }

    fun stand(state: JackState): JackState {
        if (!state.canStand) return state
        return endTurn(state, state.turn)
    }

    fun skip(state: JackState): JackState {
        if (state.phase != JackPhase.Playing) return state
        return endTurn(state, JackTurn())
    }

    private fun endTurn(state: JackState, finished: JackTurn): JackState {
        val player = state.currentPlayer
        val roundTurns = state.roundTurns.mapIndexed { index, turn -> if (index == player) finished else turn }
        if (state.turnsTaken + 1 < state.players.size) {
            return state.copy(roundTurns = roundTurns, turnsTaken = state.turnsTaken + 1, turn = JackTurn())
        }
        val points = roundPoints(roundTurns.map { requireNotNull(it) })
        return state.copy(
            roundTurns = roundTurns,
            roundPoints = points,
            scores = state.scores.mapIndexed { index, score -> score + points[index] },
            phase = if (state.round >= state.totalRounds) JackPhase.MatchOver else JackPhase.RoundOver,
        )
    }

    fun nextRound(state: JackState): JackState {
        if (state.phase != JackPhase.RoundOver) return state
        return state.copy(
            round = state.round + 1,
            starter = (state.starter + 1) % state.players.size,
            turnsTaken = 0,
            turn = JackTurn(),
            roundTurns = List(state.players.size) { null },
            roundPoints = List(state.players.size) { null },
            phase = JackPhase.Playing,
        )
    }

    internal fun roundPoints(turns: List<JackTurn>): List<Int> {
        val values = turns.map { if (it.isBust) 0 else it.score }
        return values.map { value ->
            if (value == 0) 0 else turns.size - (1 + values.count { it > value })
        }
    }
}
