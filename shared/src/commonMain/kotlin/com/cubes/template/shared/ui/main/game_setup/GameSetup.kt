package com.cubes.template.shared.ui.main.game_setup

object GameSetup {

    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 6
    const val MIN_TARGET_WINS = 1
    const val MAX_TARGET_WINS = 5

    data class ViewData(
        /** Raw names as typed; blank names fall back to "Igrač N" when the game starts. */
        val names: List<String> = List(MIN_PLAYERS) { "" },
        val targetWins: Int = 3,
        val listener: GameSetupViewListener,
    ) {
        val canAddPlayer: Boolean get() = names.size < MAX_PLAYERS
        val canRemovePlayer: Boolean get() = names.size > MIN_PLAYERS
    }
}

interface GameSetupViewListener {
    fun onNameChanged(index: Int, name: String)
    fun onAddPlayerClick()
    fun onRemovePlayerClick(index: Int)
    fun onTargetWinsChanged(targetWins: Int)
    fun onStartClick()
    fun onBackClick()
}

interface GameSetupScreenNavigator {
    fun startGame(playerNames: List<String>, targetWins: Int)
    fun back()
}
