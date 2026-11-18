package com.cubes.template.shared.ui.main.game_setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.local.LocalDataSource
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.setup_player_hint
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.main.home.Home
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameSetupViewModel(
    game: Home.GameCard,
    private val screenNavigator: GameSetupScreenNavigator,
    private val stringProvider: StringProvider,
    private val localDataSource: LocalDataSource,
) : ViewModel(), GameSetupViewListener {

    private val rememberedStyles: List<DieStyle> = localDataSource.getPlayerDieStyles()
        .map { name -> DieStyle.entries.firstOrNull { it.name == name } ?: DieStyle.WHITE }

    private fun rememberedStyle(slot: Int): DieStyle = rememberedStyles.getOrNull(slot) ?: DieStyle.WHITE

    private val initialNames: List<String> = localDataSource.getPlayerNames()
        .take(GameSetup.MAX_PLAYERS)
        .let { names -> names + List((GameSetup.MIN_PLAYERS - names.size).coerceAtLeast(0)) { "" } }

    private val _viewData = MutableStateFlow(
        GameSetup.ViewData(
            game = game,
            names = initialNames,
            dieStyles = List(initialNames.size, ::rememberedStyle),
            listener = this,
        )
    )
    val viewData: StateFlow<GameSetup.ViewData> = _viewData.asStateFlow()

    private var nameSaveJob: Job? = null

    override fun onNameChanged(index: Int, name: String) {
        _viewData.update { data ->
            data.copy(names = data.names.mapIndexed { i, old -> if (i == index) name else old })
        }
        nameSaveJob?.cancel()
        nameSaveJob = viewModelScope.launch {
            delay(NAME_SAVE_DELAY_MILLIS)
            persist()
        }
    }

    override fun onAddPlayerClick() {
        _viewData.update { if (it.canAddPlayer) it.copy(names = it.names + "", dieStyles = it.dieStyles + rememberedStyle(it.names.size)) else it }
        persist()
    }

    override fun onRemovePlayerClick(index: Int) {
        _viewData.update { data ->
            if (!data.canRemovePlayer) return@update data
            data.copy(
                names = data.names.filterIndexed { i, _ -> i != index },
                dieStyles = data.dieStyles.filterIndexed { i, _ -> i != index },
                settingsPlayer = null,
            )
        }
        persist()
    }

    override fun onSettingsClick(index: Int) {
        _viewData.update { if (index in it.names.indices) it.copy(settingsPlayer = index) else it }
    }

    override fun onDieStyleSelected(style: DieStyle) {
        _viewData.update { data ->
            val player = data.settingsPlayer ?: return@update data
            data.copy(dieStyles = data.dieStyles.mapIndexed { i, old -> if (i == player) style else old })
        }
        persist()
    }

    override fun onSettingsDismiss() {
        _viewData.update { it.copy(settingsPlayer = null) }
    }

    override fun onTotalRoundsChanged(totalRounds: Int) {
        if (totalRounds !in GameSetup.ROUND_OPTIONS) return
        _viewData.update { it.copy(totalRounds = totalRounds) }
    }

    override fun onDiceCountChanged(diceCount: Int) {
        if (diceCount !in GameSetup.DICE_COUNT_OPTIONS) return
        _viewData.update { it.copy(diceCount = diceCount) }
    }

    override fun onStartClick() {
        val data = _viewData.value
        val names = data.names.mapIndexed { index, name ->
            name.trim().ifEmpty { stringProvider.getString(Res.string.setup_player_hint, index + 1) }
        }
        persist()
        if (data.choosesDiceCount) {
            screenNavigator.startDuel(playerNames = names, dieStyles = data.dieStyles, diceCount = data.diceCount)
        } else {
            screenNavigator.startGame(
                game = data.game,
                playerNames = names,
                dieStyles = data.dieStyles,
                totalRounds = data.totalRounds,
            )
        }
    }

    override fun onBackClick() = screenNavigator.back()

    override fun onCleared() {
        persist()
    }

    private fun persist() {
        nameSaveJob?.cancel()
        val data = _viewData.value
        localDataSource.savePlayerNames(data.names.map { it.trim() })
        localDataSource.savePlayerDieStyles((data.dieStyles + rememberedStyles.drop(data.dieStyles.size)).map { it.name })
    }

    private companion object {
        const val NAME_SAVE_DELAY_MILLIS = 500L
    }
}
