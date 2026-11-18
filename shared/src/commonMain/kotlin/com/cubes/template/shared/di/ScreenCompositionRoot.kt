package com.cubes.template.shared.di

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.StringProviderImpl
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.RandomDiceRoller
import com.cubes.template.shared.ui.common.Validator
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelViewModel
import com.cubes.template.shared.ui.main.dice_roll.DiceRollViewModel
import com.cubes.template.shared.ui.main.game.GameScreenNavigator
import com.cubes.template.shared.ui.main.game.GameViewModel
import com.cubes.template.shared.ui.main.game_setup.GameSetupScreenNavigator
import com.cubes.template.shared.ui.main.game_setup.GameSetupViewModel
import com.cubes.template.shared.ui.main.home.Home
import com.cubes.template.shared.ui.main.home.HomeScreenNavigator
import com.cubes.template.shared.ui.main.home.HomeViewModel
import com.cubes.template.shared.ui.main.sevens.SevensViewModel

/**
 * Factory for screen ViewModels. One `get<Screen>ViewModel(...)` per screen; the screen's
 * navigator interface and arguments come from the caller (MainView), everything else from here.
 */
class ScreenCompositionRoot(
    private val appCompositionRoot: AppCompositionRoot,
) {

    val remoteDataSource get() = appCompositionRoot.remoteDataSource
    val localDataSource get() = appCompositionRoot.localDataSource
    val popupManager get() = appCompositionRoot.popupManager
    val stringProvider: StringProvider get() = StringProviderImpl()
    val validator get() = Validator(stringProvider)
    val diceRoller: DiceRoller get() = RandomDiceRoller()

    fun getHomeViewModel(screenNavigator: HomeScreenNavigator) = HomeViewModel(
        screenNavigator = screenNavigator,
    )

    fun getDiceRollViewModel(onBack: () -> Unit) = DiceRollViewModel(
        diceRoller = diceRoller,
        onBack = onBack,
    )

    fun getDiceDuelViewModel(onBack: () -> Unit) = DiceDuelViewModel(
        diceRoller = diceRoller,
        onBack = onBack,
    )

    fun getGameSetupViewModel(game: Home.GameCard, screenNavigator: GameSetupScreenNavigator) = GameSetupViewModel(
        game = game,
        screenNavigator = screenNavigator,
        stringProvider = stringProvider,
    )

    fun getGameViewModel(
        playerNames: List<String>,
        totalRounds: Int,
        screenNavigator: GameScreenNavigator,
    ) = GameViewModel(
        playerNames = playerNames,
        totalRounds = totalRounds,
        diceRoller = diceRoller,
        screenNavigator = screenNavigator,
    )

    fun getSevensViewModel(
        playerNames: List<String>,
        totalRounds: Int,
        onHome: () -> Unit,
    ) = SevensViewModel(
        playerNames = playerNames,
        totalRounds = totalRounds,
        diceRoller = diceRoller,
        onHome = onHome,
    )
}
