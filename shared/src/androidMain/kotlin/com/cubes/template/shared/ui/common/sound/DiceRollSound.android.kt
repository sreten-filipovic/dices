package com.cubes.template.shared.ui.common.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.cubes.template.shared.log
import com.cubes.template.shared.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun rememberDiceRollSound(): DiceRollSound {
    val context = LocalContext.current.applicationContext
    return remember { AndroidDiceRollSound.also { it.ensureLoaded(context) } }
}

private object AndroidDiceRollSound : DiceRollSound {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadStarted = false
    private var soundPool: SoundPool? = null
    private var soundId = 0

    @Volatile
    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loadStarted) return
        loadStarted = true
        scope.launch {
            try {
                val bytes = Res.readBytes(DICE_ROLL_SOUND_PATH)
                val file = withContext(Dispatchers.IO) {
                    File(context.cacheDir, "dice_roll.wav").apply { writeBytes(bytes) }
                }
                val pool = SoundPool.Builder()
                    .setMaxStreams(DICE_ROLL_SOUND_VOICES)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .build()
                pool.setOnLoadCompleteListener { _, _, status -> loaded = status == 0 }
                soundPool = pool
                soundId = pool.load(file.path, 1)
            } catch (e: Exception) {
                log("DiceRollSound", "Could not load the dice roll sound: $e")
            }
        }
    }

    override fun play() {
        if (!loaded) return
        soundPool?.play(soundId, DICE_ROLL_SOUND_VOLUME, DICE_ROLL_SOUND_VOLUME, 1, 0, randomDiceRollSoundRate())
    }
}
