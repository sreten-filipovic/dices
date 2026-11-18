package com.cubes.template.shared.ui.main

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.OpenUrlHandler
import com.cubes.template.shared.ui.app.AppScreenNavigator
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.main.game.GameScreenNavigator
import com.cubes.template.shared.ui.main.game_setup.GameSetupScreenNavigator
import com.cubes.template.shared.ui.main.home.Home
import com.cubes.template.shared.ui.main.home.HomeScreenNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface MainScreenNavigator : HomeScreenNavigator, GameSetupScreenNavigator, GameScreenNavigator {

    val backStack: StateFlow<List<MainScreen>>

    fun navigateTo(screen: MainScreen)
    override fun back()
}

class MainNavigatorImpl(
    initialBackStack: List<MainScreen>,
    val appScreenNavigator: AppScreenNavigator,
    @Suppress("unused") private val openUrlHandler: OpenUrlHandler,
) : ViewModel(), MainScreenNavigator {

    override val backStack = MutableStateFlow(initialBackStack)

    override fun navigateTo(screen: MainScreen) {
        backStack.update { it + screen }
    }

    override fun back() {
        if (backStack.value.size > 1) {
            backStack.update { it.dropLast(1) }
        }
    }

    override fun showGameSetup(game: Home.GameCard) = navigateTo(MainScreen.GameSetup(game))

    override fun startGame(
        game: Home.GameCard,
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
    ) {
        val screen = when (game) {
            Home.GameCard.SEVENS -> MainScreen.Sevens(playerNames, dieStyles, totalRounds)
            else -> MainScreen.Game(playerNames, dieStyles, totalRounds)
        }
        backStack.update { it.dropLastWhile { last -> last is MainScreen.GameSetup } + screen }
    }

    override fun startDuel(playerNames: List<String>, dieStyles: List<DieStyle>, diceCount: Int) {
        val screen = MainScreen.DiceDuel(playerNames, dieStyles, diceCount)
        backStack.update { it.dropLastWhile { last -> last is MainScreen.GameSetup } + screen }
    }

    override fun showHome() {
        backStack.value = listOf(MainScreen.Home)
    }
}
