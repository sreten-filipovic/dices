package com.cubes.template.shared.ui.common.sound

import androidx.compose.runtime.Composable
import kotlin.random.Random

interface DiceRollSound {
    fun play()
}

@Composable
expect fun rememberDiceRollSound(): DiceRollSound

internal const val DICE_ROLL_SOUND_PATH = "files/dice_roll.wav"
internal const val DICE_ROLL_SOUND_VOLUME = 0.8f

internal const val DICE_ROLL_SOUND_VOICES = 8

internal fun randomDiceRollSoundRate(): Float = 0.94f + Random.nextFloat() * 0.12f
