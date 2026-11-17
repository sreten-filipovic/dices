package com.cubes.template.shared.domain.duel

import com.cubes.template.shared.domain.game.DiceRoller

/**
 * One duel between two players (indices 0 and 1): each rolls [diceCount] dice once, the higher sum wins.
 * [wins] accumulates across duels; [starter] alternates so nobody always rolls first.
 */
data class DuelState(
    val diceCount: Int = 1,
    /** Rolled dice per player; `null` until that player has rolled in the current duel. */
    val hands: List<List<Int>?> = listOf(null, null),
    val wins: List<Int> = listOf(0, 0),
    val starter: Int = 0,
) {
    /** Player who rolls next, or `null` when both have rolled. */
    val currentPlayer: Int?
        get() = when {
            hands[starter] == null -> starter
            hands[1 - starter] == null -> 1 - starter
            else -> null
        }

    val isOver: Boolean get() = currentPlayer == null

    /** The number of dice can only change before anyone has rolled in the current duel. */
    val canChangeDiceCount: Boolean get() = hands.all { it == null }

    /** Winner of the finished duel; `null` while it is still running or when it ended in a draw. */
    val winner: Int?
        get() {
            val first = hands[0]?.sum() ?: return null
            val second = hands[1]?.sum() ?: return null
            return when {
                first > second -> 0
                second > first -> 1
                else -> null
            }
        }
}

object DiceDuel {
    val DICE_COUNT_OPTIONS = listOf(1, 2)

    /** Rolls for the current player; the roll that finishes the duel also scores it. */
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

    fun setDiceCount(state: DuelState, diceCount: Int): DuelState =
        if (state.canChangeDiceCount && diceCount in DICE_COUNT_OPTIONS) state.copy(diceCount = diceCount) else state

    /** Clears the table for another duel; the other player rolls first this time. */
    fun nextDuel(state: DuelState): DuelState =
        if (state.isOver) state.copy(hands = listOf(null, null), starter = 1 - state.starter) else state
}
