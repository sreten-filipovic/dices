package com.cubes.template.shared.ui.main.online.lobby

import com.cubes.template.shared.data.online.RoomSummary

object OnlineLobby {

    const val CODE_LENGTH = 6

    sealed interface Busy {
        data object Creating : Busy
        data object JoiningByCode : Busy
        data class JoiningRoom(val roomId: String) : Busy
    }

    data class ViewData(
        val playerOptions: List<Int>,
        val maxPlayers: Int,
        val roundOptions: List<Int> = emptyList(),
        val rounds: Int? = roundOptions.getOrNull(roundOptions.size / 2),
        val diceCountOptions: List<Int> = emptyList(),
        val diceCount: Int? = diceCountOptions.getOrNull(diceCountOptions.size / 2),
        val code: String = "",
        val codeError: String? = null,
        val rooms: List<RoomSummary> = emptyList(),
        val isLoadingRooms: Boolean = true,
        val busy: Busy? = null,
        val listener: OnlineLobbyViewListener,
    ) {
        val canJoinByCode: Boolean get() = busy == null && code.length == CODE_LENGTH
    }
}

interface OnlineLobbyViewListener {
    fun onMaxPlayersSelected(count: Int)
    fun onRoundsSelected(rounds: Int)
    fun onDiceCountSelected(count: Int)
    fun onCreateRoomClick()
    fun onCodeChanged(code: String)
    fun onJoinByCodeClick()
    fun onRoomClick(roomId: String)
}
