package com.cubes.template.shared.ui.main.game_setup

import com.cubes.template.shared.domain.duel.DiceDuel
import com.cubes.template.shared.domain.ludo.Ludo
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.main.home.Home

object GameSetup {

    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 6
    val ROUND_OPTIONS = listOf(1, 3, 5)
    val DICE_COUNT_OPTIONS = DiceDuel.DICE_COUNT_OPTIONS

    fun maxPlayers(game: Home.GameCard): Int = if (game == Home.GameCard.LUDO) Ludo.MAX_PLAYERS else MAX_PLAYERS

    enum class Mode { LOCAL, ONLINE }

    data class ViewData(
        val game: Home.GameCard = Home.GameCard.DICE_POKER,
        val mode: Mode = Mode.LOCAL,
        val isOnlineAvailable: Boolean = false,
        val names: List<String> = List(MIN_PLAYERS) { "" },
        val dieStyles: List<DieStyle> = List(MIN_PLAYERS) { DieStyle.WHITE },
        val chosenPawnColors: List<PawnColor?> = List(MIN_PLAYERS) { null },
        val settingsPlayer: Int? = null,
        val totalRounds: Int = 3,
        val diceCount: Int = DICE_COUNT_OPTIONS.first(),
        val listener: GameSetupViewListener,
    ) {
        val canAddPlayer: Boolean get() = names.size < maxPlayers(game)
        val canRemovePlayer: Boolean get() = names.size > MIN_PLAYERS

        val choosesDiceCount: Boolean get() = game == Home.GameCard.DICE_DUEL
        val choosesRounds: Boolean get() = game != Home.GameCard.DICE_DUEL && game != Home.GameCard.LUDO
        val choosesPawnColor: Boolean get() = game == Home.GameCard.LUDO

        val pawnColors: List<PawnColor> get() = PawnColor.resolve(chosenPawnColors)

        fun pawnColorsTakenByOthers(player: Int): Set<PawnColor> =
            chosenPawnColors.filterIndexed { index, _ -> index != player }.filterNotNull().toSet()
    }
}

interface GameSetupViewListener {
    fun onNameChanged(index: Int, name: String)
    fun onAddPlayerClick()
    fun onRemovePlayerClick(index: Int)
    fun onSettingsClick(index: Int)
    fun onDieStyleSelected(style: DieStyle)
    fun onPawnColorSelected(color: PawnColor)
    fun onSettingsDismiss()
    fun onTotalRoundsChanged(totalRounds: Int)
    fun onDiceCountChanged(diceCount: Int)
    fun onStartClick()
    fun onBackClick()
    fun onModeSelected(mode: GameSetup.Mode)
}

interface GameSetupScreenNavigator {
    fun startGame(
        game: Home.GameCard,
        playerNames: List<String>,
        dieStyles: List<DieStyle>,
        totalRounds: Int,
        pawnColors: List<PawnColor>,
    )
    fun startDuel(playerNames: List<String>, dieStyles: List<DieStyle>, diceCount: Int)
    fun back()
}
