package com.cubes.template.shared.ui.main.online.ludo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.GameEventRecord
import com.cubes.template.shared.data.online.RoomFailure
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.ludo.Ludo
import com.cubes.template.shared.domain.ludo.LudoEvent
import com.cubes.template.shared.domain.ludo.LudoMatch
import com.cubes.template.shared.domain.ludo.LudoOnline
import com.cubes.template.shared.domain.ludo.LudoPhase
import com.cubes.template.shared.domain.ludo.LudoState
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.ludo_leave_confirm
import com.cubes.template.shared.resources.ludo_leave_message
import com.cubes.template.shared.resources.ludo_leave_title
import com.cubes.template.shared.resources.online_error_title
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.main.ludo.LudoGeometry
import com.cubes.template.shared.ui.main.ludo.LudoUi
import com.cubes.template.shared.ui.main.ludo.LudoViewListener
import com.cubes.template.shared.ui.main.online.roomErrorMessage
import com.cubes.template.shared.ui.main.online.roomFailureReason
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class OnlineLudoViewModel(
    private val roomId: String,
    private val roomRepository: RoomRepository,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val onBack: () -> Unit,
) : ViewModel(), LudoViewListener {

    private val _viewData = MutableStateFlow<LudoUi.ViewData?>(null)
    val viewData: StateFlow<LudoUi.ViewData?> = _viewData.asStateFlow()

    private lateinit var seats: List<String>
    private lateinit var match: LudoMatch
    private var fetchedSeq = 0
    private val fetchLock = Mutex()
    private val incoming = Channel<GameEventRecord>(Channel.UNLIMITED)
    private var turnStartedAt = TimeSource.Monotonic.markNow()
    private var timeoutRequestedAfter = -1
    private var autoMovedAfter = -1
    private var finishReported = false
    private var leaving = false

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val room = retry { roomRepository.room(roomId) } ?: return
        seats = room.seated.map { it.userId }
        val names = room.seated.map { it.username }
        val history = retry { roomRepository.events(roomId, 0) } ?: return
        match = LudoOnline.replay(LudoOnline.start(names), history.map(::toEvent))
        fetchedSeq = match.lastSeq
        _viewData.value = LudoUi.ViewData(
            state = match.state,
            pawnColors = PawnColor.defaultsFor(names.size),
            dieValue = match.state.lastRoll,
            online = LudoUi.Online(myPlayer = seats.indexOf(roomRepository.currentUserId)),
            listener = this,
        )
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
        val result = LudoOnline.apply(match, toEvent(record))
        match = result.match
        turnStartedAt = TimeSource.Monotonic.markNow()
        updateOnline { it.copy(submitting = false, secondsLeft = LudoOnline.TURN_SECONDS) }
        animate(previous, result.steps)
        afterStateChanged()
    }

    private suspend fun animate(start: LudoState, steps: List<LudoState>) {
        var previous = start
        for (step in steps) {
            when {
                step.lastMove != null && step.lastMove != previous.lastMove -> animateMove(previous, step)
                step.forfeited.size > previous.forfeited.size -> _viewData.update { it?.copy(state = step) }
                step.lastRoll != null -> animateRoll(previous, step)
                else -> _viewData.update { it?.copy(state = step) }
            }
            previous = step
        }
    }

    private suspend fun animateRoll(old: LudoState, new: LudoState) {
        val roll = new.lastRoll ?: return
        _viewData.update {
            it?.copy(dieValue = roll, rollKey = it.rollKey + 1, rolling = true, extraRoll = false, finishedPlayer = null)
        }
        delay(DieAnimation.totalMillis(diceCount = 1))
        when {
            new.phase is LudoPhase.ChoosingPawn -> _viewData.update { it?.copy(state = new, rolling = false) }
            new.currentPlayer == old.currentPlayer -> _viewData.update {
                it?.copy(state = new, rolling = false, extraRoll = roll == Ludo.EXIT_ROLL)
            }
            else -> {
                _viewData.update { it?.copy(rolling = false, noMoves = true) }
                delay(NO_MOVES_MILLIS)
                _viewData.update { it?.copy(state = new, noMoves = false) }
            }
        }
    }

    private suspend fun animateMove(old: LudoState, new: LudoState) {
        val move = new.lastMove ?: return
        _viewData.update { it?.copy(state = new, moving = true) }
        val captureMillis = if (move.captured.isEmpty()) 0 else LudoGeometry.CAPTURE_MILLIS
        delay(LudoGeometry.steps(move) * LudoGeometry.STEP_MILLIS.toLong() + captureMillis + SETTLE_MILLIS)
        _viewData.update {
            it?.copy(
                moving = false,
                extraRoll = new.phase == LudoPhase.WaitingForRoll && new.currentPlayer == old.currentPlayer,
                finishedPlayer = new.ranking.lastOrNull().takeIf { new.ranking.size > old.ranking.size },
            )
        }
    }

    private fun afterStateChanged() {
        val state = match.state
        if (state.phase is LudoPhase.GameOver) {
            updateOnline { it.copy(secondsLeft = null) }
            if (!finishReported) {
                finishReported = true
                viewModelScope.launch { roomRepository.finish(roomId) }
            }
            return
        }
        val phase = state.phase as? LudoPhase.ChoosingPawn ?: return
        val single = phase.movable.singleOrNull() ?: return
        if (state.currentPlayer != myPlayer || autoMovedAfter == match.lastSeq) return
        autoMovedAfter = match.lastSeq
        viewModelScope.launch {
            delay(AUTO_MOVE_MILLIS)
            onPawnClick(single)
        }
    }

    private suspend fun tick() {
        if (match.state.phase is LudoPhase.GameOver) return
        val elapsed = turnStartedAt.elapsedNow().inWholeSeconds.toInt()
        updateOnline { it.copy(secondsLeft = (LudoOnline.TURN_SECONDS - elapsed).coerceAtLeast(0)) }
        val after = match.lastSeq
        if (elapsed >= LudoOnline.TURN_SECONDS + TIMEOUT_GRACE_SECONDS && timeoutRequestedAfter != after) {
            timeoutRequestedAfter = after
            val result = roomRepository.submitTimeout(roomId, after)
            if (result is Result.Error && result.ex.roomFailureReason == RoomFailure.Reason.TOO_EARLY) {
                timeoutRequestedAfter = -1
                turnStartedAt = TimeSource.Monotonic.markNow() - TIMEOUT_RETRY_SECONDS.seconds
            }
            fetchNew()
        }
    }

    private val myPlayer: Int get() = _viewData.value?.online?.myPlayer ?: -1

    private fun updateOnline(change: (LudoUi.Online) -> LudoUi.Online) {
        _viewData.update { data -> data?.copy(online = data.online?.let(change)) }
    }

    override fun onRollClick() {
        if (_viewData.value?.canRoll != true) return
        submit { roomRepository.submitRoll(roomId) }
    }

    override fun onPawnClick(pawn: Int) {
        val data = _viewData.value ?: return
        if (pawn !in data.movable) return
        submit { roomRepository.submitMove(roomId, pawn) }
    }

    private fun submit(action: suspend () -> Result<Unit>) {
        updateOnline { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val result = action()) {
                is Result.Success -> fetchNew()
                is Result.Error -> {
                    updateOnline { it.copy(submitting = false) }
                    showError(result.ex)
                }
            }
        }
    }

    override fun onPlayAgainClick() = Unit

    override fun onBackClick() {
        val state = _viewData.value?.state
        if (state == null || state.phase is LudoPhase.GameOver || !state.isActive(myPlayer)) {
            onBack()
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

    private fun toEvent(record: GameEventRecord): LudoEvent = record.toLudoEvent(seats)

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
        const val NO_MOVES_MILLIS = 1100L
        const val AUTO_MOVE_MILLIS = 450L
        const val SETTLE_MILLIS = 120L
    }
}
