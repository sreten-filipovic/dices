package com.cubes.template.shared.domain.game

/**
 * Pure game rules. Round flow: all players roll once (in turn order), then all players
 * in the same order either keep their dice or reroll the dice they selected. After the last
 * player the hands are compared; every player with the best hand gets a point. The match ends
 * when a single leader has at least `targetWins` points. The starting player rotates each round.
 *
 * Invalid actions for the current phase are ignored (the same state is returned).
 */
object GameEngine {

    const val MIN_PLAYERS = 2

    fun newGame(playerNames: List<String>, targetWins: Int): GameState {
        require(playerNames.size >= MIN_PLAYERS) { "At least $MIN_PLAYERS players required" }
        require(targetWins >= 1) { "targetWins must be >= 1" }
        val players = playerNames.mapIndexed { index, name -> Player(id = "p${index + 1}", name = name) }
        return GameState(
            players = players,
            targetWins = targetWins,
            scores = players.associate { it.id to 0 },
            round = 1,
            startingPlayerIndex = 0,
            currentPlayerIndex = 0,
            phase = RoundPhase.FirstRoll,
            turns = players.associate { it.id to PlayerTurn() },
        )
    }

    fun reduce(state: GameState, action: GameAction, roller: DiceRoller): GameState =
        when (action) {
            GameAction.RollFirst -> rollFirst(state, roller)
            is GameAction.ToggleSelect -> toggleSelect(state, action.index)
            GameAction.RerollSelected -> rerollSelected(state, roller)
            GameAction.Keep -> keep(state)
            GameAction.NextRound -> nextRound(state)
        }

    private fun rollFirst(state: GameState, roller: DiceRoller): GameState {
        if (state.phase != RoundPhase.FirstRoll) return state
        val dice = List(HandEvaluator.DICE_COUNT) { roller.roll() }
        val updated = state.updateCurrentTurn { PlayerTurn(dice = dice) }
        val next = updated.nextPlayerIndex()
        return if (next == updated.startingPlayerIndex) {
            updated.copy(phase = RoundPhase.SecondRoll, currentPlayerIndex = updated.startingPlayerIndex)
        } else {
            updated.copy(currentPlayerIndex = next)
        }
    }

    private fun toggleSelect(state: GameState, index: Int): GameState {
        if (state.phase != RoundPhase.SecondRoll) return state
        if (index !in 0 until HandEvaluator.DICE_COUNT) return state
        return state.updateCurrentTurn { turn ->
            turn.copy(selected = if (index in turn.selected) turn.selected - index else turn.selected + index)
        }
    }

    private fun rerollSelected(state: GameState, roller: DiceRoller): GameState {
        if (state.phase != RoundPhase.SecondRoll) return state
        val turn = state.currentTurn
        if (turn.selected.isEmpty()) return state
        val dice = turn.dice.mapIndexed { index, value -> if (index in turn.selected) roller.roll() else value }
        return finishSecondTurn(state.updateCurrentTurn { it.copy(dice = dice, selected = emptySet(), rerolled = true) })
    }

    private fun keep(state: GameState): GameState {
        if (state.phase != RoundPhase.SecondRoll) return state
        return finishSecondTurn(state.updateCurrentTurn { it.copy(selected = emptySet()) })
    }

    private fun finishSecondTurn(state: GameState): GameState {
        val next = state.nextPlayerIndex()
        if (next != state.startingPlayerIndex) return state.copy(currentPlayerIndex = next)

        val winnerIds = roundWinners(state)
        val scores = state.scores.mapValues { (id, score) -> if (id in winnerIds) score + 1 else score }
        val maxScore = scores.values.max()
        val leaders = scores.filterValues { it == maxScore }.keys
        val phase = if (maxScore >= state.targetWins && leaders.size == 1) {
            RoundPhase.MatchOver(winnerId = leaders.single(), roundWinnerIds = winnerIds)
        } else {
            RoundPhase.RoundOver(winnerIds = winnerIds)
        }
        return state.copy(scores = scores, phase = phase)
    }

    private fun nextRound(state: GameState): GameState {
        if (state.phase !is RoundPhase.RoundOver) return state
        val start = (state.startingPlayerIndex + 1) % state.players.size
        return state.copy(
            round = state.round + 1,
            startingPlayerIndex = start,
            currentPlayerIndex = start,
            phase = RoundPhase.FirstRoll,
            turns = state.players.associate { it.id to PlayerTurn() },
        )
    }

    /** Ids of all players holding the best hand, in turn order. */
    fun roundWinners(state: GameState): List<String> {
        val hands = state.turnOrder.associate { it.id to requireNotNull(state.turnOf(it.id).hand) }
        val best = hands.values.max()
        return hands.filterValues { it.compareTo(best) == 0 }.keys.toList()
    }

    private fun GameState.nextPlayerIndex() = (currentPlayerIndex + 1) % players.size

    private fun GameState.updateCurrentTurn(transform: (PlayerTurn) -> PlayerTurn): GameState =
        copy(turns = turns + (currentPlayer.id to transform(currentTurn)))
}
