package com.cubes.template.shared.ui.main.online.duel

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomRepository
import com.cubes.template.shared.domain.duel.DiceDuel
import com.cubes.template.shared.domain.duel.DiceDuelRules
import com.cubes.template.shared.domain.duel.DuelState
import com.cubes.template.shared.domain.online.TurnStep
import com.cubes.template.shared.ui.common.popup.PopupManager
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelUi
import com.cubes.template.shared.ui.main.dice_duel.DiceDuelViewListener
import com.cubes.template.shared.ui.main.online.turn.OnlinePlay
import com.cubes.template.shared.ui.main.online.turn.OnlineTurnGameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnlineDiceDuelViewModel(
    roomId: String,
    roomRepository: RoomRepository,
    stringProvider: StringProvider,
    popupManager: PopupManager,
    onBack: () -> Unit,
) : OnlineTurnGameViewModel<DuelState>(roomId, roomRepository, stringProvider, popupManager, DiceDuelRules, onBack),
    DiceDuelViewListener {

    private val _viewData = MutableStateFlow<DiceDuelUi.ViewData?>(null)
    val viewData: StateFlow<DiceDuelUi.ViewData?> = _viewData.asStateFlow()

    init {
        start()
    }

    override fun newGame(room: Room, names: List<String>): DuelState =
        DiceDuel.newGame(names, room.settings.diceCount ?: DEFAULT_DICE_COUNT)

    override fun onLoaded(state: DuelState, play: OnlinePlay) {
        _viewData.value = DiceDuelUi.ViewData(state = state, online = play, listener = this)
    }

    override suspend fun show(step: TurnStep<DuelState>, previous: DuelState) {
        val roller = step.rolledBy
        if (roller == null) {
            _viewData.update { it?.copy(state = step.state, dialogOpen = false, rolling = false) }
            return
        }
        _viewData.update { it?.copy(state = step.state, dialogOpen = true, tablePlayer = roller, rolling = true) }
        delay(DieAnimation.totalMillis(diceCount = step.state.diceCount))
        _viewData.update { it?.copy(rolling = false) }
        delay(RESULT_LINGER_MILLIS)
        _viewData.update { it?.copy(dialogOpen = false) }
    }

    override fun updatePlay(change: (OnlinePlay) -> OnlinePlay) {
        _viewData.update { data -> data?.copy(online = data.online?.let(change)) }
    }

    override fun onRollClick() = submitRoll()

    override fun onDoneClick() {
        if (_viewData.value?.rolling == true) return
        _viewData.update { it?.copy(dialogOpen = false) }
    }

    override fun onTiebreakClick() = Unit

    override fun onNextDuelClick() = submitRematch()

    override fun onBackClick() = requestLeave()

    companion object {
        const val DEFAULT_DICE_COUNT = 2
        private const val RESULT_LINGER_MILLIS = 1_500L
    }
}
