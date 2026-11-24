package com.cubes.template.shared.ui.main

import com.cubes.template.shared.ui.main.game_setup.OnlineComingSoon
import com.cubes.template.shared.ui.main.online.ludo.OnlineLudoView
import com.cubes.template.shared.ui.main.online.room.OnlineRoomView
import com.cubes.template.shared.ui.main.online.lobby.OnlineLobbyView
import com.cubes.template.shared.ui.main.home.Home
import com.cubes.template.shared.domain.ludo.Ludo
import com.cubes.template.shared.data.online.OnlineGame
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cubes.template.shared.di.ScreenCompositionRoot
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelView
import com.cubes.template.shared.ui.main.dice_jack.DiceJackView
import com.cubes.template.shared.ui.main.dice_roll.DiceRollView
import com.cubes.template.shared.ui.main.game.GameView
import com.cubes.template.shared.ui.main.game_setup.GameSetupView
import com.cubes.template.shared.ui.main.home.HomeView
import com.cubes.template.shared.ui.main.ludo.LudoView
import com.cubes.template.shared.ui.main.profile.ProfileView
import com.cubes.template.shared.ui.main.risk_it.RiskItView
import com.cubes.template.shared.ui.main.sevens.SevensView
import com.cubes.template.shared.ui.main.straight_roll.StraightRollView

@Composable
fun MainView(
    mainScreenNavigator: MainScreenNavigator,
    screenCompositionRoot: ScreenCompositionRoot,
) {
    val backStack by mainScreenNavigator.backStack.collectAsState()
    viewModel { screenCompositionRoot.getResumeGameViewModel(mainScreenNavigator) }
    // The tab bar belongs to the tab roots only; screens opened from them (setup, games) take the full screen.
    val selectedTab = backStack.singleOrNull()?.tab

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        NavDisplay(
            modifier = Modifier
                .weight(1f)
                .then(if (selectedTab != null) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier),
            backStack = backStack,
            onBack = { mainScreenNavigator.systemBack() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {

                entry<MainScreen.Home>(metadata = NoTransition) {
                    val viewModel = viewModel { screenCompositionRoot.getHomeViewModel(mainScreenNavigator) }
                    HomeView(viewModel = viewModel)
                }

                entry<MainScreen.Profile>(metadata = NoTransition) {
                    val viewModel = viewModel { screenCompositionRoot.getProfileViewModel(mainScreenNavigator) }
                    ProfileView(viewModel = viewModel)
                }

                entry<MainScreen.Ludo> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getLudoViewModel(
                            playerNames = screen.playerNames,
                            dieStyles = screen.dieStyles,
                            pawnColors = screen.pawnColors,
                            onBack = mainScreenNavigator::back,
                        )
                    }
                    LudoView(viewModel = viewModel)
                }

                entry<MainScreen.DiceRoll> {
                    val viewModel = viewModel {
                        screenCompositionRoot.getDiceRollViewModel(onBack = mainScreenNavigator::back)
                    }
                    DiceRollView(viewModel = viewModel)
                }

                entry<MainScreen.DiceDuel> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getDiceDuelViewModel(
                            playerNames = screen.playerNames,
                            dieStyles = screen.dieStyles,
                            diceCount = screen.diceCount,
                            onBack = mainScreenNavigator::back,
                        )
                    }
                    DiceDuelView(viewModel = viewModel)
                }

                entry<MainScreen.GameSetup> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getGameSetupViewModel(screen.game, mainScreenNavigator)
                    }
                    GameSetupView(
                        viewModel = viewModel,
                        onlineContent = { modifier ->
                            if (screen.game == Home.GameCard.LUDO) {
                                val lobbyViewModel = viewModel {
                                    screenCompositionRoot.getOnlineLobbyViewModel(
                                        game = OnlineGame.LUDO,
                                        playerOptions = (Ludo.MIN_PLAYERS..Ludo.MAX_PLAYERS).toList(),
                                        navigator = mainScreenNavigator,
                                    )
                                }
                                OnlineLobbyView(viewModel = lobbyViewModel, modifier = modifier)
                            } else {
                                OnlineComingSoon(modifier)
                            }
                        },
                    )
                }

                entry<MainScreen.OnlineRoom> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getOnlineRoomViewModel(screen.roomId, mainScreenNavigator)
                    }
                    DisposableEffect(viewModel) {
                        mainScreenNavigator.setBackHandler(screen, viewModel::onLeaveClick)
                        onDispose { mainScreenNavigator.setBackHandler(screen, null) }
                    }
                    OnlineRoomView(viewModel = viewModel)
                }

                entry<MainScreen.OnlineLudo> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getOnlineLudoViewModel(screen.roomId, onBack = mainScreenNavigator::back)
                    }
                    DisposableEffect(viewModel) {
                        mainScreenNavigator.setBackHandler(screen, viewModel::onBackClick)
                        onDispose { mainScreenNavigator.setBackHandler(screen, null) }
                    }
                    OnlineLudoView(viewModel = viewModel)
                }

                entry<MainScreen.Game> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getGameViewModel(
                            playerNames = screen.playerNames,
                            dieStyles = screen.dieStyles,
                            totalRounds = screen.totalRounds,
                            screenNavigator = mainScreenNavigator,
                        )
                    }
                    GameView(viewModel = viewModel)
                }

                entry<MainScreen.DiceJack> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getDiceJackViewModel(
                            playerNames = screen.playerNames,
                            dieStyles = screen.dieStyles,
                            totalRounds = screen.totalRounds,
                            onHome = mainScreenNavigator::showHome,
                        )
                    }
                    DiceJackView(viewModel = viewModel)
                }

                entry<MainScreen.StraightRoll> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getStraightRollViewModel(
                            playerNames = screen.playerNames,
                            dieStyles = screen.dieStyles,
                            totalRounds = screen.totalRounds,
                            onHome = mainScreenNavigator::showHome,
                        )
                    }
                    StraightRollView(viewModel = viewModel)
                }

                entry<MainScreen.RiskIt> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getRiskItViewModel(
                            playerNames = screen.playerNames,
                            dieStyles = screen.dieStyles,
                            totalRounds = screen.totalRounds,
                            onHome = mainScreenNavigator::showHome,
                        )
                    }
                    RiskItView(viewModel = viewModel)
                }

                entry<MainScreen.Sevens> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getSevensViewModel(
                            playerNames = screen.playerNames,
                            dieStyles = screen.dieStyles,
                            totalRounds = screen.totalRounds,
                            onHome = mainScreenNavigator::showHome,
                        )
                    }
                    SevensView(viewModel = viewModel)
                }
            }
        )

        AnimatedVisibility(
            visible = selectedTab != null,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            var lastTab by remember { mutableStateOf(MainTab.GAMES) }
            if (selectedTab != null) lastTab = selectedTab
            MainTabBar(selected = lastTab, onSelect = mainScreenNavigator::selectTab)
        }
    }
}

/** Tab roots replace each other instantly instead of sliding in like pushed screens. */
private val NoTransition = NavDisplay.transitionSpec { EnterTransition.None togetherWith ExitTransition.None }

private val MainScreen.tab: MainTab?
    get() = when (this) {
        MainScreen.Home -> MainTab.GAMES
        MainScreen.Profile -> MainTab.PROFILE
        else -> null
    }
