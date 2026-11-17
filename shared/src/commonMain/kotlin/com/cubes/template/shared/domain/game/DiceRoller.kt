package com.cubes.template.shared.domain.game

import kotlin.random.Random

/** Source of die values (1..6). Injected so tests and a future host/server can control rolls. */
fun interface DiceRoller {
    fun roll(): Int
}

class RandomDiceRoller(private val random: Random = Random.Default) : DiceRoller {
    override fun roll(): Int = random.nextInt(1, 7)
}
