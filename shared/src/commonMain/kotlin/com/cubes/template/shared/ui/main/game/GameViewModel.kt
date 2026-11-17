package com.cubes.template.shared.ui.main.game

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.GameAction
import com.cubes.template.shared.domain.game.GameEngine
import com.cubes.template.shared.domain.game.RoundPhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Local hot-seat game: every action goes straight through [GameEngine.reduce]. */
class GameViewModel(
    private val playerNames: List<String>,
    private val targetWins: Int,
    private val diceRoller: DiceRoller,
    private val screenNavigator: GameScreenNavigator,
) : ViewModel(), GameViewListener {

    private val _viewData = MutableStateFlow(
        Game.ViewData(state = GameEngine.newGame(playerNames, targetWins), listener = this)
    )
    val viewData: StateFlow<Game.ViewData> = _viewData.asStateFlow()

    private fun dispatch(action: GameAction) {
        _viewData.update { data ->
            val old = data.state
            val new = GameEngine.reduce(old, action, diceRoller)
            if (new === old) return@update data

            val stillPlaying = new.phase == RoundPhase.FirstRoll || new.phase == RoundPhase.SecondRoll
            val playerChanged = new.currentPlayerIndex != old.currentPlayerIndex || new.phase != old.phase
            val passDevice = if (stillPlaying && playerChanged && action != GameAction.NextRound) {
                Game.PassDevice(
                    previousPlayer = old.currentPlayer,
                    previousDice = new.turnOf(old.currentPlayer.id).dice,
                    nextPlayer = new.currentPlayer,
                )
            } else {
                null
            }
            data.copy(state = new, passDevice = passDevice)
        }
    }

    override fun onRollClick() = dispatch(GameAction.RollFirst)
    override fun onDieClick(index: Int) = dispatch(GameAction.ToggleSelect(index))
    override fun onRerollClick() = dispatch(GameAction.RerollSelected)
    override fun onKeepClick() = dispatch(GameAction.Keep)
    override fun onNextRoundClick() = dispatch(GameAction.NextRound)

    override fun onReadyClick() {
        _viewData.update { it.copy(passDevice = null) }
    }

    override fun onNewMatchClick() {
        _viewData.update {
            it.copy(state = GameEngine.newGame(playerNames, targetWins), passDevice = null)
        }
    }

    override fun onHomeClick() = screenNavigator.showHome()
}
