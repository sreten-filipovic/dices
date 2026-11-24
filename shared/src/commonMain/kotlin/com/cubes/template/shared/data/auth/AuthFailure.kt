package com.cubes.template.shared.data.auth

class AuthFailure(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {

    enum class Reason {
        INVALID_CREDENTIALS,
        EMAIL_TAKEN,
        USERNAME_TAKEN,
        WEAK_PASSWORD,
        EMAIL_NOT_CONFIRMED,
        WRONG_CURRENT_PASSWORD,
        SAME_PASSWORD,
        INVALID_CODE,
        TOO_MANY_REQUESTS,
        NOT_SIGNED_IN,
        NETWORK,
        UNKNOWN,
    }
}
