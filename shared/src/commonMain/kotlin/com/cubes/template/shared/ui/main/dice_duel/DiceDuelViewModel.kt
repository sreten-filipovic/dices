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
    playerNames: List<String>,
    diceCount: Int,
    private val diceRoller: DiceRoller,
    private val onBack: () -> Unit,
) : ViewModel(), DiceDuelViewListener {

    private val _viewData = MutableStateFlow(
        DiceDuelUi.ViewData(state = DiceDuel.newGame(playerNames, diceCount), listener = this)
    )
    val viewData: StateFlow<DiceDuelUi.ViewData> = _viewData.asStateFlow()

    /** Rolls for the current player and opens the dice table with their dice tumbling. */
    override fun onRollClick() {
        val current = _viewData.value
        if (current.dialogOpen) return
        val player = current.state.currentPlayer ?: return
        val rolled = DiceDuel.roll(current.state, diceRoller)
        _viewData.update { it.copy(state = rolled, dialogOpen = true, tablePlayer = player, rolling = true) }
        viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = rolled.diceCount))
            _viewData.update { it.copy(rolling = false) }
        }
    }

    override fun onDoneClick() {
        if (_viewData.value.rolling) return
        _viewData.update { it.copy(dialogOpen = false) }
    }

    override fun onTiebreakClick() {
        if (_viewData.value.dialogOpen) return
        _viewData.update { it.copy(state = DiceDuel.tiebreak(it.state)) }
    }

    override fun onNextDuelClick() {
        if (_viewData.value.dialogOpen) return
        _viewData.update { it.copy(state = DiceDuel.nextDuel(it.state)) }
    }

    override fun onBackClick() = onBack()
}
