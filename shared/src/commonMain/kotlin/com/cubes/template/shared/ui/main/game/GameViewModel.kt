package com.cubes.template.shared.ui.main.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.GameAction
import com.cubes.template.shared.domain.game.GameEngine
import com.cubes.template.shared.domain.game.HandEvaluator
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val playerNames: List<String>,
    dieStyles: List<DieStyle>,
    private val totalRounds: Int,
    private val diceRoller: DiceRoller,
    private val screenNavigator: GameScreenNavigator,
) : ViewModel(), GameViewListener {

    private val _viewData = MutableStateFlow(
        Game.ViewData(
            state = GameEngine.newGame(playerNames, totalRounds),
            dieStyles = dieStyles,
            listener = this,
        )
    )
    val viewData: StateFlow<Game.ViewData> = _viewData.asStateFlow()

    private var animationJob: Job? = null

    private val isAnimating: Boolean get() = _viewData.value.rollDialog?.animating == true

    override fun onRollClick() {
        if (isAnimating) return
        roll(GameAction.RollFirst, Game.Stage.FIRST_ROLL)
    }

    override fun onRerollClick() {
        if (isAnimating) return
        roll(GameAction.RerollSelected, Game.Stage.SECOND_ROLL)
    }

    override fun onDieClick(index: Int) {
        val dialog = _viewData.value.rollDialog ?: return
        if (dialog.animating || dialog.stage != Game.Stage.FIRST_ROLL) return
        _viewData.update { it.copy(state = GameEngine.reduce(it.state, GameAction.ToggleSelect(index), diceRoller)) }
    }

    override fun onKeepClick() {
        if (isAnimating) return
        _viewData.update { it.copy(state = GameEngine.reduce(it.state, GameAction.Keep, diceRoller), rollDialog = null) }
    }

    override fun onDoneClick() {
        if (isAnimating) return
        _viewData.update { it.copy(rollDialog = null) }
    }

    override fun onNextRoundClick() {
        _viewData.update { it.copy(state = GameEngine.reduce(it.state, GameAction.NextRound, diceRoller)) }
    }

    override fun onNewMatchClick() {
        animationJob?.cancel()
        _viewData.update { it.copy(state = GameEngine.newGame(playerNames, totalRounds), rollDialog = null) }
    }

    override fun onHomeClick() = screenNavigator.showHome()

    private fun roll(action: GameAction, stage: Game.Stage) {
        val data = _viewData.value
        val old = data.state
        val new = GameEngine.reduce(old, action, diceRoller)
        if (new === old) return

        val rollerId = old.currentPlayer.id
        val rolled = when (action) {
            GameAction.RerollSelected -> old.currentTurn.selected
            else -> (0 until HandEvaluator.DICE_COUNT).toSet()
        }
        val ticks = data.rollTicksOf(rollerId).mapIndexed { index, tick -> if (index in rolled) tick + 1 else tick }

        _viewData.value = data.copy(
            state = new,
            rollTicks = data.rollTicks + (rollerId to ticks),
            rollDialog = Game.RollDialog(playerId = rollerId, stage = stage, animating = true),
        )
        animationJob = viewModelScope.launch {
            delay(DieAnimation.totalMillis())
            _viewData.update { it.copy(rollDialog = it.rollDialog?.copy(animating = false)) }
        }
    }
}
