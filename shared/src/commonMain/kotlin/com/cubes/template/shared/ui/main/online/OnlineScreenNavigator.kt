package com.cubes.template.shared.ui.main.online

import com.cubes.template.shared.data.online.OnlineGame

interface OnlineScreenNavigator {
    fun showOnlineRoom(roomId: String)
    fun showOnlineGame(roomId: String, game: OnlineGame)
    fun back()
}
