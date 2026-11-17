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

/** One line of the round ranking; [place] starts at 1 and is shared by equal hands. */
@Serializable
data class RankedPlayer(
    val place: Int,
    val playerId: String,
    val hand: Hand,
)

@Serializable
sealed interface RoundPhase {
    /** The current player rolls all five dice. */
    @Serializable
    data object FirstRoll : RoundPhase

    /** The current player either keeps the dice or rerolls a chosen subset. */
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
    /** Regular rounds chosen at setup (1, 3 or 5); tiebreak rounds come on top. */
    val totalRounds: Int,
    /** Total points per player id. */
    val scores: Map<String, Int>,
    val round: Int,
    /** Index into [players] of who starts this round; rotates every round. */
    val startingPlayerIndex: Int,
    /** Players taking part in this round: everyone, or only the tied leaders in a tiebreak. */
    val participantIds: List<String>,
    /** Index into [turnOrder] of the player whose turn it is. */
    val currentTurnIndex: Int,
    val phase: RoundPhase,
    val turns: Map<String, PlayerTurn>,
    /** Points awarded in the last finished round (shown on the result screen). */
    val lastRoundPoints: Map<String, Int> = emptyMap(),
) {
    /** Participants of this round in playing order. */
    val turnOrder: List<Player>
        get() = List(players.size) { players[(startingPlayerIndex + it) % players.size] }
            .filter { it.id in participantIds }

    val currentPlayer: Player get() = turnOrder[currentTurnIndex]
    val currentTurn: PlayerTurn get() = turnOf(currentPlayer.id)

    /** True for the extra rounds played after [totalRounds] to break a tie for first place. */
    val isTiebreak: Boolean get() = round > totalRounds

    fun turnOf(playerId: String): PlayerTurn = turns[playerId] ?: PlayerTurn()
    fun scoreOf(playerId: String): Int = scores[playerId] ?: 0
    fun player(id: String): Player? = players.firstOrNull { it.id == id }
    fun isParticipant(playerId: String): Boolean = playerId in participantIds

    /** Ids of the players with the highest total score. */
    val leaderIds: List<String>
        get() {
            val max = scores.values.maxOrNull() ?: 0
            return players.map { it.id }.filter { scoreOf(it) == max }
        }
}
