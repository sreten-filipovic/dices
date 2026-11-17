package com.cubes.template.shared.ui.common

import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.GameAction
import com.cubes.template.shared.domain.game.GameEngine
import com.cubes.template.shared.domain.game.GameState
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
        override fun onNewGameClick() = Unit
        override fun onTryDieClick() = Unit
    })

    fun diceRoll(value: Int?) = DiceRoll.ViewData(value = value, listener = object : DiceRollViewListener {
        override fun onRollClick() = Unit
        override fun onBackClick() = Unit
    })

    fun gameSetup() = GameSetup.ViewData(
        names = listOf("Ana", "Marko", ""),
        listener = object : GameSetupViewListener {
            override fun onNameChanged(index: Int, name: String) = Unit
            override fun onAddPlayerClick() = Unit
            override fun onRemovePlayerClick(index: Int) = Unit
            override fun onTargetWinsChanged(targetWins: Int) = Unit
            override fun onStartClick() = Unit
            override fun onBackClick() = Unit
        },
    )

    fun gameSecondRoll() = game(
        sampleGame()
            .roll(5, 5, 2, 3, 6)
            .roll(4, 4, 4, 1, 2)
            .roll(1, 2, 3, 4, 6)
            .let { GameEngine.reduce(it, GameAction.ToggleSelect(2), fixedRoller()) }
    )

    fun gameRoundOver() = game(
        sampleGame()
            .roll(5, 5, 2, 3, 6)
            .roll(4, 4, 4, 1, 2)
            .roll(1, 2, 3, 4, 6)
            .keep().keep().keep()
    )

    private fun sampleGame() = GameEngine.newGame(listOf("Ana", "Marko", "Jovan"), targetWins = 3)

    private fun GameState.roll(vararg dice: Int) = GameEngine.reduce(this, GameAction.RollFirst, fixedRoller(*dice))

    private fun GameState.keep() = GameEngine.reduce(this, GameAction.Keep, fixedRoller())

    private fun fixedRoller(vararg values: Int): DiceRoller {
        val queue = ArrayDeque(values.toList())
        return DiceRoller { queue.removeFirstOrNull() ?: 1 }
    }

    private fun game(state: GameState) = Game.ViewData(state = state, listener = noOpGameListener)

    private val noOpGameListener = object : GameViewListener {
        override fun onRollClick() = Unit
        override fun onDieClick(index: Int) = Unit
        override fun onRerollClick() = Unit
        override fun onKeepClick() = Unit
        override fun onReadyClick() = Unit
        override fun onNextRoundClick() = Unit
        override fun onNewMatchClick() = Unit
        override fun onHomeClick() = Unit
    }
}
