package com.cubes.template.shared.data.remote

/**
 * Known backend error messages that the app wants to react to specifically.
 * Add project-specific entries here; everything else surfaces as a generic error.
 */
enum class ApiErrorMessage(val message: List<String>) {
    NotFound(listOf("NOT_FOUND")),
    InvalidData(listOf("Invalid data!"));

    companion object {
        fun find(message: String?) = entries.find { it.message.contains(message) }
    }
}

data class KnownApiException(
    val errorMsg: ApiErrorMessage,
    override val cause: Throwable? = null,
) : Exception()

fun Exception.ifKnownApiException(block: (ApiErrorMessage) -> Exception) =
    if (this is KnownApiException) block(this.errorMsg) else this
