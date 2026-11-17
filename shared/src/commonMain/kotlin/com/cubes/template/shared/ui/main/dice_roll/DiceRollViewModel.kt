package com.cubes.template.shared.ui.main.dice_roll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.ui.common.views.DieAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiceRollViewModel(
    private val diceRoller: DiceRoller,
    private val onBack: () -> Unit,
) : ViewModel(), DiceRollViewListener {

    private val _viewData = MutableStateFlow(DiceRoll.ViewData(listener = this))
    val viewData: StateFlow<DiceRoll.ViewData> = _viewData.asStateFlow()

    override fun onRollClick() {
        if (_viewData.value.rolling) return
        _viewData.update { it.copy(value = diceRoller.roll(), rollKey = it.rollKey + 1, rolling = true) }
        viewModelScope.launch {
            delay(DieAnimation.totalMillis(diceCount = 1))
            _viewData.update { it.copy(rolling = false) }
        }
    }

    override fun onBackClick() = onBack()
}
