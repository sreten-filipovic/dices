package com.cubes.template.shared.ui.common.sound

import androidx.compose.runtime.Composable
import kotlin.random.Random

/** Plays the sound of a single die being rolled; calls may overlap when several dice roll at once. */
interface DiceRollSound {
    fun play()
}

/** Process-wide player; the sound is loaded in the background the first time this is called. */
@Composable
expect fun rememberDiceRollSound(): DiceRollSound

internal const val DICE_ROLL_SOUND_PATH = "files/dice_roll.wav"
internal const val DICE_ROLL_SOUND_VOLUME = 0.8f

/** How many dice can be heard at the same time. */
internal const val DICE_ROLL_SOUND_VOICES = 8

/** Slightly different playback speed for every die, so a handful of dice does not sound like one. */
internal fun randomDiceRollSoundRate(): Float = 0.94f + Random.nextFloat() * 0.12f
