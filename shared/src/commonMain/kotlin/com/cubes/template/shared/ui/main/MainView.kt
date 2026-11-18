package com.cubes.template.shared.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.cubes.template.shared.ui.main.sevens.SevensView

@Composable
fun MainView(
    mainScreenNavigator: MainScreenNavigator,
    screenCompositionRoot: ScreenCompositionRoot,
) {
    val backStack by mainScreenNavigator.backStack.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { mainScreenNavigator.back() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {

                entry<MainScreen.Home> {
                    val viewModel = viewModel { screenCompositionRoot.getHomeViewModel(mainScreenNavigator) }
                    HomeView(viewModel = viewModel)
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
                    GameSetupView(viewModel = viewModel)
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
    }
}
