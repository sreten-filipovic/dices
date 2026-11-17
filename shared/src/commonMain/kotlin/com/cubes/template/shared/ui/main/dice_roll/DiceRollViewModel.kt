package com.cubes.template.shared.ui.main.dice_roll

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.domain.game.DiceRoller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DiceRollViewModel(
    private val diceRoller: DiceRoller,
    private val onBack: () -> Unit,
) : ViewModel(), DiceRollViewListener {

    private val _viewData = MutableStateFlow(DiceRoll.ViewData(listener = this))
    val viewData: StateFlow<DiceRoll.ViewData> = _viewData.asStateFlow()

    override fun onRollClick() {
        _viewData.update { it.copy(value = diceRoller.roll()) }
    }

    override fun onBackClick() = onBack()
}
