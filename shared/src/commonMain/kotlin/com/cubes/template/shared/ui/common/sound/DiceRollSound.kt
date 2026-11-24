package com.cubes.template.shared.ui.common.sound

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.random.Random

interface SoundEffect {
    fun play(rate: Float = 1f)
}

@Composable
expect fun rememberSoundEffect(path: String, voices: Int, volume: Float): SoundEffect

interface DiceRollSound {
    fun play()
}

@Composable
fun rememberDiceRollSound(): DiceRollSound {
    val effect = rememberSoundEffect(DICE_ROLL_SOUND_PATH, DICE_ROLL_SOUND_VOICES, DICE_ROLL_SOUND_VOLUME)
    return remember(effect) {
        object : DiceRollSound {
            override fun play() = effect.play(randomDiceRollSoundRate())
        }
    }
}

internal const val DICE_ROLL_SOUND_PATH = "files/dice_roll.wav"
internal const val DICE_ROLL_SOUND_VOLUME = 0.8f

internal const val DICE_ROLL_SOUND_VOICES = 8

internal fun randomDiceRollSoundRate(): Float = 0.94f + Random.nextFloat() * 0.12f
