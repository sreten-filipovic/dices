package com.cubes.template.shared.ui.main

/**
 * Destinations inside the Main flow. Group them per feature as the app grows:
 * `sealed class Profile : MainScreen() { data object Home : Profile() ... }`
 */
sealed class MainScreen {
    data object Home : MainScreen()
    data object DiceRoll : MainScreen()
    data object DiceDuel : MainScreen()
    data object GameSetup : MainScreen()
    data class Game(val playerNames: List<String>, val totalRounds: Int) : MainScreen()
}
