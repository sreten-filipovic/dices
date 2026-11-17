package com.cubes.template.shared.ui.main.home

import androidx.lifecycle.ViewModel

class HomeViewModel(
    private val screenNavigator: HomeScreenNavigator,
) : ViewModel(), HomeViewListener {

    val viewData = Home.ViewData(listener = this)

    override fun onNewGameClick() = screenNavigator.showGameSetup()
    override fun onTryDieClick() = screenNavigator.showDiceRoll()
}
