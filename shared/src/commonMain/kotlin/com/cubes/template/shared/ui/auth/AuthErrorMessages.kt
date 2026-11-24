package com.cubes.template.shared.ui.auth

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.data.auth.AuthFailure
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.auth_error_email_not_confirmed
import com.cubes.template.shared.resources.auth_error_email_taken
import com.cubes.template.shared.resources.auth_error_invalid_credentials
import com.cubes.template.shared.resources.auth_error_network
import com.cubes.template.shared.resources.auth_error_invalid_code
import com.cubes.template.shared.resources.auth_error_not_signed_in
import com.cubes.template.shared.resources.auth_error_same_password
import com.cubes.template.shared.resources.auth_error_too_many_requests
import com.cubes.template.shared.resources.auth_error_wrong_current_password
import com.cubes.template.shared.resources.auth_error_unknown
import com.cubes.template.shared.resources.auth_error_username_taken
import com.cubes.template.shared.resources.auth_error_weak_password

val Throwable.authFailureReason: AuthFailure.Reason
    get() = (this as? AuthFailure)?.reason ?: AuthFailure.Reason.UNKNOWN

fun StringProvider.authErrorMessage(error: Throwable): String = getString(
    when (error.authFailureReason) {
        AuthFailure.Reason.INVALID_CREDENTIALS -> Res.string.auth_error_invalid_credentials
        AuthFailure.Reason.EMAIL_TAKEN -> Res.string.auth_error_email_taken
        AuthFailure.Reason.USERNAME_TAKEN -> Res.string.auth_error_username_taken
        AuthFailure.Reason.WEAK_PASSWORD -> Res.string.auth_error_weak_password
        AuthFailure.Reason.EMAIL_NOT_CONFIRMED -> Res.string.auth_error_email_not_confirmed
        AuthFailure.Reason.NOT_SIGNED_IN -> Res.string.auth_error_not_signed_in
        AuthFailure.Reason.WRONG_CURRENT_PASSWORD -> Res.string.auth_error_wrong_current_password
        AuthFailure.Reason.SAME_PASSWORD -> Res.string.auth_error_same_password
        AuthFailure.Reason.INVALID_CODE -> Res.string.auth_error_invalid_code
        AuthFailure.Reason.TOO_MANY_REQUESTS -> Res.string.auth_error_too_many_requests
        AuthFailure.Reason.NETWORK -> Res.string.auth_error_network
        AuthFailure.Reason.UNKNOWN -> Res.string.auth_error_unknown
    }
)
