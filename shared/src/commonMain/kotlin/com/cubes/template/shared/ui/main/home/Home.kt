package com.cubes.template.shared.ui.main.home

object Home {
    data class ViewData(val listener: HomeViewListener)
}

interface HomeViewListener {
    fun onNewGameClick()
    fun onTryDieClick()
}

interface HomeScreenNavigator {
    fun showGameSetup()
    fun showDiceRoll()
}
