package com.cubes.template.shared.data.auth

class AuthFailure(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {

    enum class Reason {
        INVALID_CREDENTIALS,
        EMAIL_TAKEN,
        USERNAME_TAKEN,
        WEAK_PASSWORD,
        EMAIL_NOT_CONFIRMED,
        NOT_SIGNED_IN,
        NETWORK,
        UNKNOWN,
    }
}
