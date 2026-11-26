package com.cubes.template.shared.domain.ludo

import com.cubes.template.shared.domain.game.DiceRoller

enum class LudoSeat { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT }

data class LudoPlayer(val name: String, val seat: LudoSeat)

sealed interface LudoPhase {
    data object WaitingForRoll : LudoPhase
    data class ChoosingPawn(val roll: Int, val movable: Set<Int>) : LudoPhase
    data class GameOver(val ranking: List<Int>) : LudoPhase
}

data class LudoMove(val player: Int, val pawn: Int, val from: Int, val to: Int, val captured: List<Pair<Int, Int>>)

data class LudoState(
    val players: List<LudoPlayer>,
    val pawns: List<List<Int>>,
    val currentPlayer: Int = 0,
    val rollsLeft: Int,
    val lastRoll: Int? = null,
    val sixesInRow: Int = 0,
    val lastMove: LudoMove? = null,
    val ranking: List<Int> = emptyList(),
    val forfeited: List<Int> = emptyList(),
    val phase: LudoPhase = LudoPhase.WaitingForRoll,
) {
    fun isActive(player: Int): Boolean = player !in ranking && player !in forfeited

    val current: LudoPlayer get() = players[currentPlayer]

    fun finishedCount(player: Int): Int = pawns[player].count { it >= Ludo.TRACK_LENGTH }

    fun placeOf(player: Int): Int? = ranking.indexOf(player).takeIf { it >= 0 }?.plus(1)
}

object Ludo {
    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 4
    const val PAWNS = 4
    const val HOME = -1
    const val TRACK_LENGTH = 40
    const val GOAL_LENGTH = 4
    const val LAST = TRACK_LENGTH + GOAL_LENGTH - 1
    const val EXIT_ROLL = 6
    const val TRIES_WITHOUT_PAWN_IN_PLAY = 3
    const val MAX_SIXES_IN_ROW = 3
    private const val ARM = TRACK_LENGTH / 4

    fun seatsFor(playerCount: Int): List<LudoSeat> = when (playerCount) {
        2 -> listOf(LudoSeat.TOP_LEFT, LudoSeat.BOTTOM_RIGHT)
        3 -> listOf(LudoSeat.TOP_LEFT, LudoSeat.TOP_RIGHT, LudoSeat.BOTTOM_RIGHT)
        else -> LudoSeat.entries
    }

    fun newGame(names: List<String>): LudoState {
        require(names.size in MIN_PLAYERS..MAX_PLAYERS) { "Ludo needs $MIN_PLAYERS–$MAX_PLAYERS players" }
        val players = names.zip(seatsFor(names.size)) { name, seat -> LudoPlayer(name, seat) }
        val pawns = List(players.size) { List(PAWNS) { HOME } }
        return LudoState(players = players, pawns = pawns, rollsLeft = rollsFor(pawns[0]))
    }

    fun startCell(seat: LudoSeat): Int = seat.ordinal * ARM

    fun cellOf(seat: LudoSeat, progress: Int): Int? =
        if (progress in 0 until TRACK_LENGTH) (startCell(seat) + progress) % TRACK_LENGTH else null

    fun target(progress: Int, roll: Int): Int? = when (progress) {
        HOME -> if (roll == EXIT_ROLL) 0 else null
        else -> (progress + roll).takeIf { it <= LAST }
    }

    fun movablePawns(state: LudoState, roll: Int): Set<Int> {
        val own = state.pawns[state.currentPlayer]
        return own.indices.filter { pawn ->
            val from = own[pawn]
            val to = target(from, roll) ?: return@filter false
            val blockedInGoal = to >= TRACK_LENGTH &&
                own.any { it in maxOf(from + 1, TRACK_LENGTH)..to }
            to !in own && !blockedInGoal
        }.toSet()
    }

    fun roll(state: LudoState, roller: DiceRoller): LudoState {
        if (state.phase != LudoPhase.WaitingForRoll) return state
        val roll = roller.roll()
        val sixes = if (roll == EXIT_ROLL) state.sixesInRow + 1 else 0
        val rolled = state.copy(lastRoll = roll, lastMove = null, sixesInRow = sixes)
        if (sixes >= MAX_SIXES_IN_ROW) return nextPlayer(rolled)
        val movable = movablePawns(state, roll)
        return when {
            movable.isNotEmpty() -> rolled.copy(phase = LudoPhase.ChoosingPawn(roll, movable))
            roll == EXIT_ROLL -> rolled.copy(rollsLeft = rollsFor(state.pawns[state.currentPlayer]))
            state.rollsLeft > 1 -> rolled.copy(rollsLeft = state.rollsLeft - 1)
            else -> nextPlayer(rolled)
        }
    }

    fun move(state: LudoState, pawn: Int): LudoState {
        val phase = state.phase as? LudoPhase.ChoosingPawn ?: return state
        if (pawn !in phase.movable) return state
        val player = state.currentPlayer
        val seat = state.players[player].seat
        val from = state.pawns[player][pawn]
        val to = target(from, phase.roll) ?: return state
        val cell = cellOf(seat, to)

        val captured = mutableListOf<Pair<Int, Int>>()
        val pawns = state.pawns.mapIndexed { owner, list ->
            list.mapIndexed { index, progress ->
                when {
                    owner == player && index == pawn -> to
                    owner != player && cell != null && cellOf(state.players[owner].seat, progress) == cell -> {
                        captured += owner to index
                        HOME
                    }
                    else -> progress
                }
            }
        }

        val moved = state.copy(
            pawns = pawns,
            lastMove = LudoMove(player, pawn, from, to, captured),
            phase = LudoPhase.WaitingForRoll,
        )
        if (pawns[player].all { it >= TRACK_LENGTH }) {
            val ranked = moved.copy(ranking = state.ranking + player)
            return finishIfDecided(ranked) ?: nextPlayer(ranked)
        }
        return when {
            phase.roll == EXIT_ROLL -> moved.copy(rollsLeft = rollsFor(pawns[player]))
            else -> nextPlayer(moved)
        }
    }

    fun forfeit(state: LudoState, player: Int): LudoState {
        if (state.phase is LudoPhase.GameOver || !state.isActive(player)) return state
        val out = state.copy(
            pawns = state.pawns.mapIndexed { owner, list -> if (owner == player) list.map { HOME } else list },
            forfeited = state.forfeited + player,
            lastMove = null,
        )
        finishIfDecided(out)?.let { return it }
        return if (player == state.currentPlayer) nextPlayer(out.copy(phase = LudoPhase.WaitingForRoll)) else out
    }

    private fun finishIfDecided(state: LudoState): LudoState? {
        val remaining = state.players.indices.filter(state::isActive)
        if (remaining.size > 1) return null
        val finalRanking = state.ranking + remaining + state.forfeited.reversed()
        return state.copy(ranking = finalRanking, phase = LudoPhase.GameOver(finalRanking))
    }

    private fun nextPlayer(state: LudoState): LudoState {
        val count = state.players.size
        val next = (1..count)
            .map { (state.currentPlayer + it) % count }
            .first(state::isActive)
        return state.copy(currentPlayer = next, rollsLeft = rollsFor(state.pawns[next]), sixesInRow = 0)
    }

    private fun rollsFor(pawns: List<Int>): Int {
        val onTrack = pawns.any { it in 0 until TRACK_LENGTH }
        val inGoal = pawns.filter { it >= TRACK_LENGTH }.sortedDescending()
        val goalPacked = inGoal.withIndex().all { (index, progress) -> progress == LAST - index }
        return if (!onTrack && goalPacked) TRIES_WITHOUT_PAWN_IN_PLAY else 1
    }
}
