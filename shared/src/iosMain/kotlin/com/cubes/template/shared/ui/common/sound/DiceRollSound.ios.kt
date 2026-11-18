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
actual fun rememberDiceRollSound(): DiceRollSound =
    remember { IosDiceRollSound.also { it.ensureLoaded() } }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private object IosDiceRollSound : DiceRollSound {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadStarted = false

    private var players: List<AVAudioPlayer> = emptyList()
    private var nextPlayer = 0

    fun ensureLoaded() {
        if (loadStarted) return
        loadStarted = true
        scope.launch {
            try {
                val bytes = Res.readBytes(DICE_ROLL_SOUND_PATH)
                val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
                AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, error = null)
                players = List(DICE_ROLL_SOUND_VOICES) {
                    AVAudioPlayer(data = data, error = null).apply {
                        enableRate = true
                        volume = DICE_ROLL_SOUND_VOLUME
                        prepareToPlay()
                    }
                }
            } catch (e: Exception) {
                log("DiceRollSound", "Could not load the dice roll sound: $e")
            }
        }
    }

    override fun play() {
        if (players.isEmpty()) return
        val player = players[nextPlayer]
        nextPlayer = (nextPlayer + 1) % players.size
        player.stop()
        player.currentTime = 0.0
        player.rate = randomDiceRollSoundRate()
        player.play()
    }
}
