package com.cubes.template.shared.data.online

import com.cubes.template.shared.data.remote.Result
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    val currentUserId: String?

    suspend fun activeGameId(): String?
    suspend fun createRoom(game: OnlineGame, maxPlayers: Int, settings: RoomSettings): Result<String>
    suspend fun openRooms(game: OnlineGame): Result<List<RoomSummary>>
    suspend fun findRoom(code: String): Result<String>
    suspend fun requestJoin(roomId: String): Result<Unit>
    suspend fun respondToRequest(roomId: String, userId: String, accept: Boolean): Result<Unit>
    suspend fun kick(roomId: String, userId: String): Result<Unit>
    suspend fun keepAlive(roomId: String): Result<Unit>
    suspend fun leave(roomId: String): Result<Unit>
    suspend fun start(roomId: String): Result<Unit>
    suspend fun finish(roomId: String): Result<Unit>
    suspend fun room(roomId: String): Result<Room>
    fun roomChanges(roomId: String): Flow<Unit>

    suspend fun events(roomId: String, afterSeq: Int): Result<List<GameEventRecord>>
    fun eventChanges(roomId: String): Flow<Unit>
    suspend fun submitRoll(roomId: String): Result<Unit>
    suspend fun submitMove(roomId: String, pawn: Int): Result<Unit>
    suspend fun submitDice(roomId: String, count: Int): Result<Unit>
    suspend fun submitReroll(roomId: String, indices: Set<Int>): Result<Unit>
    suspend fun submitAction(roomId: String, action: String): Result<Unit>
    suspend fun submitTimeout(roomId: String, afterSeq: Int): Result<Unit>
}
