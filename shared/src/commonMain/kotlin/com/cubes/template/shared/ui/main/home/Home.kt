package com.cubes.template.shared.ui.main.home

object Home {
    data class ViewData(
        val games: List<GameCard> = GameCard.entries - GameCard.LUDO,
        val listener: HomeViewListener,
    )

    enum class GameCard(val dice: List<List<Int>>, val available: Boolean) {
        DICE_DUEL(dice = listOf(listOf(6, 5)), available = true),
        DICE_POKER(dice = listOf(listOf(6, 6, 6), listOf(5, 5)), available = true),
        DICE_JACK(dice = listOf(listOf(6, 5), listOf(4, 6)), available = true),
        SEVENS(dice = listOf(listOf(3, 4)), available = true),
        RISK_IT(dice = listOf(listOf(6, 6, 1)), available = true),
        STRAIGHT_ROLL(dice = listOf(listOf(1, 2, 3), listOf(4, 5, 6)), available = true),
        LUDO(dice = emptyList(), available = true),
    }
}

interface HomeViewListener {
    fun onGameClick(game: Home.GameCard)
}

interface HomeScreenNavigator {
    fun showGameSetup(game: Home.GameCard)
}
