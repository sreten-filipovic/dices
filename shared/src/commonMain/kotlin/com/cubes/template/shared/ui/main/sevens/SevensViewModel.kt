package com.cubes.template.shared.ui.main.sevens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.seven.Sevens
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SevensViewModel(
    private val playerNames: List<String>,
    dieStyles: List<DieStyle>,
    private val totalRounds: Int,
    private val diceRoller: DiceRoller,
    private val onHome: () -> Unit,
) : ViewModel(), SevensViewListener {

    private val _viewData = MutableStateFlow(
        SevensUi.ViewData(
            state = Sevens.newGame(playerNames, totalRounds),
            dieStyles = dieStyles,
            listener = this,
        )
    )
    val viewData: StateFlow<SevensUi.ViewData> = _viewData.asStateFlow()

    private var animationJob: Job? = null

    override fun onRollClick() {
        val data = _viewData.value
        if (data.rolling) return
        val old = data.state
        val new = Sevens.roll(old, diceRoller)
        if (new === old) return

        val rolled = if (old.turn.hasRolled) old.turn.active.toSet() else (0 until Sevens.DICE_COUNT).toSet()
        _viewData.value = data.copy(
            state = new,
            dialogOpen = true,
            tablePlayer = old.currentPlayer,
            tableTurn = new.turn,
            tableRollsLeft = new.rollLimit - new.turn.rolls,
            pairsShown = if (old.turn.hasRolled) data.pairsShown else emptyList(),
            rollTicks = data.rollTicks.mapIndexed { index, tick -> if (index in rolled) tick + 1 else tick },
            rolling = true,
        )
        animationJob = viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = Sevens.DICE_COUNT))
            _viewData.update { it.copy(rolling = false, pairsShown = new.turn.pairs) }
        }
    }

    override fun onBankClick() {
        if (_viewData.value.rolling) return
        _viewData.update { it.copy(state = Sevens.bank(it.state), dialogOpen = false) }
    }

    override fun onNextRoundClick() {
        _viewData.update { it.copy(state = Sevens.nextRound(it.state)) }
    }

    override fun onNewMatchClick() {
        animationJob?.cancel()
        _viewData.update {
            it.copy(state = Sevens.newGame(playerNames, totalRounds), dialogOpen = false, rolling = false)
        }
    }

    override fun onHomeClick() = onHome()
}
