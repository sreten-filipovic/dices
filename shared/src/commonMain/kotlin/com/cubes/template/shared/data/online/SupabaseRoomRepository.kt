package com.cubes.template.shared.data.online

import com.cubes.template.shared.data.remote.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random

class SupabaseRoomRepository(private val supabase: SupabaseClient) : RoomRepository {

    override val currentUserId: String? get() = supabase.auth.currentUserOrNull()?.id

    override suspend fun activeGameId(): String? {
        if (currentUserId == null) return null
        val result = attempt { call("my_active_game", buildJsonObject { }).data.trim() }
        val id = (result as? Result.Success)?.data ?: return null
        return id.removeSurrounding("\"").takeIf { it.isNotEmpty() && it != "null" }
    }

    override suspend fun createRoom(game: OnlineGame, maxPlayers: Int): Result<String> = attempt {
        call("create_room", buildJsonObject {
            put("p_game", game.key)
            put("p_max_players", maxPlayers)
        }).decodeAs<String>()
    }

    override suspend fun openRooms(game: OnlineGame): Result<List<RoomSummary>> = attempt {
        call("list_open_rooms", buildJsonObject { put("p_game", game.key) })
            .decodeList<RoomSummaryDto>()
            .map { RoomSummary(it.id, it.code, it.hostName, it.maxPlayers, it.playerCount) }
    }

    override suspend fun findRoom(code: String): Result<String> = attempt {
        call("find_room", buildJsonObject { put("p_code", code) }).decodeAs<String>()
    }

    override suspend fun requestJoin(roomId: String): Result<Unit> = attempt {
        call("request_join", roomArgs(roomId))
    }

    override suspend fun respondToRequest(roomId: String, userId: String, accept: Boolean): Result<Unit> = attempt {
        call("respond_join", buildJsonObject {
            put("p_room", roomId)
            put("p_user", userId)
            put("p_accept", accept)
        })
    }

    override suspend fun kick(roomId: String, userId: String): Result<Unit> = attempt {
        call("kick_member", buildJsonObject {
            put("p_room", roomId)
            put("p_user", userId)
        })
    }

    override suspend fun keepAlive(roomId: String): Result<Unit> = attempt { call("touch_room", roomArgs(roomId)) }

    override suspend fun leave(roomId: String): Result<Unit> = attempt { call("leave_room", roomArgs(roomId)) }

    override suspend fun start(roomId: String): Result<Unit> = attempt { call("start_room", roomArgs(roomId)) }

    override suspend fun finish(roomId: String): Result<Unit> = attempt { call("finish_room", roomArgs(roomId)) }

    override suspend fun room(roomId: String): Result<Room> = attempt {
        val json = call("get_room", roomArgs(roomId)).data.trim()
        if (json.isEmpty() || json == "null") throw RoomFailure(RoomFailure.Reason.ROOM_NOT_FOUND)
        val room = Json { ignoreUnknownKeys = true }.decodeFromString<RoomDto>(json)
        val members = room.members
        Room(
            id = room.id,
            code = room.code,
            game = room.game,
            hostId = room.hostId,
            maxPlayers = room.maxPlayers,
            status = enumValueOf(room.status.uppercase()),
            members = members.map {
                RoomMember(it.userId, it.username, enumValueOf(it.status.uppercase()), it.seat)
            },
        )
    }

    override fun roomChanges(roomId: String): Flow<Unit> = changes(roomId, ROOMS to "id", MEMBERS to "room_id")

    override suspend fun events(roomId: String, afterSeq: Int): Result<List<GameEventRecord>> = attempt {
        supabase.postgrest.from(EVENTS).select {
            filter {
                eq("room_id", roomId)
                gt("seq", afterSeq)
            }
            order("seq", Order.ASCENDING)
        }.decodeList<EventDto>().map { GameEventRecord(it.seq, it.userId, it.type, it.payload) }
    }

    override fun eventChanges(roomId: String): Flow<Unit> = changes(roomId, EVENTS to "room_id")

    override suspend fun submitRoll(roomId: String): Result<Unit> = attempt { call("submit_roll", roomArgs(roomId)) }

    override suspend fun submitMove(roomId: String, pawn: Int): Result<Unit> = attempt {
        call("submit_move", buildJsonObject {
            put("p_room", roomId)
            put("p_pawn", pawn)
        })
    }

    override suspend fun submitTimeout(roomId: String, afterSeq: Int): Result<Unit> = attempt {
        call("submit_timeout", buildJsonObject {
            put("p_room", roomId)
            put("p_after_seq", afterSeq)
        })
    }

    private fun changes(roomId: String, vararg tables: Pair<String, String>): Flow<Unit> = channelFlow {
        val channel = supabase.channel("room-$roomId-${Random.nextInt().toUInt()}")
        val flows = tables.map { (table, column) ->
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                this.table = table
                filter(column, FilterOperator.EQ, roomId)
            }
        }
        launch { merge(*flows.toTypedArray()).collect { send(Unit) } }
        try {
            channel.subscribe()
            awaitCancellation()
        } finally {
            withContext(NonCancellable) { runCatching { supabase.realtime.removeChannel(channel) } }
        }
    }

    private suspend fun call(function: String, args: JsonObject) = supabase.postgrest.rpc(function, args)

    private fun roomArgs(roomId: String) = buildJsonObject { put("p_room", roomId) }

    private suspend fun <T : Any> attempt(block: suspend () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toRoomFailure())
    }

    private fun Exception.toRoomFailure(): RoomFailure = when (this) {
        is RoomFailure -> this
        is PostgrestRestException -> RoomFailure(
            when (message?.lineSequence()?.firstOrNull()?.trim()) {
                "room_not_found" -> RoomFailure.Reason.ROOM_NOT_FOUND
                "room_full" -> RoomFailure.Reason.ROOM_FULL
                "request_rejected" -> RoomFailure.Reason.REQUEST_REJECTED
                "not_enough_players" -> RoomFailure.Reason.NOT_ENOUGH_PLAYERS
                "too_early" -> RoomFailure.Reason.TOO_EARLY
                "not_allowed" -> RoomFailure.Reason.NOT_ALLOWED
                "not_signed_in" -> RoomFailure.Reason.NOT_SIGNED_IN
                else -> RoomFailure.Reason.UNKNOWN
            },
            this,
        )
        is HttpRequestException, is HttpRequestTimeoutException, is IOException ->
            RoomFailure(RoomFailure.Reason.NETWORK, this)
        else -> RoomFailure(RoomFailure.Reason.UNKNOWN, this)
    }

    @Serializable
    private data class RoomSummaryDto(
        val id: String,
        val code: String,
        @SerialName("host_name") val hostName: String,
        @SerialName("max_players") val maxPlayers: Int,
        @SerialName("player_count") val playerCount: Int,
    )

    @Serializable
    private data class RoomDto(
        val id: String,
        val code: String,
        val game: String,
        @SerialName("host_id") val hostId: String,
        @SerialName("max_players") val maxPlayers: Int,
        val status: String,
        val members: List<MemberDto> = emptyList(),
    )

    @Serializable
    private data class MemberDto(
        @SerialName("user_id") val userId: String,
        val username: String,
        val status: String,
        val seat: Int? = null,
    )

    @Serializable
    private data class EventDto(
        val seq: Int,
        @SerialName("user_id") val userId: String,
        val type: String,
        val payload: JsonObject,
    )

    private companion object {
        const val ROOMS = "rooms"
        const val MEMBERS = "room_members"
        const val EVENTS = "game_events"
    }
}
