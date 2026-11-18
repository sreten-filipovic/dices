package com.cubes.template.shared.ui.main.risk_it

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.risk.RiskIt
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RiskItViewModel(
    private val playerNames: List<String>,
    dieStyles: List<DieStyle>,
    private val totalRounds: Int,
    private val diceRoller: DiceRoller,
    private val onHome: () -> Unit,
) : ViewModel(), RiskItViewListener {

    private val _viewData = MutableStateFlow(
        RiskItUi.ViewData(
            state = RiskIt.newGame(playerNames, totalRounds),
            dieStyles = dieStyles,
            listener = this,
        )
    )
    val viewData: StateFlow<RiskItUi.ViewData> = _viewData.asStateFlow()

    private var animationJob: Job? = null

    override fun onRollClick() {
        val data = _viewData.value
        if (data.rolling) return
        val old = data.state
        val new = RiskIt.roll(old, diceRoller)
        if (new === old) return

        _viewData.value = data.copy(
            state = new,
            dialogOpen = true,
            tablePlayer = old.currentPlayer,
            tableTurn = new.turn,
            rollTick = data.rollTick + 1,
            rolling = true,
        )
        animationJob = viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = 1))
            _viewData.update { it.copy(rolling = false) }
        }
    }

    override fun onStopClick() {
        if (_viewData.value.rolling) return
        _viewData.update { it.copy(state = RiskIt.stop(it.state), dialogOpen = false) }
    }

    override fun onNextRoundClick() {
        _viewData.update { it.copy(state = RiskIt.nextRound(it.state)) }
    }

    override fun onNewMatchClick() {
        animationJob?.cancel()
        _viewData.update {
            it.copy(state = RiskIt.newGame(playerNames, totalRounds), dialogOpen = false, rolling = false)
        }
    }

    override fun onHomeClick() = onHome()
}
