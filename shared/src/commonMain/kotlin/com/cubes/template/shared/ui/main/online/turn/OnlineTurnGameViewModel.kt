package com.cubes.template.shared.ui.main.online.turn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.GameEventRecord
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomFailure
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.online.TurnGameOnline
import com.cubes.template.shared.domain.online.TurnMatch
import com.cubes.template.shared.domain.online.TurnRules
import com.cubes.template.shared.domain.online.TurnStep
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.ludo_leave_confirm
import com.cubes.template.shared.resources.ludo_leave_message
import com.cubes.template.shared.resources.ludo_leave_title
import com.cubes.template.shared.resources.online_error_title
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import com.cubes.template.shared.ui.main.online.roomErrorMessage
import com.cubes.template.shared.ui.main.online.roomFailureReason
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

abstract class OnlineTurnGameViewModel<S>(
    private val roomId: String,
    private val roomRepository: RoomRepository,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val rules: TurnRules<S>,
    private val onBack: () -> Unit,
) : ViewModel() {

    private val online = TurnGameOnline(rules)
    private lateinit var seats: List<String>
    protected lateinit var match: TurnMatch<S>
        private set
    protected var myPlayer: Int = -1
        private set
    protected var busy: Boolean = false
        private set
    private var fetchedSeq = 0
    private val fetchLock = Mutex()
    private val incoming = Channel<GameEventRecord>(Channel.UNLIMITED)
    private var turnStartedAt = TimeSource.Monotonic.markNow()
    private var timeoutRequestedAfter = -1
    private var finishReported = false
    private var leaving = false

    protected abstract fun newGame(room: Room, names: List<String>): S
    protected abstract fun onLoaded(state: S, play: OnlinePlay)
    protected abstract suspend fun show(step: TurnStep<S>, previous: S)
    protected abstract fun updatePlay(change: (OnlinePlay) -> OnlinePlay)

    protected fun start() {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val room = retry { roomRepository.room(roomId) } ?: return
        seats = room.seated.map { it.userId }
        val names = room.seated.map { it.username }
        val history = retry { roomRepository.events(roomId, 0) } ?: return
        match = online.replay(online.start(newGame(room, names)), history.map { it.toTurnEvent(seats) })
        fetchedSeq = match.lastSeq
        myPlayer = seats.indexOf(roomRepository.currentUserId)
        onLoaded(match.state, OnlinePlay(myPlayer = myPlayer, secondsLeft = TurnGameOnline.TURN_SECONDS))
        afterStateChanged()

        viewModelScope.launch { for (record in incoming) process(record) }
        viewModelScope.launch { roomRepository.eventChanges(roomId).collect { fetchNew() } }
        viewModelScope.launch {
            while (true) {
                fetchNew()
                delay(POLL_MILLIS)
            }
        }
        viewModelScope.launch {
            while (true) {
                tick()
                delay(TICK_MILLIS)
            }
        }
    }

    private suspend fun <T : Any> retry(block: suspend () -> Result<T>): T? {
        while (true) {
            when (val result = block()) {
                is Result.Success -> return result.data
                is Result.Error -> if (result.ex.roomFailureReason == RoomFailure.Reason.ROOM_NOT_FOUND) {
                    onBack()
                    return null
                }
            }
            delay(POLL_MILLIS)
        }
    }

    private suspend fun fetchNew() = fetchLock.withLock {
        val result = roomRepository.events(roomId, fetchedSeq)
        if (result !is Result.Success) return@withLock
        result.data.filter { it.seq > fetchedSeq }.forEach { record ->
            fetchedSeq = record.seq
            incoming.send(record)
        }
    }

    private suspend fun process(record: GameEventRecord) {
        val previous = match.state
        val result = online.apply(match, record.toTurnEvent(seats))
        match = result.match
        updatePlay { it.copy(submitting = false) }
        busy = true
        var before = previous
        for (step in result.steps) {
            show(step, before)
            if (rules.isRoundOver(step.state)) delay(ROUND_RESULT_MILLIS)
            before = step.state
        }
        busy = false
        turnStartedAt = TimeSource.Monotonic.markNow()
        updatePlay { it.copy(secondsLeft = TurnGameOnline.TURN_SECONDS) }
        afterStateChanged()
    }

    private fun afterStateChanged() {
        if (!rules.isMatchOver(match.state)) return
        updatePlay { it.copy(secondsLeft = null) }
        if (rules.isFinished(match.state) && !finishReported) {
            finishReported = true
            viewModelScope.launch { roomRepository.finish(roomId) }
        }
    }

    private suspend fun tick() {
        if (rules.isMatchOver(match.state) || busy) return
        val elapsed = turnStartedAt.elapsedNow().inWholeSeconds.toInt()
        updatePlay { it.copy(secondsLeft = (TurnGameOnline.TURN_SECONDS - elapsed).coerceAtLeast(0)) }
        val after = match.lastSeq
        if (elapsed >= TurnGameOnline.TURN_SECONDS + TIMEOUT_GRACE_SECONDS && timeoutRequestedAfter != after) {
            timeoutRequestedAfter = after
            val result = roomRepository.submitTimeout(roomId, after)
            if (result is Result.Error && result.ex.roomFailureReason == RoomFailure.Reason.TOO_EARLY) {
                timeoutRequestedAfter = -1
                turnStartedAt = TimeSource.Monotonic.markNow() - TIMEOUT_RETRY_SECONDS.seconds
            }
            fetchNew()
        }
    }

    protected val isMyTurn: Boolean
        get() = ::match.isInitialized && !busy && rules.currentPlayer(match.state) == myPlayer

    protected fun submitRoll() {
        if (!isMyTurn) return
        val count = rules.diceForNextRoll(match.state)
        if (count == 0) return
        submit { roomRepository.submitDice(roomId, count) }
    }

    protected fun submitEndTurn(action: String) {
        if (!isMyTurn || !rules.canEndTurn(match.state)) return
        submit { roomRepository.submitAction(roomId, action) }
    }

    private fun submit(action: suspend () -> Result<Unit>) {
        updatePlay { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val result = action()) {
                is Result.Success -> fetchNew()
                is Result.Error -> {
                    updatePlay { it.copy(submitting = false) }
                    showError(result.ex)
                }
            }
        }
    }

    protected val canRematch: Boolean
        get() = ::match.isInitialized && !busy && myPlayer >= 0 && myPlayer !in match.left &&
            rules.rematch(match.state) != null

    protected fun submitRematch() {
        if (!canRematch) return
        submit { roomRepository.submitAction(roomId, REMATCH) }
    }

    protected fun requestLeave() {
        if (!::match.isInitialized || rules.isFinished(match.state) || myPlayer < 0 || myPlayer in match.left) {
            onBack()
            return
        }
        if (rules.isMatchOver(match.state)) {
            leave()
            return
        }
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.ludo_leave_title),
                message = stringProvider.getString(Res.string.ludo_leave_message),
                confirmText = stringProvider.getString(Res.string.ludo_leave_confirm),
                isDestructive = true,
                onConfirm = ::leave,
            )
        )
    }

    private fun leave() {
        if (leaving) return
        leaving = true
        viewModelScope.launch {
            roomRepository.leave(roomId)
            onBack()
        }
    }

    private fun showError(error: Throwable) {
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.online_error_title),
                message = stringProvider.roomErrorMessage(error),
            )
        )
    }

    private companion object {
        const val POLL_MILLIS = 2_500L
        const val TICK_MILLIS = 1_000L
        const val TIMEOUT_GRACE_SECONDS = 2
        const val TIMEOUT_RETRY_SECONDS = 55
        const val ROUND_RESULT_MILLIS = 5_000L
    }
}
