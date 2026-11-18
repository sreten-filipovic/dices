package com.cubes.template.shared.domain.game

import kotlin.random.Random

fun interface DiceRoller {
    fun roll(): Int
}

class RandomDiceRoller(private val random: Random = Random.Default) : DiceRoller {
    override fun roll(): Int = random.nextInt(1, 7)
}
