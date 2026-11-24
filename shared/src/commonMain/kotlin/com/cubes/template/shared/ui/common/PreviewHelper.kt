package com.cubes.template.shared.ui.common

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.GameAction
import com.cubes.template.shared.domain.game.GameEngine
import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.domain.duel.DuelState
import com.cubes.template.shared.domain.jack.DiceJack
import com.cubes.template.shared.domain.jack.JackState
import com.cubes.template.shared.domain.risk.RiskIt
import com.cubes.template.shared.domain.risk.RiskState
import com.cubes.template.shared.domain.seven.Sevens
import com.cubes.template.shared.domain.straight.StraightRoll
import com.cubes.template.shared.domain.straight.StraightState
import com.cubes.template.shared.domain.seven.SevenState
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.main.risk_it.RiskItUi
import com.cubes.template.shared.ui.main.risk_it.RiskItViewListener
import com.cubes.template.shared.ui.main.sevens.SevensUi
import com.cubes.template.shared.ui.main.sevens.SevensViewListener
import com.cubes.template.shared.ui.main.straight_roll.StraightRollUi
import com.cubes.template.shared.ui.main.straight_roll.StraightRollViewListener
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelUi
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelViewListener
import com.cubes.template.shared.ui.main.dice_jack.DiceJackUi
import com.cubes.template.shared.ui.main.dice_jack.DiceJackViewListener
import com.cubes.template.shared.ui.main.dice_roll.DiceRoll
import com.cubes.template.shared.ui.main.dice_roll.DiceRollViewListener
import com.cubes.template.shared.ui.main.game.Game
import com.cubes.template.shared.ui.main.game.GameViewListener
import com.cubes.template.shared.ui.main.game_setup.GameSetup
import com.cubes.template.shared.ui.main.game_setup.GameSetupViewListener
import com.cubes.template.shared.ui.main.home.Home
import com.cubes.template.shared.ui.main.home.HomeViewListener

object PreviewHelper {

    fun home() = Home.ViewData(listener = object : HomeViewListener {
        override fun onGameClick(game: Home.GameCard) = Unit
    })

    fun diceRoll(value: Int?) = DiceRoll.ViewData(value = value, listener = object : DiceRollViewListener {
        override fun onRollClick() = Unit
        override fun onBackClick() = Unit
    })

    fun diceDuelTurn() = diceDuel(
        DuelState(players = listOf("Ana", "Marko"), diceCount = 2, hands = listOf(listOf(4, 5), null)),
    )

    fun diceDuelDialog() = diceDuelTurn().copy(dialogOpen = true)

    fun diceDuelResult() = diceDuel(
        DuelState(
            players = listOf("Ana", "Marko"),
            diceCount = 2,
            hands = listOf(listOf(4, 5), listOf(6, 6)),
            wins = listOf(1, 2),
        ),
    )

    fun diceDuelRanking() = diceDuel(
        DuelState(
            players = listOf("Ana", "Marko", "Jovan", "Mila"),
            diceCount = 3,
            hands = listOf(listOf(4, 5, 1), listOf(6, 6, 2), listOf(1, 3, 3), listOf(2, 5, 3)),
            wins = listOf(1, 2, 0, 0),
        ),
    )

    private fun diceDuel(state: DuelState) = DiceDuelUi.ViewData(
        state = state,
        listener = object : DiceDuelViewListener {
            override fun onRollClick() = Unit
            override fun onDoneClick() = Unit
            override fun onTiebreakClick() = Unit
            override fun onNextDuelClick() = Unit
            override fun onBackClick() = Unit
        },
    )

    fun diceJackTurn() = diceJack(
        sampleJack()
            .let { DiceJack.roll(it, fixedRoller(6, 5, 4)) }
            .let { DiceJack.roll(it, fixedRoller(3)) }
            .let(DiceJack::stand)
    )

    fun diceJackDialog() = diceJack(
        state = sampleJack()
            .let { DiceJack.roll(it, fixedRoller(6, 5, 4)) }
            .let(DiceJack::stand)
            .let { DiceJack.roll(it, fixedRoller(2, 5, 3)) }
            .let { DiceJack.roll(it, fixedRoller(4)) },
        dialogOpen = true,
    )

    fun diceJackMatchOver() = diceJack(
        sampleJack()
            .let { DiceJack.roll(it, fixedRoller(4, 4, 4)) }
            .let(DiceJack::stand)
            .let { DiceJack.roll(it, fixedRoller(6, 6, 5)) }
            .let { DiceJack.roll(it, fixedRoller(6)) }
            .let(DiceJack::stand)
    )

