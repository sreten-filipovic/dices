package com.cubes.template.shared.ui.main.online.poker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.GameEventRecord
import com.cubes.template.shared.data.online.RoomFailure
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.data.remote.Result
import com.cubes.template.shared.domain.game.DicePokerOnline
import com.cubes.template.shared.domain.game.DiceRoller
import com.cubes.template.shared.domain.game.GameAction
import com.cubes.template.shared.domain.game.GameEngine
import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.domain.game.PokerEvent
import com.cubes.template.shared.domain.game.PokerMatch
import com.cubes.template.shared.domain.game.PokerRollStage
import com.cubes.template.shared.domain.game.PokerStep
import com.cubes.template.shared.domain.game.RoundPhase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.ludo_leave_confirm
import com.cubes.template.shared.resources.ludo_leave_message
import com.cubes.template.shared.resources.ludo_leave_title
import com.cubes.template.shared.resources.online_error_title
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.popup.PopupState
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.main.game.Game
import com.cubes.template.shared.ui.main.game.GameViewListener
import com.cubes.template.shared.ui.main.online.ResumeGameViewModel
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

class OnlineDicePokerViewModel(
    private val roomId: String,
    private val roomRepository: RoomRepository,
    private val stringProvider: StringProvider,
    private val popupManager: PopupManager,
    private val onBack: () -> Unit,
) : ViewModel(), GameViewListener {

    private val _viewData = MutableStateFlow<Game.ViewData?>(null)
    val viewData: StateFlow<Game.ViewData?> = _viewData.asStateFlow()

    private lateinit var seats: List<String>
    private lateinit var match: PokerMatch
    private var shownState: GameState? = null
    private var selected: Set<Int> = emptySet()
    private var fetchedSeq = 0
    private val fetchLock = Mutex()
    private val incoming = Channel<GameEventRecord>(Channel.UNLIMITED)
    private var turnStartedAt = TimeSource.Monotonic.markNow()
    private var timerPaused = false
    private var timeoutRequestedAfter = -1
    private var finishReported = false
    private var leaving = false

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val room = retry { roomRepository.room(roomId) } ?: return
        seats = room.seated.map { it.userId }
        val names = room.seated.map { it.username }
        val rounds = room.settings.rounds ?: ResumeGameViewModel.DEFAULT_ROUNDS
        val history = retry { roomRepository.events(roomId, 0) } ?: return
        match = DicePokerOnline.replay(DicePokerOnline.start(names, rounds), history.map(::toEvent))
        fetchedSeq = match.lastSeq
        val mySeat = seats.indexOf(roomRepository.currentUserId)
        val myId = match.state.players.getOrNull(mySeat)?.id.orEmpty()
        shownState = match.state
        _viewData.value = Game.ViewData(
            state = match.state,
            online = Game.Online(myPlayerId = myId, secondsLeft = DicePokerOnline.TURN_SECONDS),
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
        val result = DicePokerOnline.apply(match, toEvent(record))
        match = result.match
        if (result.steps.isNotEmpty()) selected = emptySet()
        updateOnline { it.copy(submitting = false) }
        timerPaused = true
        result.steps.forEach { show(it) }
        timerPaused = false
        turnStartedAt = TimeSource.Monotonic.markNow()
        updateOnline { it.copy(secondsLeft = DicePokerOnline.TURN_SECONDS) }
        afterStateChanged()
    }

    private suspend fun show(step: PokerStep) {
        val roll = step.roll
        shownState = step.state
        if (roll == null) {
            _viewData.update { it?.copy(state = displayed(), rollDialog = null) }
        } else {
            val stage = if (roll.stage == PokerRollStage.FIRST) Game.Stage.FIRST_ROLL else Game.Stage.SECOND_ROLL
            _viewData.update { data ->
                data ?: return@update null
                val ticks = data.rollTicksOf(roll.playerId)
                    .mapIndexed { index, tick -> if (index in roll.indices) tick + 1 else tick }
                data.copy(
                    state = displayed(),
                    rollTicks = data.rollTicks + (roll.playerId to ticks),
                    rollDialog = Game.RollDialog(playerId = roll.playerId, stage = stage, animating = true),
                )
            }
            delay(DieAnimation.totalMillis())
            _viewData.update { it?.copy(rollDialog = it.rollDialog?.copy(animating = false)) }
            if (stage == Game.Stage.SECOND_ROLL) {
                delay(RESULT_LINGER_MILLIS)
                _viewData.update { it?.copy(rollDialog = null) }
            }
        }
        if (step.state.phase is RoundPhase.RoundOver) {
            _viewData.update { it?.copy(rollDialog = null) }
            delay(ROUND_RESULT_MILLIS)
        }
    }

    private fun displayed(): GameState {
        val state = shownState ?: match.state
        if (selected.isEmpty() || state.phase != RoundPhase.SecondRoll) return state
        return selected.fold(state) { current, index ->
            GameEngine.reduce(current, GameAction.ToggleSelect(index), NO_ROLLER)
        }
    }

    private fun afterStateChanged() {
        if (match.state.phase !is RoundPhase.MatchOver) return
        updateOnline { it.copy(secondsLeft = null) }
        if (!finishReported) {
            finishReported = true
            viewModelScope.launch { roomRepository.finish(roomId) }
        }
    }

    private suspend fun tick() {
        if (match.state.phase is RoundPhase.MatchOver || timerPaused) return
        val elapsed = turnStartedAt.elapsedNow().inWholeSeconds.toInt()
        updateOnline { it.copy(secondsLeft = (DicePokerOnline.TURN_SECONDS - elapsed).coerceAtLeast(0)) }
        val after = match.lastSeq
        if (elapsed >= DicePokerOnline.TURN_SECONDS + TIMEOUT_GRACE_SECONDS && timeoutRequestedAfter != after) {
            timeoutRequestedAfter = after
            val result = roomRepository.submitTimeout(roomId, after)
            if (result is Result.Error && result.ex.roomFailureReason == RoomFailure.Reason.TOO_EARLY) {
                timeoutRequestedAfter = -1
                turnStartedAt = TimeSource.Monotonic.markNow() - TIMEOUT_RETRY_SECONDS.seconds
            }
            fetchNew()
        }
    }

    private fun updateOnline(change: (Game.Online) -> Game.Online) {
        _viewData.update { data -> data?.copy(online = data.online?.let(change)) }
    }

    private val myTurnNow: Boolean
        get() {
            val data = _viewData.value ?: return false
            return data.isMyTurn && data.online?.submitting == false && data.rollDialog?.animating != true &&
                shownState == match.state
        }

    override fun onRollClick() {
        if (!myTurnNow || match.state.phase != RoundPhase.FirstRoll) return
        submit { roomRepository.submitDice(roomId, DICE_COUNT) }
    }

    override fun onDieClick(index: Int) {
        if (!myTurnNow || match.state.phase != RoundPhase.SecondRoll) return
        selected = if (index in selected) selected - index else selected + index
        _viewData.update { it?.copy(state = displayed()) }
    }

    override fun onRerollClick() {
        if (!myTurnNow || match.state.phase != RoundPhase.SecondRoll || selected.isEmpty()) return
        val indices = selected
        submit { roomRepository.submitReroll(roomId, indices) }
    }

    override fun onKeepClick() {
        if (!myTurnNow || match.state.phase != RoundPhase.SecondRoll) return
        submit { roomRepository.submitAction(roomId, KEEP) }
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

    override fun onDoneClick() {
        _viewData.update { it?.copy(rollDialog = null) }
    }

    override fun onNextRoundClick() = Unit

    override fun onNewMatchClick() = Unit

    override fun onHomeClick() {
        if (!::match.isInitialized) {
            onBack()
            return
        }
        val state = match.state
        val myId = _viewData.value?.online?.myPlayerId
        if (state.phase is RoundPhase.MatchOver || myId == null || myId in state.leftIds) {
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

    private fun toEvent(record: GameEventRecord): PokerEvent = record.toPokerEvent(seats)

    private fun showError(error: Throwable) {
        popupManager.show(
            PopupState.Info(
                title = stringProvider.getString(Res.string.online_error_title),
                message = stringProvider.roomErrorMessage(error),
            )
        )
    }

    private companion object {
        const val DICE_COUNT = 5
        const val KEEP = "keep"
        const val POLL_MILLIS = 2_500L
        const val TICK_MILLIS = 1_000L
        const val TIMEOUT_GRACE_SECONDS = 2
        const val TIMEOUT_RETRY_SECONDS = 55
        const val RESULT_LINGER_MILLIS = 1_500L
        const val ROUND_RESULT_MILLIS = 5_000L
        val NO_ROLLER = DiceRoller { 1 }
    }
}
