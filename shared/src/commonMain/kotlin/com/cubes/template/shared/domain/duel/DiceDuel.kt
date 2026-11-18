package com.cubes.template.shared.domain.duel

import com.cubes.template.shared.domain.game.DiceRoller

/**
 * One duel between [players] (identified by index): each rolls [diceCount] dice once, the highest
 * sum wins. If the highest sum is shared, only the tied players roll again until one of them wins.
 * [wins] accumulates across duels; [starter] moves on so nobody always rolls first.
 */
data class DuelState(
    val players: List<String>,
    val diceCount: Int,
    /** Latest dice per player; `null` while that player still has to roll in the current stage of the duel. */
    val hands: List<List<Int>?> = List(players.size) { null },
    val wins: List<Int> = List(players.size) { 0 },
    val starter: Int = 0,
    /** Players rolling in the current stage: everyone, or only the tied leaders in a tiebreak. */
    val participants: List<Int> = players.indices.toList(),
    /** Per player the sums of the rolls that ended in a tie earlier in this duel, oldest first. */
    val tiedSums: List<List<Int>> = List(players.size) { emptyList() },
) {
    /** True when every participant of the current stage has rolled. */
    val isStageOver: Boolean get() = participants.all { hands[it] != null }

    /** Player who rolls next, or `null` when the stage is over. Players roll in order from [starter]. */
    val currentPlayer: Int?
        get() = players.indices
            .map { (starter + it) % players.size }
            .firstOrNull { it in participants && hands[it] == null }

    /** Sum of a player's latest dice, or `null` if they have not rolled yet. */
    fun sumOf(player: Int): Int? = hands[player]?.sum()

    /** Participants with the highest sum of the finished stage; empty while it is still running. */
    val leaders: List<Int>
        get() {
            if (!isStageOver) return emptyList()
            val best = participants.maxOf { sumOf(it) ?: 0 }
            return participants.filter { sumOf(it) == best }
        }

    /** The stage is over but the highest sum is shared: [leaders] have to roll again. */
    val needsTiebreak: Boolean get() = leaders.size > 1

    /** Winner of the duel; `null` while it is still running or waiting for a tiebreak. */
    val winner: Int? get() = leaders.singleOrNull()

    /** True when the duel has a winner. */
    val isOver: Boolean get() = winner != null

    /** True while only the tied leaders are rolling. */
    val isTiebreak: Boolean get() = participants.size < players.size

    /** All sums a player rolled in this duel, oldest first; more than one only for players who tied. */
    fun sumsOf(player: Int): List<Int> = tiedSums[player] + listOfNotNull(sumOf(player))

    /**
     * Place of a player in the duel, starting at 1. Players are compared roll by roll, so a tiebreak
     * only reorders the players who were tied; equal results share a place.
     */
    fun placeOf(player: Int): Int {
        val mine = sumsOf(player)
        return 1 + players.indices.count { compareSums(sumsOf(it), mine) > 0 }
    }

    private fun compareSums(a: List<Int>, b: List<Int>): Int {
        for (i in 0 until minOf(a.size, b.size)) {
            if (a[i] != b[i]) return a[i].compareTo(b[i])
        }
        return a.size.compareTo(b.size)
    }
}

object DiceDuel {
    const val MIN_PLAYERS = 2
    val DICE_COUNT_OPTIONS = listOf(1, 2, 3)

    fun newGame(players: List<String>, diceCount: Int): DuelState {
        require(players.size >= MIN_PLAYERS) { "A duel needs at least $MIN_PLAYERS players" }
        require(diceCount in DICE_COUNT_OPTIONS) { "A duel is played with one of $DICE_COUNT_OPTIONS dice" }
        return DuelState(players = players, diceCount = diceCount)
    }

    /** Rolls for the current player; the roll that decides the duel also scores it. */
    fun roll(state: DuelState, roller: DiceRoller): DuelState {
        val player = state.currentPlayer ?: return state
        val rolled = state.copy(
            hands = state.hands.mapIndexed { index, hand ->
                if (index == player) List(state.diceCount) { roller.roll() } else hand
            },
        )
        val winner = rolled.winner ?: return rolled
        return rolled.copy(wins = rolled.wins.mapIndexed { index, wins -> if (index == winner) wins + 1 else wins })
    }

    /** Sends the tied leaders back to the table; everyone else keeps their dice and sits the tiebreak out. */
    fun tiebreak(state: DuelState): DuelState {
        if (!state.needsTiebreak) return state
        val tied = state.leaders
        return state.copy(
            participants = tied,
            hands = state.hands.mapIndexed { index, hand -> if (index in tied) null else hand },
            tiedSums = state.tiedSums.mapIndexed { index, sums ->
                if (index in tied) sums + (state.sumOf(index) ?: 0) else sums
            },
        )
    }

    /** Clears the table for another duel; the next player rolls first this time. */
    fun nextDuel(state: DuelState): DuelState =
        if (state.isOver) {
            DuelState(
                players = state.players,
                diceCount = state.diceCount,
                wins = state.wins,
                starter = (state.starter + 1) % state.players.size,
            )
        } else {
            state
        }
}
