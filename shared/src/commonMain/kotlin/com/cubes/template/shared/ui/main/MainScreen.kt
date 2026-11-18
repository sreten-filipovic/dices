package com.cubes.template.shared.ui.main

import com.cubes.template.shared.ui.main.home.Home

/**
 * Destinations inside the Main flow. Group them per feature as the app grows:
 * `sealed class Profile : MainScreen() { data object Home : Profile() ... }`
 */
sealed class MainScreen {
    data object Home : MainScreen()
    data object DiceRoll : MainScreen()
    data class DiceDuel(val playerNames: List<String>, val diceCount: Int) : MainScreen()
    /** Players and match length for [game], which is started from here. */
    data class GameSetup(val game: Home.GameCard) : MainScreen()
    data class Game(val playerNames: List<String>, val totalRounds: Int) : MainScreen()
    data class Sevens(val playerNames: List<String>, val totalRounds: Int) : MainScreen()
}
