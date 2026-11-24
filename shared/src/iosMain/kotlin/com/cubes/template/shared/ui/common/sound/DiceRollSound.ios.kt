package com.cubes.template.shared.ui.common.sound

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cubes.template.shared.log
import com.cubes.template.shared.resources.Res
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.Foundation.NSData
import platform.Foundation.create

@Composable
actual fun rememberSoundEffect(path: String, voices: Int, volume: Float): SoundEffect =
    remember(path) { IosSoundEffects.get(path, voices, volume) }

private object IosSoundEffects {
    private val effects = mutableMapOf<String, IosSoundEffect>()

    fun get(path: String, voices: Int, volume: Float): IosSoundEffect =
        effects.getOrPut(path) { IosSoundEffect(path, voices, volume).also { it.load() } }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosSoundEffect(
    private val path: String,
    private val voices: Int,
    private val volume: Float,
) : SoundEffect {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var players: List<AVAudioPlayer> = emptyList()
    private var nextPlayer = 0

    fun load() {
        scope.launch {
            try {
                val bytes = Res.readBytes(path)
                val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
                AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, error = null)
                players = List(voices) {
                    AVAudioPlayer(data = data, error = null).apply {
                        enableRate = true
                        volume = this@IosSoundEffect.volume
                        prepareToPlay()
                    }
                }
            } catch (e: Exception) {
                log("SoundEffect", "Could not load $path: $e")
            }
        }
    }

    override fun play(rate: Float) {
        if (players.isEmpty()) return
        val player = players[nextPlayer]
        nextPlayer = (nextPlayer + 1) % players.size
        player.stop()
        player.currentTime = 0.0
        player.rate = rate
        player.play()
    }
}
