package com.cubes.template.shared.ui.main.home

object Home {
    data class ViewData(
        val games: List<GameCard> = GameCard.entries,
        val listener: HomeViewListener,
    )

    /**
     * Games in the home screen collection. [dice] is the card artwork: rows of die faces.
     * Games whose rules are not settled yet are listed but not [available].
     */
    enum class GameCard(val dice: List<List<Int>>, val available: Boolean) {
        DICE_POKER(dice = listOf(listOf(6, 6, 6), listOf(5, 5)), available = true),
        DICE_JACK(dice = listOf(listOf(6, 5), listOf(4, 6)), available = false),
        DICE_DUEL(dice = listOf(listOf(6, 5)), available = true),
        LUCKY_SEVEN(dice = listOf(listOf(3, 4)), available = false),
        RISK_IT(dice = listOf(listOf(6, 6, 1)), available = false),
        STRAIGHT_ROLL(dice = listOf(listOf(1, 2, 3), listOf(4, 5, 6)), available = false),
    }
}

interface HomeViewListener {
    fun onGameClick(game: Home.GameCard)
}

interface HomeScreenNavigator {
    fun showGameSetup()
    fun showDiceDuel()
}
