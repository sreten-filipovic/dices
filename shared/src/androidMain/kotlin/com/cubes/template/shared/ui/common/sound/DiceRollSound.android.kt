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
actual fun rememberSoundEffect(path: String, voices: Int, volume: Float): SoundEffect {
    val context = LocalContext.current.applicationContext
    return remember(path) { AndroidSoundEffects.get(context, path, voices, volume) }
}

private object AndroidSoundEffects {
    private val effects = mutableMapOf<String, AndroidSoundEffect>()

    fun get(context: Context, path: String, voices: Int, volume: Float): AndroidSoundEffect =
        effects.getOrPut(path) { AndroidSoundEffect(path, voices, volume).also { it.load(context) } }
}

private class AndroidSoundEffect(
    private val path: String,
    private val voices: Int,
    private val volume: Float,
) : SoundEffect {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var soundPool: SoundPool? = null
    private var soundId = 0

    @Volatile
    private var loaded = false

    fun load(context: Context) {
        scope.launch {
            try {
                val bytes = Res.readBytes(path)
                val file = withContext(Dispatchers.IO) {
                    File(context.cacheDir, path.substringAfterLast('/')).apply { writeBytes(bytes) }
                }
                val pool = SoundPool.Builder()
                    .setMaxStreams(voices)
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
                log("SoundEffect", "Could not load $path: $e")
            }
        }
    }

    override fun play(rate: Float) {
        if (!loaded) return
        soundPool?.play(soundId, volume, volume, 1, 0, rate)
    }
}
