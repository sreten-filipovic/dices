package com.cubes.template.shared.domain.ludo

import com.cubes.template.shared.domain.game.DiceRoller

sealed interface LudoAction {
    data class Roll(val value: Int) : LudoAction
    data class Move(val pawn: Int) : LudoAction
    data object Leave : LudoAction
    data class Timeout(val rolls: List<Int>) : LudoAction
}

data class LudoEvent(val seq: Int, val player: Int, val action: LudoAction)

data class LudoMatch(
    val state: LudoState,
    val missedTurns: List<Int>,
    val lastSeq: Int = 0,
)

data class LudoReplayResult(val match: LudoMatch, val steps: List<LudoState>)

object LudoOnline {
    const val TURN_SECONDS = 60
    const val MAX_MISSED_TURNS = 3

    fun start(names: List<String>): LudoMatch = LudoMatch(Ludo.newGame(names), List(names.size) { 0 })

    fun replay(match: LudoMatch, events: List<LudoEvent>): LudoMatch =
        events.fold(match) { current, event -> apply(current, event).match }

    fun apply(match: LudoMatch, event: LudoEvent): LudoReplayResult {
        val advanced = match.copy(lastSeq = event.seq)
        val state = match.state
        if (state.phase is LudoPhase.GameOver || event.player !in state.players.indices) {
            return LudoReplayResult(advanced, emptyList())
        }
        return when (val action = event.action) {
            is LudoAction.Roll -> {
                if (event.player != state.currentPlayer || state.phase != LudoPhase.WaitingForRoll) {
                    LudoReplayResult(advanced, emptyList())
                } else {
                    val rolled = Ludo.roll(state, FixedRoller(listOf(action.value)))
                    LudoReplayResult(advanced.withState(rolled).resetMissed(event.player), listOf(rolled))
                }
            }
            is LudoAction.Move -> {
                val phase = state.phase as? LudoPhase.ChoosingPawn
                if (event.player != state.currentPlayer || phase == null || action.pawn !in phase.movable) {
                    LudoReplayResult(advanced, emptyList())
                } else {
                    val moved = Ludo.move(state, action.pawn)
                    LudoReplayResult(advanced.withState(moved).resetMissed(event.player), listOf(moved))
                }
            }
            LudoAction.Leave -> {
                val out = Ludo.forfeit(state, event.player)
                LudoReplayResult(advanced.withState(out), if (out == state) emptyList() else listOf(out))
            }
            is LudoAction.Timeout -> timeout(advanced, action.rolls)
        }
    }

    private fun timeout(match: LudoMatch, rolls: List<Int>): LudoReplayResult {
        val player = match.state.currentPlayer
        val missed = match.missedTurns.mapIndexed { index, count -> if (index == player) count + 1 else count }
        if (missed[player] >= MAX_MISSED_TURNS) {
            val out = Ludo.forfeit(match.state, player)
            return LudoReplayResult(match.copy(state = out, missedTurns = missed), listOf(out))
        }
        val roller = FixedRoller(rolls)
        val steps = mutableListOf<LudoState>()
        var state = match.state
        while (state.currentPlayer == player && state.phase !is LudoPhase.GameOver) {
            state = when (val phase = state.phase) {
                is LudoPhase.ChoosingPawn -> Ludo.move(state, autoPawn(state, phase))
                else -> if (roller.hasNext) Ludo.roll(state, roller) else break
            }
            steps += state
        }
        return LudoReplayResult(match.copy(state = state, missedTurns = missed), steps)
    }

    fun autoPawn(state: LudoState, phase: LudoPhase.ChoosingPawn): Int =
        phase.movable.maxBy { state.pawns[state.currentPlayer][it] }

    private fun LudoMatch.withState(state: LudoState) = copy(state = state)

    private fun LudoMatch.resetMissed(player: Int) =
        copy(missedTurns = missedTurns.mapIndexed { index, count -> if (index == player) 0 else count })

    private class FixedRoller(private val values: List<Int>) : DiceRoller {
        private var index = 0
        val hasNext: Boolean get() = index < values.size
        override fun roll(): Int = values[index++]
    }
}
