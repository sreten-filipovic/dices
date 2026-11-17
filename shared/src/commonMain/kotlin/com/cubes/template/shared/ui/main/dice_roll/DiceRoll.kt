package com.cubes.template.shared.ui.main.dice_roll

/** Phase 1 prototype: roll a single die and show the result. */
object DiceRoll {
    data class ViewData(
        val value: Int? = null,
        val listener: DiceRollViewListener,
    )
}

interface DiceRollViewListener {
    fun onRollClick()
    fun onBackClick()
}
