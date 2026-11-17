package com.cubes.template.shared.ui.main.game_setup

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.setup_player_hint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameSetupViewModel(
    private val screenNavigator: GameSetupScreenNavigator,
    private val stringProvider: StringProvider,
) : ViewModel(), GameSetupViewListener {

    private val _viewData = MutableStateFlow(GameSetup.ViewData(listener = this))
    val viewData: StateFlow<GameSetup.ViewData> = _viewData.asStateFlow()

    override fun onNameChanged(index: Int, name: String) {
        _viewData.update { data ->
            data.copy(names = data.names.mapIndexed { i, old -> if (i == index) name else old })
        }
    }

    override fun onAddPlayerClick() {
        _viewData.update { if (it.canAddPlayer) it.copy(names = it.names + "") else it }
    }

    override fun onRemovePlayerClick(index: Int) {
        _viewData.update { data ->
            if (data.canRemovePlayer) data.copy(names = data.names.filterIndexed { i, _ -> i != index }) else data
        }
    }

    override fun onTotalRoundsChanged(totalRounds: Int) {
        if (totalRounds !in GameSetup.ROUND_OPTIONS) return
        _viewData.update { it.copy(totalRounds = totalRounds) }
    }

    override fun onStartClick() {
        val data = _viewData.value
        val names = data.names.mapIndexed { index, name ->
            name.trim().ifEmpty { stringProvider.getString(Res.string.setup_player_hint, index + 1) }
        }
        screenNavigator.startGame(playerNames = names, totalRounds = data.totalRounds)
    }

    override fun onBackClick() = screenNavigator.back()
}
