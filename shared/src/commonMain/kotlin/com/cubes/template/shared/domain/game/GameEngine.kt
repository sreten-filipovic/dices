package com.cubes.template.shared.domain.game

object GameEngine {

    const val MIN_PLAYERS = 2

    fun newGame(playerNames: List<String>, totalRounds: Int): GameState {
        require(playerNames.size >= MIN_PLAYERS) { "At least $MIN_PLAYERS players required" }
        require(totalRounds >= 1) { "totalRounds must be >= 1" }
        val players = playerNames.mapIndexed { index, name -> Player(id = "p${index + 1}", name = name) }
        return GameState(
            players = players,
            totalRounds = totalRounds,
            scores = players.associate { it.id to 0 },
            round = 1,
            startingPlayerIndex = 0,
            participantIds = players.map { it.id },
            currentTurnIndex = 0,
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

    fun pointsForPlace(place: Int, participants: Int): Int = participants - place

    fun roundRanking(state: GameState): List<RankedPlayer> {
        val hands = state.turnOrder.mapNotNull { player -> state.turnOf(player.id).hand?.let { player.id to it } }
        return hands
            .sortedByDescending { it.second }
            .map { (id, hand) ->
                RankedPlayer(place = 1 + hands.count { it.second > hand }, playerId = id, hand = hand)
            }
    }

    private fun rollFirst(state: GameState, roller: DiceRoller): GameState {
        if (state.phase != RoundPhase.FirstRoll) return state
        val dice = List(HandEvaluator.DICE_COUNT) { roller.roll() }
        return state.updateCurrentTurn { PlayerTurn(dice = dice) }.copy(phase = RoundPhase.SecondRoll)
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
        return finishTurn(state.updateCurrentTurn { it.copy(dice = dice, selected = emptySet(), rerolled = true) })
    }

    private fun keep(state: GameState): GameState {
        if (state.phase != RoundPhase.SecondRoll) return state
        return finishTurn(state.updateCurrentTurn { it.copy(selected = emptySet()) })
    }

    private fun finishTurn(state: GameState): GameState {
        if (state.currentTurnIndex < state.turnOrder.lastIndex) {
            return state.copy(currentTurnIndex = state.currentTurnIndex + 1, phase = RoundPhase.FirstRoll)
        }
        return finishRound(state)
    }

    private fun finishRound(state: GameState): GameState {
        val ranking = roundRanking(state)
        val points = ranking.associate { it.playerId to pointsForPlace(it.place, ranking.size) }
        val scores = state.scores.mapValues { (id, score) -> score + (points[id] ?: 0) }
        val roundWinnerIds = ranking.filter { it.place == 1 }.map { it.playerId }
        val scored = state.copy(scores = scores, lastRoundPoints = points)

        val leaders = scored.leaderIds
        val phase = if (state.round >= state.totalRounds && leaders.size == 1) {
            RoundPhase.MatchOver(winnerId = leaders.single(), roundWinnerIds = roundWinnerIds)
        } else {
            RoundPhase.RoundOver(winnerIds = roundWinnerIds)
        }
        return scored.copy(phase = phase)
    }

    private fun nextRound(state: GameState): GameState {
        if (state.phase !is RoundPhase.RoundOver) return state
        val participants = if (state.round >= state.totalRounds) state.leaderIds else state.players.map { it.id }
        return state.copy(
            round = state.round + 1,
            startingPlayerIndex = (state.startingPlayerIndex + 1) % state.players.size,
            participantIds = participants,
            currentTurnIndex = 0,
            phase = RoundPhase.FirstRoll,
            turns = state.players.associate { it.id to PlayerTurn() },
            lastRoundPoints = emptyMap(),
        )
    }

    private fun GameState.updateCurrentTurn(transform: (PlayerTurn) -> PlayerTurn): GameState =
        copy(turns = turns + (currentPlayer.id to transform(currentTurn)))
}
