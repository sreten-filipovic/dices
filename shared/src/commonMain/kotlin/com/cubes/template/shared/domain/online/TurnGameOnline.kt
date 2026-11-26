package com.cubes.template.shared.domain.online

import com.cubes.template.shared.domain.game.DiceRoller

interface TurnRules<S> {
    fun playerCount(state: S): Int
    fun currentPlayer(state: S): Int?
    fun isRoundOver(state: S): Boolean
    fun isMatchOver(state: S): Boolean
    fun diceForNextRoll(state: S): Int
    fun roll(state: S, roller: DiceRoller): S
    fun canEndTurn(state: S): Boolean
    fun endTurn(state: S): S
    fun skipTurn(state: S): S
    fun nextRound(state: S): S
    fun withLeft(state: S, left: Set<Int>): S
    fun endMatch(state: S): S
    fun isFinished(state: S): Boolean = isMatchOver(state)
    fun rematch(state: S): S? = null
}

sealed interface TurnAction {
    data class Roll(val values: List<Int>) : TurnAction
    data object EndTurn : TurnAction
    data object Rematch : TurnAction
    data object Leave : TurnAction
    data object Timeout : TurnAction
    data object Unknown : TurnAction
}

data class TurnEvent(val seq: Int, val player: Int, val action: TurnAction)

data class TurnMatch<S>(
    val state: S,
    val missedTurns: List<Int>,
    val left: Set<Int> = emptySet(),
    val lastSeq: Int = 0,
)

data class TurnStep<S>(val state: S, val rolledBy: Int? = null)

data class TurnReplayResult<S>(val match: TurnMatch<S>, val steps: List<TurnStep<S>>)

class TurnGameOnline<S>(private val rules: TurnRules<S>) {

    fun start(state: S): TurnMatch<S> = TurnMatch(state, List(rules.playerCount(state)) { 0 })

    fun replay(match: TurnMatch<S>, events: List<TurnEvent>): TurnMatch<S> =
        events.fold(match) { current, event -> apply(current, event).match }

    fun isActive(match: TurnMatch<S>, player: Int): Boolean = player !in match.left

    fun apply(match: TurnMatch<S>, event: TurnEvent): TurnReplayResult<S> {
        val advanced = match.copy(lastSeq = event.seq)
        val state = match.state
        if (rules.isFinished(state) || event.player !in 0 until rules.playerCount(state)) {
            return TurnReplayResult(advanced, emptyList())
        }
        if (rules.isMatchOver(state)) {
            return when (event.action) {
                TurnAction.Leave -> leave(advanced, event.player)
                TurnAction.Rematch -> rematch(advanced, event.player)
                else -> TurnReplayResult(advanced, emptyList())
            }
        }
        val isTurn = rules.currentPlayer(state) == event.player
        return when (val action = event.action) {
            is TurnAction.Roll -> {
                val needed = rules.diceForNextRoll(state)
                if (!isTurn || needed == 0 || action.values.size < needed || action.values.any { it !in 1..6 }) {
                    TurnReplayResult(advanced, emptyList())
                } else {
                    val rolled = rules.roll(state, FixedRoller(action.values))
                    settle(advanced.withMissedReset(event.player), listOf(TurnStep(rolled, rolledBy = event.player)))
                }
            }
            TurnAction.EndTurn -> if (isTurn && rules.canEndTurn(state)) {
                settle(advanced.withMissedReset(event.player), listOf(TurnStep(rules.endTurn(state))))
            } else {
                TurnReplayResult(advanced, emptyList())
            }
            TurnAction.Leave -> leave(advanced, event.player)
            TurnAction.Rematch -> TurnReplayResult(advanced, emptyList())
            TurnAction.Timeout -> timeout(advanced)
            TurnAction.Unknown -> TurnReplayResult(advanced, emptyList())
        }
    }

    private fun rematch(match: TurnMatch<S>, player: Int): TurnReplayResult<S> {
        if (player in match.left) return TurnReplayResult(match, emptyList())
        val next = rules.rematch(match.state) ?: return TurnReplayResult(match, emptyList())
        return settle(match.copy(missedTurns = match.missedTurns.map { 0 }), listOf(TurnStep(next)))
    }

    private fun timeout(match: TurnMatch<S>): TurnReplayResult<S> {
        val player = rules.currentPlayer(match.state) ?: return TurnReplayResult(match, emptyList())
        val missed = match.missedTurns.mapIndexed { index, count -> if (index == player) count + 1 else count }
        val counted = match.copy(missedTurns = missed)
        if (missed[player] >= MAX_MISSED_TURNS) return leave(counted, player)
        val state = match.state
        val ended = if (rules.canEndTurn(state)) rules.endTurn(state) else rules.skipTurn(state)
        return settle(counted, listOf(TurnStep(ended)))
    }

    private fun leave(match: TurnMatch<S>, player: Int): TurnReplayResult<S> {
        if (player in match.left) return TurnReplayResult(match, emptyList())
        val left = match.left + player
        val marked = rules.withLeft(match.state, left)
        val remaining = rules.playerCount(marked) - left.size
        val next = when {
            remaining <= 1 -> rules.endMatch(marked)
            rules.currentPlayer(marked) == player -> rules.skipTurn(marked)
            else -> marked
        }
        return settle(match.copy(left = left), listOf(TurnStep(next)))
    }

    private fun settle(match: TurnMatch<S>, steps: List<TurnStep<S>>): TurnReplayResult<S> {
        val all = steps.toMutableList()
        repeat(MAX_SETTLE_STEPS) {
            val state = all.last().state
            val next = when {
                rules.isMatchOver(state) -> null
                rules.isRoundOver(state) -> rules.nextRound(state)
                rules.currentPlayer(state)?.let { it in match.left } == true -> rules.skipTurn(state)
                else -> null
            } ?: return TurnReplayResult(match.copy(state = state), all)
            all += TurnStep(next)
        }
        return TurnReplayResult(match.copy(state = all.last().state), all)
    }

    private fun TurnMatch<S>.withMissedReset(player: Int) =
        copy(missedTurns = missedTurns.mapIndexed { index, count -> if (index == player) 0 else count })

    private class FixedRoller(private val values: List<Int>) : DiceRoller {
        private var index = 0
        override fun roll(): Int = values[index++]
    }

    companion object {
        const val TURN_SECONDS = 60
        const val MAX_MISSED_TURNS = 3
        private const val MAX_SETTLE_STEPS = 200
    }
}
