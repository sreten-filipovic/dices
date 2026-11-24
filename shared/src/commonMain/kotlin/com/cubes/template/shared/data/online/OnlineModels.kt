package com.cubes.template.shared.data.online

import kotlinx.serialization.json.JsonObject

enum class OnlineGame(val key: String) { LUDO("ludo") }

data class RoomSummary(
    val id: String,
    val code: String,
    val hostName: String,
    val maxPlayers: Int,
    val playerCount: Int,
)

enum class RoomStatus { OPEN, PLAYING, FINISHED, CLOSED }

enum class MemberStatus { PENDING, ACCEPTED, REJECTED, LEFT, KICKED }

data class RoomMember(
    val userId: String,
    val username: String,
    val status: MemberStatus,
    val seat: Int?,
)

data class Room(
    val id: String,
    val code: String,
    val game: String,
    val hostId: String,
    val maxPlayers: Int,
    val status: RoomStatus,
    val members: List<RoomMember>,
) {
    val accepted: List<RoomMember> get() = members.filter { it.status == MemberStatus.ACCEPTED }
    val pending: List<RoomMember> get() = members.filter { it.status == MemberStatus.PENDING }
    val seated: List<RoomMember> get() = accepted.filter { it.seat != null }.sortedBy { it.seat }
    val isFull: Boolean get() = accepted.size >= maxPlayers

    fun member(userId: String): RoomMember? = members.firstOrNull { it.userId == userId }
}

data class GameEventRecord(
    val seq: Int,
    val userId: String,
    val type: String,
    val payload: JsonObject,
)

class RoomFailure(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {
    enum class Reason {
        ROOM_NOT_FOUND,
        ROOM_FULL,
        REQUEST_REJECTED,
        NOT_ENOUGH_PLAYERS,
        TOO_EARLY,
        NOT_ALLOWED,
        NOT_SIGNED_IN,
        NETWORK,
        UNKNOWN,
    }
}
