package com.cubes.template.shared.ui.main.home

object Home {
    data class ViewData(
        val games: List<GameCard> = GameCard.entries - GameCard.LUDO,
        val listener: HomeViewListener,
    )

    enum class GameCard(val available: Boolean) {
        DICE_DUEL(available = true),
        DICE_POKER(available = true),
        DICE_JACK(available = true),
        SEVENS(available = true),
        RISK_IT(available = true),
        STRAIGHT_ROLL(available = true),
        LUDO(available = true),
    }
}

interface HomeViewListener {
    fun onGameClick(game: Home.GameCard)
}

interface HomeScreenNavigator {
    fun showGameSetup(game: Home.GameCard)
}
