package com.cubes.template.shared.ui.main

import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.main.home.Home

sealed class MainScreen {
    data object Home : MainScreen()
    data object DiceRoll : MainScreen()
    data class DiceDuel(val playerNames: List<String>, val dieStyles: List<DieStyle>, val diceCount: Int) : MainScreen()
    data class GameSetup(val game: Home.GameCard) : MainScreen()
    data class Game(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
    data class DiceJack(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
    data class Sevens(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
}
