package com.cubes.template.shared.di

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.StringProviderImpl
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.RandomDiceRoller
import com.cubes.template.shared.ui.auth.AuthViewModel
import com.cubes.template.shared.data.online.OnlineGame
import com.cubes.template.shared.ui.main.online.OnlineScreenNavigator
import com.cubes.template.shared.ui.main.online.ResumeGameViewModel
import com.cubes.template.shared.ui.main.online.lobby.OnlineLobbyViewModel
import com.cubes.template.shared.ui.main.online.ludo.OnlineLudoViewModel
import com.cubes.template.shared.ui.main.online.room.OnlineRoomViewModel
import com.cubes.template.shared.ui.password.Password
import com.cubes.template.shared.ui.password.PasswordViewModel
import com.cubes.template.shared.ui.common.Validator
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelViewModel
import com.cubes.template.shared.ui.main.dice_jack.DiceJackViewModel
import com.cubes.template.shared.ui.main.dice_roll.DiceRollViewModel
import com.cubes.template.shared.ui.main.game.GameScreenNavigator
import com.cubes.template.shared.ui.main.game.GameViewModel
import com.cubes.template.shared.ui.main.game_setup.GameSetupScreenNavigator
import com.cubes.template.shared.ui.main.game_setup.GameSetupViewModel
import com.cubes.template.shared.ui.main.home.Home
import com.cubes.template.shared.ui.main.home.HomeScreenNavigator
import com.cubes.template.shared.ui.main.home.HomeViewModel
import com.cubes.template.shared.ui.main.ludo.LudoViewModel
import com.cubes.template.shared.ui.main.profile.ProfileScreenNavigator
import com.cubes.template.shared.ui.main.profile.ProfileViewModel
import com.cubes.template.shared.ui.main.online.poker.OnlineDicePokerViewModel
import com.cubes.template.shared.ui.main.online.risk.OnlineRiskItViewModel
import com.cubes.template.shared.ui.main.online.duel.OnlineDiceDuelViewModel
import com.cubes.template.shared.ui.main.online.jack.OnlineDiceJackViewModel
import com.cubes.template.shared.ui.main.online.sevens.OnlineSevensViewModel
import com.cubes.template.shared.ui.main.online.straight.OnlineStraightRollViewModel
import com.cubes.template.shared.ui.main.settings.SettingsViewModel
import com.cubes.template.shared.ui.main.risk_it.RiskItViewModel
import com.cubes.template.shared.ui.main.sevens.SevensViewModel
import com.cubes.template.shared.ui.main.straight_roll.StraightRollViewModel

