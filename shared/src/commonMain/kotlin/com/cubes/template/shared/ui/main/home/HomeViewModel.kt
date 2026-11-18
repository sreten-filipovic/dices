package com.cubes.template.shared.ui.main.home

import androidx.lifecycle.ViewModel

class HomeViewModel(
    private val screenNavigator: HomeScreenNavigator,
) : ViewModel(), HomeViewListener {

    val viewData = Home.ViewData(listener = this)

    override fun onGameClick(game: Home.GameCard) {
        if (!game.available) return
        when (game) {
            Home.GameCard.DICE_POKER,
            Home.GameCard.DICE_DUEL,
            Home.GameCard.SEVENS -> screenNavigator.showGameSetup(game)
            // Rules not defined yet
            Home.GameCard.DICE_JACK,
            Home.GameCard.RISK_IT,
            Home.GameCard.STRAIGHT_ROLL -> Unit
        }
    }
}
