package com.cubes.template.shared.ui.common

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.GameAction
import com.cubes.template.shared.domain.game.GameEngine
import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.domain.duel.DuelState
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelUi
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelViewListener
import com.cubes.template.shared.ui.main.dice_roll.DiceRoll
import com.cubes.template.shared.ui.main.dice_roll.DiceRollViewListener
import com.cubes.template.shared.ui.main.game.Game
import com.cubes.template.shared.ui.main.game.GameViewListener
import com.cubes.template.shared.ui.main.game_setup.GameSetup
import com.cubes.template.shared.ui.main.game_setup.GameSetupViewListener
import com.cubes.template.shared.ui.main.home.Home
import com.cubes.template.shared.ui.main.home.HomeViewListener

/**
 * Fake data + no-op listeners for @Preview functions. Keep one builder per screen state.
 */
object PreviewHelper {

    fun home() = Home.ViewData(listener = object : HomeViewListener {
        override fun onGameClick(game: Home.GameCard) = Unit
    })

    fun diceRoll(value: Int?) = DiceRoll.ViewData(value = value, listener = object : DiceRollViewListener {
        override fun onRollClick() = Unit
        override fun onBackClick() = Unit
    })

    fun diceDuelTurn() = diceDuel(DuelState(diceCount = 2, hands = listOf(listOf(4, 5), null)))

    fun diceDuelResult() = diceDuel(
        DuelState(diceCount = 2, hands = listOf(listOf(4, 5), listOf(6, 6)), wins = listOf(1, 2)),
    )

    private fun diceDuel(state: DuelState) = DiceDuelUi.ViewData(
        state = state,
        wins = state.wins,
        listener = object : DiceDuelViewListener {
            override fun onDiceCountChanged(diceCount: Int) = Unit
            override fun onRollClick() = Unit
            override fun onNextDuelClick() = Unit
            override fun onBackClick() = Unit
        },
    )

    fun gameSetup() = GameSetup.ViewData(
        names = listOf("Ana", "Marko", ""),
        listener = object : GameSetupViewListener {
            override fun onNameChanged(index: Int, name: String) = Unit
            override fun onAddPlayerClick() = Unit
            override fun onRemovePlayerClick(index: Int) = Unit
            override fun onTotalRoundsChanged(totalRounds: Int) = Unit
            override fun onStartClick() = Unit
            override fun onBackClick() = Unit
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
