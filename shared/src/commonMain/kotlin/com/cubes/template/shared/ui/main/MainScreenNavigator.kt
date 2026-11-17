package com.cubes.template.shared.ui.main

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.OpenUrlHandler
import com.cubes.template.shared.ui.app.AppScreenNavigator
import com.cubes.template.shared.ui.main.game.GameScreenNavigator
import com.cubes.template.shared.ui.main.game_setup.GameSetupScreenNavigator
import com.cubes.template.shared.ui.main.home.HomeScreenNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Aggregates per-feature navigator interfaces. ViewModels depend only on the narrow
 * feature interface (e.g. [HomeScreenNavigator]); this class implements all of them.
 */
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

    // HomeScreenNavigator
    override fun showGameSetup() = navigateTo(MainScreen.GameSetup)
    override fun showDiceRoll() = navigateTo(MainScreen.DiceRoll)

    // GameSetupScreenNavigator: setup is replaced by the game, so back from the game goes home
    override fun startGame(playerNames: List<String>, targetWins: Int) {
        backStack.update { it.dropLastWhile { screen -> screen is MainScreen.GameSetup } + MainScreen.Game(playerNames, targetWins) }
    }

    // GameScreenNavigator
    override fun showHome() {
        backStack.value = listOf(MainScreen.Home)
    }
}
