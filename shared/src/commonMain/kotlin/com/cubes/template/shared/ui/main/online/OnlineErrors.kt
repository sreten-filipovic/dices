package com.cubes.template.shared.ui.main.online

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.online.RoomFailure
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.auth_error_network
import com.cubes.template.shared.resources.auth_error_not_signed_in
import com.cubes.template.shared.resources.auth_error_unknown
import com.cubes.template.shared.resources.online_error_not_allowed
import com.cubes.template.shared.resources.online_error_not_enough_players
import com.cubes.template.shared.resources.online_error_rejected
import com.cubes.template.shared.resources.online_error_room_full
import com.cubes.template.shared.resources.online_error_room_not_found

val Throwable.roomFailureReason: RoomFailure.Reason
    get() = (this as? RoomFailure)?.reason ?: RoomFailure.Reason.UNKNOWN

fun StringProvider.roomErrorMessage(error: Throwable): String = getString(
    when (error.roomFailureReason) {
        RoomFailure.Reason.ROOM_NOT_FOUND -> Res.string.online_error_room_not_found
        RoomFailure.Reason.ROOM_FULL -> Res.string.online_error_room_full
        RoomFailure.Reason.REQUEST_REJECTED -> Res.string.online_error_rejected
        RoomFailure.Reason.NOT_ENOUGH_PLAYERS -> Res.string.online_error_not_enough_players
        RoomFailure.Reason.NOT_ALLOWED -> Res.string.online_error_not_allowed
        RoomFailure.Reason.NOT_SIGNED_IN -> Res.string.auth_error_not_signed_in
        RoomFailure.Reason.NETWORK -> Res.string.auth_error_network
        RoomFailure.Reason.TOO_EARLY, RoomFailure.Reason.UNKNOWN -> Res.string.auth_error_unknown
    }
)
