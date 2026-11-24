package com.cubes.template.shared.ui.main.online.room

import com.cubes.template.shared.data.online.MemberStatus
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomMember

object OnlineRoom {

    data class ViewData(
        val room: Room? = null,
        val myUserId: String? = null,
        val busyUserId: String? = null,
        val isStarting: Boolean = false,
        val isLeaving: Boolean = false,
        val listener: OnlineRoomViewListener,
    ) {
        val isHost: Boolean get() = room != null && room.hostId == myUserId
        val me: RoomMember? get() = myUserId?.let { room?.member(it) }
        val isPending: Boolean get() = me?.status == MemberStatus.PENDING
        val hostName: String get() = room?.members?.firstOrNull { it.userId == room.hostId }?.username.orEmpty()
        val canStart: Boolean get() = isHost && !isStarting && (room?.accepted?.size ?: 0) >= 2
    }
}

interface OnlineRoomViewListener {
    fun onAcceptClick(userId: String)
    fun onDeclineClick(userId: String)
    fun onRemoveClick(userId: String)
    fun onStartClick()
    fun onLeaveClick()
}
