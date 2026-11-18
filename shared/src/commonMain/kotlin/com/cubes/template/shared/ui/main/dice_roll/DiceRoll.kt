package com.cubes.template.shared.ui.main.dice_roll

object DiceRoll {
    data class ViewData(
        val value: Int? = null,
        val rollKey: Int = 0,
        val rolling: Boolean = false,
        val listener: DiceRollViewListener,
    )
}

interface DiceRollViewListener {
    fun onRollClick()
    fun onBackClick()
}
