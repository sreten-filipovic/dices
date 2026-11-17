package com.cubes.template.shared.ui.main.dice_roll

/** Phase 1 prototype: roll a single die and show the result. */
object DiceRoll {
    data class ViewData(
        val value: Int? = null,
        /** Grows on every roll; drives the die animation. */
        val rollKey: Int = 0,
        /** True while the die is tumbling; the result is revealed afterwards. */
        val rolling: Boolean = false,
        val listener: DiceRollViewListener,
    )
}

interface DiceRollViewListener {
    fun onRollClick()
    fun onBackClick()
}
