package com.cubes.template.shared.domain.game

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
)

@Serializable
data class PlayerTurn(
    val dice: List<Int> = emptyList(),
    val selected: Set<Int> = emptySet(),
    val rerolled: Boolean = false,
) {
    val hasRolled: Boolean get() = dice.size == HandEvaluator.DICE_COUNT
    val hand: Hand? get() = if (hasRolled) HandEvaluator.evaluate(dice) else null
}

@Serializable
data class RankedPlayer(
    val place: Int,
    val playerId: String,
    val hand: Hand,
)

@Serializable
sealed interface RoundPhase {
    @Serializable
    data object FirstRoll : RoundPhase

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

@Serializable
data class GameState(
    val players: List<Player>,
    val totalRounds: Int,
    val scores: Map<String, Int>,
    val round: Int,
    val startingPlayerIndex: Int,
    val participantIds: List<String>,
    val currentTurnIndex: Int,
    val phase: RoundPhase,
    val turns: Map<String, PlayerTurn>,
    val lastRoundPoints: Map<String, Int> = emptyMap(),
) {
    val turnOrder: List<Player>
        get() = List(players.size) { players[(startingPlayerIndex + it) % players.size] }
            .filter { it.id in participantIds }

    val currentPlayer: Player get() = turnOrder[currentTurnIndex]
    val currentTurn: PlayerTurn get() = turnOf(currentPlayer.id)

    val isTiebreak: Boolean get() = round > totalRounds

    fun turnOf(playerId: String): PlayerTurn = turns[playerId] ?: PlayerTurn()
    fun scoreOf(playerId: String): Int = scores[playerId] ?: 0
    fun player(id: String): Player? = players.firstOrNull { it.id == id }
    fun isParticipant(playerId: String): Boolean = playerId in participantIds

    val leaderIds: List<String>
        get() {
            val max = scores.values.maxOrNull() ?: 0
            return players.map { it.id }.filter { scoreOf(it) == max }
        }
}
