package com.cubes.template.shared.ui.main.online.turn

import com.cubes.template.shared.data.online.GameEventRecord
import com.cubes.template.shared.domain.online.TurnAction
import com.cubes.template.shared.domain.online.TurnEvent
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

data class OnlinePlay(
    val myPlayer: Int,
    val secondsLeft: Int? = null,
    val submitting: Boolean = false,
)

const val REMATCH = "rematch"

fun GameEventRecord.toTurnEvent(seats: List<String>): TurnEvent {
    val action = runCatching {
        when (type) {
            "dice" -> TurnAction.Roll(payload.getValue("values").jsonArray.map { it.jsonPrimitive.int })
            "action" -> if (payload["name"]?.jsonPrimitive?.content == REMATCH) TurnAction.Rematch else TurnAction.EndTurn
            "leave" -> TurnAction.Leave
            "timeout" -> TurnAction.Timeout
            else -> TurnAction.Unknown
        }
    }.getOrDefault(TurnAction.Unknown)
    return TurnEvent(seq, seats.indexOf(userId), action)
}
