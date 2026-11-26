package com.cubes.template.shared.ui.main

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.OpenUrlHandler
import com.cubes.template.shared.ui.app.AppScreenNavigator
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.main.game.GameScreenNavigator
import com.cubes.template.shared.ui.main.game_setup.GameSetupScreenNavigator
import com.cubes.template.shared.ui.main.home.Home
import com.cubes.template.shared.ui.main.home.HomeScreenNavigator
import com.cubes.template.shared.ui.main.online.OnlineScreenNavigator
import com.cubes.template.shared.ui.main.profile.ProfileScreenNavigator
import com.cubes.template.shared.ui.password.Password
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface MainScreenNavigator :
    HomeScreenNavigator,
    GameSetupScreenNavigator,
    GameScreenNavigator,
    ProfileScreenNavigator,
    OnlineScreenNavigator {

    val backStack: StateFlow<List<MainScreen>>

    fun systemBack()
    fun setBackHandler(screen: MainScreen, handler: (() -> Unit)?)

    fun navigateTo(screen: MainScreen)
    fun selectTab(tab: MainTab)
    override fun back()
}

class MainNavigatorImpl(
    initialBackStack: List<MainScreen>,
    val appScreenNavigator: AppScreenNavigator,
    @Suppress("unused") private val openUrlHandler: OpenUrlHandler,
) : ViewModel(), MainScreenNavigator {

    override val backStack = MutableStateFlow(initialBackStack)

    private val backHandlers = mutableMapOf<MainScreen, () -> Unit>()

    override fun systemBack() {
        backHandlers[backStack.value.last()]?.invoke() ?: back()
    }

    override fun setBackHandler(screen: MainScreen, handler: (() -> Unit)?) {
        if (handler == null) backHandlers.remove(screen) else backHandlers[screen] = handler
    }

    override fun showOnlineRoom(roomId: String) = navigateTo(MainScreen.OnlineRoom(roomId))

    override fun showOnlineLudo(roomId: String) {
        backStack.update { it.dropLastWhile { last -> last is MainScreen.OnlineRoom } + MainScreen.OnlineLudo(roomId) }
    }

    override fun navigateTo(screen: MainScreen) {
        backStack.update { it + screen }
    }

    override fun back() {
        val stack = backStack.value
        when {
            stack.size > 1 -> backStack.update { it.dropLast(1) }
            stack.single() != MainScreen.Home -> showHome()
        }
    }

    override fun selectTab(tab: MainTab) {
        backStack.value = listOf(
            when (tab) {
                MainTab.GAMES -> MainScreen.Home
                MainTab.PROFILE -> MainScreen.Profile
                MainTab.SETTINGS -> MainScreen.Settings
                MainTab.SHOP -> return
            }
        )
    }

    override fun showAuth() = appScreenNavigator.showAuth()

    override fun showChangePassword() = appScreenNavigator.showPassword(Password.Mode.CHANGE)

    override fun showGameSetup(game: Home.GameCard) = navigateTo(MainScreen.GameSetup(game))

    override fun startGame(
        game: Home.GameCard,
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
        pawnColors: List<PawnColor>,
    ) {
        val screen = when (game) {
            Home.GameCard.SEVENS -> MainScreen.Sevens(playerNames, dieStyles, totalRounds)
            Home.GameCard.STRAIGHT_ROLL -> MainScreen.StraightRoll(playerNames, dieStyles, totalRounds)
            Home.GameCard.RISK_IT -> MainScreen.RiskIt(playerNames, dieStyles, totalRounds)
            Home.GameCard.DICE_JACK -> MainScreen.DiceJack(playerNames, dieStyles, totalRounds)
            Home.GameCard.LUDO -> MainScreen.Ludo(playerNames, dieStyles, pawnColors)
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