    private fun sampleJack() = DiceJack.newGame(listOf("Ana", "Marko"), totalRounds = 1)

    private fun diceJack(state: JackState, dialogOpen: Boolean = false) = DiceJackUi.ViewData(
        state = state,
        dieStyles = listOf(DieStyle.WHITE, DieStyle.BLACK),
        dialogOpen = dialogOpen,
        tablePlayer = state.currentPlayer,
        tableTurn = state.turn,
        listener = object : DiceJackViewListener {
            override fun onRollClick() = Unit
            override fun onStandClick() = Unit
            override fun onNextRoundClick() = Unit
            override fun onNewMatchClick() = Unit
            override fun onHomeClick() = Unit
        },
    )

    fun straightRollTurn() = straightRoll(
        sampleStraight()
            .let { StraightRoll.roll(it, fixedRoller(1, 2, 3, 4, 5, 6)) }
            .let(StraightRoll::finish)
    )

    fun straightRollDialog() = straightRoll(
        state = sampleStraight()
            .let { StraightRoll.roll(it, fixedRoller(1, 2, 3, 4, 5, 6)) }
            .let(StraightRoll::finish)
            .let { StraightRoll.roll(it, fixedRoller(1, 1, 3, 4, 4, 6)) },
        dialogOpen = true,
    )

    fun straightRollMatchOver() = straightRoll(
        sampleStraight()
            .let { StraightRoll.roll(it, fixedRoller(1, 2, 3, 4, 5, 6)) }
            .let(StraightRoll::finish)
            .let { StraightRoll.roll(it, fixedRoller(1, 1, 3, 4, 5, 6)) }
            .let { StraightRoll.roll(it, fixedRoller(2)) }
            .let(StraightRoll::finish)
    )

    private fun sampleStraight() = StraightRoll.newGame(listOf("Ana", "Marko"), totalRounds = 1)

    private fun straightRoll(state: StraightState, dialogOpen: Boolean = false) = StraightRollUi.ViewData(
        state = state,
        dialogOpen = dialogOpen,
        tablePlayer = state.currentPlayer,
        tableTurn = state.turn,
        onTable = state.turn.remaining,
        collected = state.turn.keptValues,
        listener = object : StraightRollViewListener {
            override fun onRollClick() = Unit
            override fun onDoneClick() = Unit
            override fun onNextRoundClick() = Unit
            override fun onNewMatchClick() = Unit
            override fun onHomeClick() = Unit
        },
    )

    fun riskItTurn() = riskIt(
        sampleRisk()
            .let { RiskIt.roll(it, fixedRoller(5)) }
            .let { RiskIt.roll(it, fixedRoller(1)) }
            .let { RiskIt.roll(it, fixedRoller(6)) }
            .let(RiskIt::stop)
    )

    fun riskItDialog() = riskIt(
        state = sampleRisk()
            .let { RiskIt.roll(it, fixedRoller(5)) }
            .let(RiskIt::stop)
            .let { RiskIt.roll(it, fixedRoller(4)) }
            .let { RiskIt.roll(it, fixedRoller(1)) }
            .let { RiskIt.roll(it, fixedRoller(6)) }
            .let { RiskIt.roll(it, fixedRoller(3)) },
        dialogOpen = true,
    )

    fun riskItMatchOver() = riskIt(
        sampleRisk()
            .let { RiskIt.roll(it, fixedRoller(5)) }
            .let { RiskIt.roll(it, fixedRoller(6)) }
            .let(RiskIt::stop)
            .let { RiskIt.roll(it, fixedRoller(4)) }
            .let(RiskIt::stop)
    )

    private fun sampleRisk() = RiskIt.newGame(listOf("Ana", "Marko"), totalRounds = 1)

    private fun riskIt(state: RiskState, dialogOpen: Boolean = false) = RiskItUi.ViewData(
        state = state,
        dialogOpen = dialogOpen,
        tablePlayer = state.currentPlayer,
        tableTurn = state.turn,
        listener = object : RiskItViewListener {
            override fun onRollClick() = Unit
            override fun onStopClick() = Unit
            override fun onNextRoundClick() = Unit
            override fun onNewMatchClick() = Unit
            override fun onHomeClick() = Unit
        },
    )