class ScreenCompositionRoot(
    private val appCompositionRoot: AppCompositionRoot,
) {

    val remoteDataSource get() = appCompositionRoot.remoteDataSource
    val localDataSource get() = appCompositionRoot.localDataSource
    val popupManager get() = appCompositionRoot.popupManager
    val connectivityMonitor get() = appCompositionRoot.connectivityMonitor
    val userSession get() = appCompositionRoot.userSession
    val authRepository get() = appCompositionRoot.authRepository
    val stringProvider: StringProvider get() = StringProviderImpl()
    val validator get() = Validator(stringProvider)
    val diceRoller: DiceRoller get() = RandomDiceRoller()

    fun getAuthViewModel(canGoBack: Boolean, onFinished: () -> Unit, onForgotPassword: (String) -> Unit) =
        AuthViewModel(
            canGoBack = canGoBack,
            authRepository = authRepository,
            userSession = userSession,
            validator = validator,
            stringProvider = stringProvider,
            popupManager = popupManager,
            onFinished = onFinished,
            onForgotPassword = onForgotPassword,
        )

    fun getPasswordViewModel(mode: Password.Mode, email: String, onBack: () -> Unit, onSignedIn: () -> Unit) =
        PasswordViewModel(
            mode = mode,
            email = email,
            authRepository = authRepository,
            userSession = userSession,
            validator = validator,
            stringProvider = stringProvider,
            popupManager = popupManager,
            onBack = onBack,
            onSignedIn = onSignedIn,
        )

    fun getOnlineLobbyViewModel(
        game: OnlineGame,
        playerOptions: List<Int>,
        roundOptions: List<Int>,
        diceCountOptions: List<Int>,
        navigator: OnlineScreenNavigator,
    ) =
        OnlineLobbyViewModel(
            game = game,
            playerOptions = playerOptions,
            roundOptions = roundOptions,
            diceCountOptions = diceCountOptions,
            roomRepository = appCompositionRoot.roomRepository,
            stringProvider = stringProvider,
            popupManager = popupManager,
            navigator = navigator,
        )

    fun getResumeGameViewModel(navigator: OnlineScreenNavigator) = ResumeGameViewModel(
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        navigator = navigator,
    )

    fun getOnlineRoomViewModel(roomId: String, navigator: OnlineScreenNavigator) = OnlineRoomViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        navigator = navigator,
    )

    fun getOnlineLudoViewModel(roomId: String, onBack: () -> Unit) = OnlineLudoViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        onBack = onBack,
    )

    fun getOnlineDicePokerViewModel(roomId: String, onBack: () -> Unit) = OnlineDicePokerViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        onBack = onBack,
    )

    fun getOnlineRiskItViewModel(roomId: String, onBack: () -> Unit) = OnlineRiskItViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        onBack = onBack,
    )

    fun getOnlineDiceJackViewModel(roomId: String, onBack: () -> Unit) = OnlineDiceJackViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        onBack = onBack,
    )

    fun getOnlineSevensViewModel(roomId: String, onBack: () -> Unit) = OnlineSevensViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        onBack = onBack,
    )

    fun getOnlineStraightRollViewModel(roomId: String, onBack: () -> Unit) = OnlineStraightRollViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        onBack = onBack,
    )

    fun getOnlineDiceDuelViewModel(roomId: String, onBack: () -> Unit) = OnlineDiceDuelViewModel(
        roomId = roomId,
        roomRepository = appCompositionRoot.roomRepository,
        stringProvider = stringProvider,
        popupManager = popupManager,
        onBack = onBack,
    )

    fun getProfileViewModel(screenNavigator: ProfileScreenNavigator) = ProfileViewModel(
        authRepository = authRepository,
        userSession = userSession,
        validator = validator,
        stringProvider = stringProvider,
        popupManager = popupManager,
        screenNavigator = screenNavigator,
    )

    fun getSettingsViewModel() = SettingsViewModel(
        themeController = appCompositionRoot.themeController,
    )

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

    fun getLudoViewModel(
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        pawnColors: List<PawnColor>,
        onBack: () -> Unit,
    ) = LudoViewModel(
        playerNames = playerNames,
        dieStyles = dieStyles,
        pawnColors = pawnColors,
        diceRoller = diceRoller,
        onBack = onBack,
    )

    fun getGameSetupViewModel(game: Home.GameCard, screenNavigator: GameSetupScreenNavigator) = GameSetupViewModel(
        game = game,
        screenNavigator = screenNavigator,
        stringProvider = stringProvider,
        localDataSource = localDataSource,
        connectivityMonitor = connectivityMonitor,
        userSession = userSession,
        popupManager = popupManager,
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

    fun getDiceJackViewModel(
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
        onHome: () -> Unit,
    ) = DiceJackViewModel(
        playerNames = playerNames,
        dieStyles = dieStyles,
        totalRounds = totalRounds,
        diceRoller = diceRoller,
        onHome = onHome,
    )

    fun getStraightRollViewModel(
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
        onHome: () -> Unit,
    ) = StraightRollViewModel(
        playerNames = playerNames,
        dieStyles = dieStyles,
        totalRounds = totalRounds,
        diceRoller = diceRoller,
        onHome = onHome,
    )

    fun getRiskItViewModel(
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
        onHome: () -> Unit,
    ) = RiskItViewModel(
        playerNames = playerNames,
        dieStyles = dieStyles,
        totalRounds = totalRounds,
        diceRoller = diceRoller,
        onHome = onHome,
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
