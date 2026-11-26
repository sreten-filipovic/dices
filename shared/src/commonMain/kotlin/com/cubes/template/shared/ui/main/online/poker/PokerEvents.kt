package com.cubes.template.shared.ui.main.online.poker

import com.cubes.template.shared.data.online.GameEventRecord
import com.cubes.template.shared.domain.game.PokerAction
import com.cubes.template.shared.domain.game.PokerEvent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

fun GameEventRecord.toPokerEvent(seats: List<String>): PokerEvent {
    val action = runCatching {
        when (type) {
            "dice" -> PokerAction.Roll(payload.ints("values"))
            "reroll" -> PokerAction.Reroll(payload.ints("indices").toSet(), payload.ints("values"))
            "action" -> when (payload["name"]?.jsonPrimitive?.contentOrNull) {
                "keep" -> PokerAction.Keep
                else -> PokerAction.Unknown
            }
            "leave" -> PokerAction.Leave
            "timeout" -> PokerAction.Timeout(payload.ints("rolls"))
            else -> PokerAction.Unknown
        }
    }.getOrDefault(PokerAction.Unknown)
    return PokerEvent(seq, seats.indexOf(userId), action)
}

private fun JsonObject.ints(key: String): List<Int> = getValue(key).jsonArray.map { it.jsonPrimitive.int }
