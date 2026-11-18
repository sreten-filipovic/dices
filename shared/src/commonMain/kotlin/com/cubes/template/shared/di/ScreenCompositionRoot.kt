package com.cubes.template.shared.di

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.StringProviderImpl
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.RandomDiceRoller
import com.cubes.template.shared.ui.common.Validator
import com.cubes.template.shared.ui.common.views.DieStyle
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

    fun getDiceDuelViewModel(
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        diceCount: Int,
        onBack: () -> Unit,
    ) = DiceDuelViewModel(
        playerNames = playerNames,
        dieStyles = dieStyles,
        diceCount = diceCount,
        diceRoller = diceRoller,
        onBack = onBack,
    )

    fun getGameSetupViewModel(game: Home.GameCard, screenNavigator: GameSetupScreenNavigator) = GameSetupViewModel(
        game = game,
        screenNavigator = screenNavigator,
        stringProvider = stringProvider,
        localDataSource = localDataSource,
    )

    fun getGameViewModel(
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
        screenNavigator: GameScreenNavigator,
    ) = GameViewModel(
        playerNames = playerNames,
        dieStyles = dieStyles,
        totalRounds = totalRounds,
        diceRoller = diceRoller,
        screenNavigator = screenNavigator,
    )

    fun getSevensViewModel(
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
        onHome: () -> Unit,
    ) = SevensViewModel(
        playerNames = playerNames,
        dieStyles = dieStyles,
        totalRounds = totalRounds,
        diceRoller = diceRoller,
        onHome = onHome,
    )
}
