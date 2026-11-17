package com.cubes.template.shared.ui.main.dice_duel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.duel.DiceDuel
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.ui.common.views.DieAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiceDuelViewModel(
    private val diceRoller: DiceRoller,
    private val onBack: () -> Unit,
) : ViewModel(), DiceDuelViewListener {

    private val _viewData = MutableStateFlow(DiceDuelUi.ViewData(listener = this))
    val viewData: StateFlow<DiceDuelUi.ViewData> = _viewData.asStateFlow()

    override fun onDiceCountChanged(diceCount: Int) {
        if (_viewData.value.rolling) return
        _viewData.update { it.copy(state = DiceDuel.setDiceCount(it.state, diceCount)) }
    }

    override fun onRollClick() {
        val current = _viewData.value
        if (current.rolling) return
        val player = current.state.currentPlayer ?: return
        val rolled = DiceDuel.roll(current.state, diceRoller)
        _viewData.update {
            it.copy(
                state = rolled,
                rollKeys = it.rollKeys.mapIndexed { index, key -> if (index == player) key + 1 else key },
                rollingPlayer = player,
            )
        }
        viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = rolled.diceCount))
            _viewData.update { it.copy(rollingPlayer = null, wins = rolled.wins) }
        }
    }

    override fun onNextDuelClick() {
        if (_viewData.value.rolling) return
        _viewData.update { it.copy(state = DiceDuel.nextDuel(it.state)) }
    }

    override fun onBackClick() = onBack()
}
