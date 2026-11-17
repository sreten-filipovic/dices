package com.cubes.template.shared.di

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.StringProviderImpl
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.RandomDiceRoller
import com.cubes.template.shared.ui.common.Validator
import com.cubes.template.shared.ui.main.dice_roll.DiceRollViewModel
import com.cubes.template.shared.ui.main.game.GameScreenNavigator
import com.cubes.template.shared.ui.main.game.GameViewModel
import com.cubes.template.shared.ui.main.game_setup.GameSetupScreenNavigator
import com.cubes.template.shared.ui.main.game_setup.GameSetupViewModel
import com.cubes.template.shared.ui.main.home.HomeScreenNavigator
import com.cubes.template.shared.ui.main.home.HomeViewModel

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

    fun getGameSetupViewModel(screenNavigator: GameSetupScreenNavigator) = GameSetupViewModel(
        screenNavigator = screenNavigator,
        stringProvider = stringProvider,
    )

    fun getGameViewModel(
        playerNames: List<String>,
        targetWins: Int,
        screenNavigator: GameScreenNavigator,
    ) = GameViewModel(
        playerNames = playerNames,
        targetWins = targetWins,
        diceRoller = diceRoller,
        screenNavigator = screenNavigator,
    )
}
