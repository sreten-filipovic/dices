package com.cubes.template.shared.ui.main.online.ludo

import com.cubes.template.shared.data.online.GameEventRecord
import com.cubes.template.shared.domain.ludo.LudoAction
import com.cubes.template.shared.domain.ludo.LudoEvent
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private const val UNKNOWN_PAWN = -1

fun GameEventRecord.toLudoEvent(seats: List<String>): LudoEvent {
    val action = when (type) {
        "roll" -> LudoAction.Roll(payload.getValue("value").jsonPrimitive.int)
        "move" -> LudoAction.Move(payload.getValue("pawn").jsonPrimitive.int)
        "leave" -> LudoAction.Leave
        "timeout" -> LudoAction.Timeout(payload.getValue("rolls").jsonArray.map { it.jsonPrimitive.int })
        else -> LudoAction.Move(UNKNOWN_PAWN)
    }
    return LudoEvent(seq, seats.indexOf(userId), action)
}
