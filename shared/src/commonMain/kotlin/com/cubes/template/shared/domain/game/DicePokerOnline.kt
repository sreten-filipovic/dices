package com.cubes.template.shared.domain.game

sealed interface PokerAction {
    data class Roll(val values: List<Int>) : PokerAction
    data class Reroll(val indices: Set<Int>, val values: List<Int>) : PokerAction
    data object Keep : PokerAction
    data object Leave : PokerAction
    data class Timeout(val rolls: List<Int>) : PokerAction
    data object Unknown : PokerAction
}

data class PokerEvent(val seq: Int, val player: Int, val action: PokerAction)

data class PokerMatch(
    val state: GameState,
    val missedTurns: List<Int>,
    val lastSeq: Int = 0,
)

enum class PokerRollStage { FIRST, SECOND }

data class PokerRoll(val playerId: String, val indices: Set<Int>, val stage: PokerRollStage)

data class PokerStep(val state: GameState, val roll: PokerRoll? = null)

data class PokerReplayResult(val match: PokerMatch, val steps: List<PokerStep>)

object DicePokerOnline {
    const val TURN_SECONDS = 60
    const val MAX_MISSED_TURNS = 3

    fun start(names: List<String>, totalRounds: Int): PokerMatch =
        PokerMatch(GameEngine.newGame(names, totalRounds), List(names.size) { 0 })

    fun replay(match: PokerMatch, events: List<PokerEvent>): PokerMatch =
        events.fold(match) { current, event -> apply(current, event).match }

    fun seatOf(state: GameState, playerId: String): Int = state.players.indexOfFirst { it.id == playerId }

    fun currentSeat(state: GameState): Int? =
        if (state.phase.isPlaying) seatOf(state, state.currentPlayer.id) else null

    fun apply(match: PokerMatch, event: PokerEvent): PokerReplayResult {
        val advanced = match.copy(lastSeq = event.seq)
        val state = match.state
        if (state.phase is RoundPhase.MatchOver || event.player !in state.players.indices) {
            return PokerReplayResult(advanced, emptyList())
        }
        val actorId = state.players[event.player].id
        val isTurn = currentSeat(state) == event.player
        val steps: List<PokerStep> = when (val action = event.action) {
            is PokerAction.Roll -> if (isTurn && state.phase == RoundPhase.FirstRoll) {
                listOfNotNull(rollFirst(state, action.values))
            } else {
                emptyList()
            }
            is PokerAction.Reroll -> if (isTurn && state.phase == RoundPhase.SecondRoll) {
                listOfNotNull(reroll(state, action.indices, action.values))
            } else {
                emptyList()
            }
            PokerAction.Keep -> if (isTurn && state.phase == RoundPhase.SecondRoll) {
                listOf(PokerStep(GameEngine.reduce(state, GameAction.Keep, NO_ROLLER)))
            } else {
                emptyList()
            }
            PokerAction.Leave -> GameEngine.removePlayer(state, actorId)
                .takeIf { it != state }
                ?.let { listOf(PokerStep(it)) }
                .orEmpty()
            is PokerAction.Timeout -> return timeout(advanced, action.rolls)
            PokerAction.Unknown -> emptyList()
        }
        if (steps.isEmpty()) return PokerReplayResult(advanced, emptyList())
        val missed = if (event.action is PokerAction.Leave) advanced.missedTurns else advanced.missedTurns.reset(event.player)
        return finish(advanced.copy(missedTurns = missed), steps)
    }

    private fun timeout(match: PokerMatch, rolls: List<Int>): PokerReplayResult {
        val state = match.state
        val seat = currentSeat(state) ?: return PokerReplayResult(match, emptyList())
        val missed = match.missedTurns.mapIndexed { index, count -> if (index == seat) count + 1 else count }
        val playerId = state.players[seat].id
        if (missed[seat] >= MAX_MISSED_TURNS) {
            return finish(match.copy(missedTurns = missed), listOf(PokerStep(GameEngine.removePlayer(state, playerId))))
        }
        val steps = mutableListOf<PokerStep>()
        var current = state
        if (current.phase == RoundPhase.FirstRoll) {
            val rolled = rollFirst(current, rolls) ?: return PokerReplayResult(match.copy(missedTurns = missed), emptyList())
            steps += rolled
            current = rolled.state
        }
        if (current.phase == RoundPhase.SecondRoll) {
            steps += PokerStep(GameEngine.reduce(current, GameAction.Keep, NO_ROLLER))
        }
        return finish(match.copy(missedTurns = missed), steps)
    }

    private fun finish(match: PokerMatch, steps: List<PokerStep>): PokerReplayResult {
        val last = steps.last().state
        val all = if (last.phase is RoundPhase.RoundOver) {
            steps + PokerStep(GameEngine.reduce(last, GameAction.NextRound, NO_ROLLER))
        } else {
            steps
        }
        return PokerReplayResult(match.copy(state = all.last().state), all)
    }

    private fun rollFirst(state: GameState, values: List<Int>): PokerStep? {
        if (values.size < HandEvaluator.DICE_COUNT || values.any { it !in 1..6 }) return null
        val playerId = state.currentPlayer.id
        val rolled = GameEngine.reduce(state, GameAction.RollFirst, FixedRoller(values))
        return PokerStep(rolled, PokerRoll(playerId, (0 until HandEvaluator.DICE_COUNT).toSet(), PokerRollStage.FIRST))
    }

    private fun reroll(state: GameState, indices: Set<Int>, values: List<Int>): PokerStep? {
        val valid = indices.filter { it in 0 until HandEvaluator.DICE_COUNT }.toSet()
        if (valid.isEmpty() || values.size < valid.size || values.any { it !in 1..6 }) return null
        val playerId = state.currentPlayer.id
        val selected = valid.sorted().fold(clearSelection(state)) { current, index ->
            GameEngine.reduce(current, GameAction.ToggleSelect(index), NO_ROLLER)
        }
        val rolled = GameEngine.reduce(selected, GameAction.RerollSelected, FixedRoller(values))
        return PokerStep(rolled, PokerRoll(playerId, valid, PokerRollStage.SECOND))
    }

    private fun clearSelection(state: GameState): GameState {
        val turn = state.currentTurn
        return state.copy(turns = state.turns + (state.currentPlayer.id to turn.copy(selected = emptySet())))
    }

    private fun List<Int>.reset(seat: Int) = mapIndexed { index, count -> if (index == seat) 0 else count }

    private val RoundPhase.isPlaying: Boolean
        get() = this == RoundPhase.FirstRoll || this == RoundPhase.SecondRoll

    private val NO_ROLLER = DiceRoller { 1 }

    private class FixedRoller(private val values: List<Int>) : DiceRoller {
        private var index = 0
        override fun roll(): Int = values[index++]
    }
}