    fun sevensTurn() = sevens(
        sampleSeven()
            .let { Sevens.roll(it, fixedRoller(6, 1, 5, 5, 3, 2)) }
            .let(Sevens::bank)
    )

    fun sevensDialog() = sevens(
        state = sampleSeven()
            .let { Sevens.roll(it, fixedRoller(6, 1, 5, 5, 3, 2)) }
            .let(Sevens::bank)
            .let { Sevens.roll(it, fixedRoller(4, 3, 6, 6, 2, 1)) },
        dialogOpen = true,
    )

    fun sevensMatchOver() = sevens(
        sampleSeven()
            .let { Sevens.roll(it, fixedRoller(6, 1, 5, 5, 3, 2)) }
            .let(Sevens::bank)
            .let { Sevens.roll(it, fixedRoller(4, 3, 6, 6, 2, 1)) }
            .let(Sevens::bank)
    )

    private fun sampleSeven() = Sevens.newGame(listOf("Ana", "Marko"), totalRounds = 1)

    private fun sevens(state: SevenState, dialogOpen: Boolean = false) = SevensUi.ViewData(
        state = state,
        dialogOpen = dialogOpen,
        tablePlayer = state.currentPlayer,
        tableTurn = state.turn,
        tableRollsLeft = state.rollLimit - state.turn.rolls,
        pairsShown = state.turn.pairs,
        listener = object : SevensViewListener {
            override fun onRollClick() = Unit
            override fun onBankClick() = Unit
            override fun onNextRoundClick() = Unit
            override fun onNewMatchClick() = Unit
            override fun onHomeClick() = Unit
        },
    )

    fun gameSetup() = GameSetup.ViewData(
        names = listOf("Ana", "Marko", ""),
        dieStyles = listOf(DieStyle.WHITE, DieStyle.BLACK, DieStyle.DARK_GRAY),
        listener = object : GameSetupViewListener {
            override fun onNameChanged(index: Int, name: String) = Unit
            override fun onAddPlayerClick() = Unit
            override fun onRemovePlayerClick(index: Int) = Unit
            override fun onSettingsClick(index: Int) = Unit
            override fun onDieStyleSelected(style: DieStyle) = Unit
            override fun onPawnColorSelected(color: PawnColor) = Unit
            override fun onSettingsDismiss() = Unit
            override fun onTotalRoundsChanged(totalRounds: Int) = Unit
            override fun onDiceCountChanged(diceCount: Int) = Unit
            override fun onStartClick() = Unit
            override fun onBackClick() = Unit
            override fun onModeSelected(mode: GameSetup.Mode) = Unit
        },
    )

    fun gameTurn() = game(
        sampleGame()
            .roll(5, 5, 2, 3, 6).keep()
    )

    fun gameDialogFirstRoll() = game(
        state = sampleGame()
            .roll(5, 5, 2, 3, 6).keep()
            .roll(4, 4, 4, 1, 2)
            .let { GameEngine.reduce(it, GameAction.ToggleSelect(3), fixedRoller()) },
        rollDialog = Game.RollDialog(playerId = "p2", stage = Game.Stage.FIRST_ROLL, animating = false),
    )

    fun gameRoundOver() = game(
        sampleGame()
            .roll(5, 5, 2, 3, 6).keep()
            .roll(4, 4, 4, 1, 2).keep()
            .roll(1, 2, 3, 4, 6).keep()
    )

    private fun sampleGame() = GameEngine.newGame(listOf("Ana", "Marko", "Jovan"), totalRounds = 3)

    private fun GameState.roll(vararg dice: Int) = GameEngine.reduce(this, GameAction.RollFirst, fixedRoller(*dice))

    private fun GameState.keep() = GameEngine.reduce(this, GameAction.Keep, fixedRoller())

    private fun fixedRoller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirstOrNull() ?: 1 }
    }

    private fun game(state: GameState, rollDialog: Game.RollDialog? = null) =
        Game.ViewData(state = state, rollDialog = rollDialog, listener = noOpGameListener)

    private val noOpGameListener = object : GameViewListener {
        override fun onRollClick() = Unit
        override fun onDieClick(index: Int) = Unit
        override fun onRerollClick() = Unit
        override fun onKeepClick() = Unit
        override fun onDoneClick() = Unit
        override fun onNextRoundClick() = Unit
        override fun onNewMatchClick() = Unit
        override fun onHomeClick() = Unit
    }
}
