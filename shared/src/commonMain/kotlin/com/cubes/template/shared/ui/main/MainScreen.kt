package com.cubes.template.shared.ui.main

import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.main.home.Home

sealed class MainScreen {
    data object Home : MainScreen()
    data object Profile : MainScreen()
    data object Settings : MainScreen()
    data object DiceRoll : MainScreen()
    data class Ludo(
        val playerNames: List<String>,
        val dieStyles: List<DieStyle>,
        val pawnColors: List<PawnColor>,
    ) : MainScreen()
    data class DiceDuel(val playerNames: List<String>, val dieStyles: List<DieStyle>, val diceCount: Int) : MainScreen()
    data class GameSetup(val game: Home.GameCard) : MainScreen()
    data class OnlineRoom(val roomId: String) : MainScreen()
    data class OnlineLudo(val roomId: String) : MainScreen()
    data class OnlineDicePoker(val roomId: String) : MainScreen()
    data class OnlineRiskIt(val roomId: String) : MainScreen()
    data class OnlineDiceDuel(val roomId: String) : MainScreen()
    data class OnlineDiceJack(val roomId: String) : MainScreen()
    data class OnlineSevens(val roomId: String) : MainScreen()
    data class OnlineStraightRoll(val roomId: String) : MainScreen()
    data class Game(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
    data class DiceJack(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
    data class StraightRoll(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
    data class RiskIt(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
    data class Sevens(val playerNames: List<String>, val dieStyles: List<DieStyle>, val totalRounds: Int) : MainScreen()
}
