package com.cubes.template.shared.ui.main.dice_jack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.jack.DiceJack
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiceJackViewModel(
    private val playerNames: List<String>,
    dieStyles: List<DieStyle>,
    private val totalRounds: Int,
    private val diceRoller: DiceRoller,
    private val onHome: () -> Unit,
) : ViewModel(), DiceJackViewListener {

    private val _viewData = MutableStateFlow(
        DiceJackUi.ViewData(
            state = DiceJack.newGame(playerNames, totalRounds),
            dieStyles = dieStyles,
            listener = this,
        )
    )
    val viewData: StateFlow<DiceJackUi.ViewData> = _viewData.asStateFlow()

    private var animationJob: Job? = null

    override fun onRollClick() {
        val data = _viewData.value
        if (data.rolling) return
        val old = data.state
        val new = DiceJack.roll(old, diceRoller)
        if (new === old) return

        val rolled = old.turn.dice.size until new.turn.dice.size
        _viewData.value = data.copy(
            state = new,
            dialogOpen = true,
            tablePlayer = old.currentPlayer,
            tableTurn = new.turn,
            rollTicks = data.rollTicks.mapIndexed { index, tick -> if (index in rolled) tick + 1 else tick },
            rolling = true,
        )
        animationJob = viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = rolled.count()))
            _viewData.update { it.copy(rolling = false) }
        }
    }

    override fun onStandClick() {
        if (_viewData.value.rolling) return
        _viewData.update { it.copy(state = DiceJack.stand(it.state), dialogOpen = false) }
    }

    override fun onNextRoundClick() {
        _viewData.update { it.copy(state = DiceJack.nextRound(it.state)) }
    }

    override fun onNewMatchClick() {
        animationJob?.cancel()
        _viewData.update {
            it.copy(state = DiceJack.newGame(playerNames, totalRounds), dialogOpen = false, rolling = false)
        }
    }

    override fun onHomeClick() = onHome()
}
