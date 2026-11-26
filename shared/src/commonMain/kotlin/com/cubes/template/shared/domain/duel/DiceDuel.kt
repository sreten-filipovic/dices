package com.cubes.template.shared.domain.duel

import com.cubes.template.shared.domain.game.DiceRoller

data class DuelState(
    val players: List<String>,
    val diceCount: Int,
    val hands: List<List<Int>?> = List(players.size) { null },
    val wins: List<Int> = List(players.size) { 0 },
    val starter: Int = 0,
    val participants: List<Int> = players.indices.toList(),
    val tiedSums: List<List<Int>> = List(players.size) { emptyList() },
    val left: Set<Int> = emptySet(),
    val out: Set<Int> = emptySet(),
    val ended: Boolean = false,
) {
    val isStageOver: Boolean get() = participants.all { hands[it] != null }

    val currentPlayer: Int?
        get() = players.indices
            .map { (starter + it) % players.size }
            .firstOrNull { it in participants && hands[it] == null }

    fun sumOf(player: Int): Int? = hands[player]?.sum()

    val leaders: List<Int>
        get() {
            if (!isStageOver) return emptyList()
            val contenders = participants.filter { it !in out }
            val best = contenders.maxOfOrNull { sumOf(it) ?: 0 } ?: return emptyList()
            return contenders.filter { sumOf(it) == best }
        }

    val needsTiebreak: Boolean get() = !ended && leaders.size > 1

    val winner: Int?
        get() = leaders.singleOrNull() ?: if (ended) players.indices.singleOrNull { it !in left } else null

    val isOver: Boolean get() = ended || winner != null || (isStageOver && leaders.isEmpty())

    val isTiebreak: Boolean get() = participants.size < players.size

    fun sumsOf(player: Int): List<Int> = tiedSums[player] + listOfNotNull(sumOf(player))

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

    fun roll(state: DuelState, roller: DiceRoller): DuelState {
        val player = state.currentPlayer ?: return state
        return withHand(state, player, List(state.diceCount) { roller.roll() })
    }

    fun skip(state: DuelState): DuelState {
        val player = state.currentPlayer ?: return state
        return withHand(state, player, emptyList())
    }

    fun forfeit(state: DuelState): DuelState =
        if (state.winner != null) state.copy(ended = true) else awardWin(state.copy(ended = true))

    fun withLeft(state: DuelState, left: Set<Int>): DuelState =
        if (state.isOver) state.copy(left = left) else state.copy(left = left, out = left)

    fun rematch(state: DuelState): DuelState? = if (state.isOver && !state.ended) nextDuel(state) else null

    private fun withHand(state: DuelState, player: Int, hand: List<Int>): DuelState =
        awardWin(state.copy(hands = state.hands.mapIndexed { index, old -> if (index == player) hand else old }))

    private fun awardWin(state: DuelState): DuelState {
        val winner = state.winner ?: return state
        return state.copy(wins = state.wins.mapIndexed { index, wins -> if (index == winner) wins + 1 else wins })
    }

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

    fun nextDuel(state: DuelState): DuelState =
        if (state.isOver) {
            DuelState(
                players = state.players,
                diceCount = state.diceCount,
                wins = state.wins,
                starter = (state.starter + 1) % state.players.size,
                left = state.left,
                out = state.left,
            )
        } else {
            state
        }
}
