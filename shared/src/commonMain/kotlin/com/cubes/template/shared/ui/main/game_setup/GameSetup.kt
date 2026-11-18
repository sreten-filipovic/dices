package com.cubes.template.shared.ui.main.game_setup

import com.cubes.template.shared.domain.duel.DiceDuel
import com.cubes.template.shared.ui.main.home.Home

object GameSetup {

    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 6
    /** Match lengths the players can choose from. */
    val ROUND_OPTIONS = listOf(1, 3, 5)
    /** Dice per roll the players of Dice Duel can choose from. */
    val DICE_COUNT_OPTIONS = DiceDuel.DICE_COUNT_OPTIONS

    data class ViewData(
        /** The game this setup is for. */
        val game: Home.GameCard = Home.GameCard.DICE_POKER,
        /** Raw names as typed; blank names fall back to "Player N" when the game starts. */
        val names: List<String> = List(MIN_PLAYERS) { "" },
        val totalRounds: Int = 3,
        val diceCount: Int = DICE_COUNT_OPTIONS.first(),
        val listener: GameSetupViewListener,
    ) {
        val canAddPlayer: Boolean get() = names.size < MAX_PLAYERS
        val canRemovePlayer: Boolean get() = names.size > MIN_PLAYERS

        /** Dice Duel has no fixed length; its setup asks for the number of dice instead of rounds. */
        val choosesDiceCount: Boolean get() = game == Home.GameCard.DICE_DUEL
    }
}

interface GameSetupViewListener {
    fun onNameChanged(index: Int, name: String)
    fun onAddPlayerClick()
    fun onRemovePlayerClick(index: Int)
    fun onTotalRoundsChanged(totalRounds: Int)
    fun onDiceCountChanged(diceCount: Int)
    fun onStartClick()
    fun onBackClick()
}

interface GameSetupScreenNavigator {
    fun startGame(game: Home.GameCard, playerNames: List<String>, totalRounds: Int)
    fun startDuel(playerNames: List<String>, diceCount: Int)
    fun back()
}
