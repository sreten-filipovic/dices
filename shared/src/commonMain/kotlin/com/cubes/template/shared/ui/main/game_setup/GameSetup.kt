package com.cubes.template.shared.ui.main.game_setup

import com.cubes.template.shared.domain.duel.DiceDuel
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.main.home.Home

object GameSetup {

    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 6
    val ROUND_OPTIONS = listOf(1, 3, 5)
    val DICE_COUNT_OPTIONS = DiceDuel.DICE_COUNT_OPTIONS

    data class ViewData(
        val game: Home.GameCard = Home.GameCard.DICE_POKER,
        val names: List<String> = List(MIN_PLAYERS) { "" },
        val dieStyles: List<DieStyle> = List(MIN_PLAYERS) { DieStyle.WHITE },
        val settingsPlayer: Int? = null,
        val totalRounds: Int = 3,
        val diceCount: Int = DICE_COUNT_OPTIONS.first(),
        val listener: GameSetupViewListener,
    ) {
        val canAddPlayer: Boolean get() = names.size < MAX_PLAYERS
        val canRemovePlayer: Boolean get() = names.size > MIN_PLAYERS

        val choosesDiceCount: Boolean get() = game == Home.GameCard.DICE_DUEL
    }
}

interface GameSetupViewListener {
    fun onNameChanged(index: Int, name: String)
    fun onAddPlayerClick()
    fun onRemovePlayerClick(index: Int)
    fun onSettingsClick(index: Int)
    fun onDieStyleSelected(style: DieStyle)
    fun onSettingsDismiss()
    fun onTotalRoundsChanged(totalRounds: Int)
    fun onDiceCountChanged(diceCount: Int)
    fun onStartClick()
    fun onBackClick()
}

interface GameSetupScreenNavigator {
    fun startGame(game: Home.GameCard, playerNames: List<String>, dieStyles: List<DieStyle>, totalRounds: Int)
    fun startDuel(playerNames: List<String>, dieStyles: List<DieStyle>, diceCount: Int)
    fun back()
}
