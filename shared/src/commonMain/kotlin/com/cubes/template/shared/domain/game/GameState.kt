package com.cubes.template.shared.domain.game

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
)

/** One player's dice in the current round. */
@Serializable
data class PlayerTurn(
    val dice: List<Int> = emptyList(),
    /** Indices chosen for the second roll (only meaningful during [RoundPhase.SecondRoll]). */
    val selected: Set<Int> = emptySet(),
    val rerolled: Boolean = false,
) {
    val hasRolled: Boolean get() = dice.size == HandEvaluator.DICE_COUNT
    val hand: Hand? get() = if (hasRolled) HandEvaluator.evaluate(dice) else null
}

@Serializable
sealed interface RoundPhase {
    /** Every player in turn order rolls all five dice. */
    @Serializable
    data object FirstRoll : RoundPhase

    /** Every player in turn order either keeps the dice or rerolls a chosen subset. */
    @Serializable
    data object SecondRoll : RoundPhase

    @Serializable
    data class RoundOver(val winnerIds: List<String>) : RoundPhase

    @Serializable
    data class MatchOver(val winnerId: String, val roundWinnerIds: List<String>) : RoundPhase
}

@Serializable
sealed interface GameAction {
    @Serializable
    data object RollFirst : GameAction

    @Serializable
    data class ToggleSelect(val index: Int) : GameAction

    @Serializable
    data object RerollSelected : GameAction

    @Serializable
    data object Keep : GameAction

    @Serializable
    data object NextRound : GameAction
}

/**
 * Full, serializable state of a match. Mutated only through [GameEngine.reduce], so the same
 * logic can later run on a Bluetooth host or a server.
 */
@Serializable
data class GameState(
    val players: List<Player>,
    val targetWins: Int,
    val scores: Map<String, Int>,
    val round: Int,
    val startingPlayerIndex: Int,
    val currentPlayerIndex: Int,
    val phase: RoundPhase,
    val turns: Map<String, PlayerTurn>,
) {
    val currentPlayer: Player get() = players[currentPlayerIndex]
    val currentTurn: PlayerTurn get() = turnOf(currentPlayer.id)

    fun turnOf(playerId: String): PlayerTurn = turns[playerId] ?: PlayerTurn()
    fun scoreOf(playerId: String): Int = scores[playerId] ?: 0
    fun player(id: String): Player? = players.firstOrNull { it.id == id }

    /** Players in this round's turn order. */
    val turnOrder: List<Player>
        get() = List(players.size) { players[(startingPlayerIndex + it) % players.size] }
}
