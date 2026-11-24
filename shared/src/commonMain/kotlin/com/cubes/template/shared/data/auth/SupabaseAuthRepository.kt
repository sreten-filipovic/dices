package com.cubes.template.shared.data.auth

import com.cubes.template.shared.data.remote.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class SupabaseAuthRepository(private val supabase: SupabaseClient) : AuthRepository {

    private val auth get() = supabase.auth

    override suspend fun restoreSession(cached: User?): User? {
        auth.awaitInitialization()
        val info = auth.currentUserOrNull() ?: return null
        return try {
            fetchUser(info)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            cached?.takeIf { it.id == info.id } ?: fallbackUser(info)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = attempt {
        auth.signInWith(Email, redirectUrl = null) {
            this.email = email
            this.password = password
        }
        fetchUser(currentUser())
    }

    override suspend fun register(username: String, email: String, password: String): Result<User> = attempt {
        if (!isUsernameAvailable(username)) throw AuthFailure(AuthFailure.Reason.USERNAME_TAKEN)
        auth.signUpWith(Email, redirectUrl = null) {
            this.email = email
            this.password = password
            data = buildJsonObject { put(USERNAME_METADATA, username) }
        }
        if (auth.currentSessionOrNull() == null) throw AuthFailure(AuthFailure.Reason.EMAIL_NOT_CONFIRMED)
        fetchUser(currentUser())
    }

    override suspend fun updateUsername(user: User, username: String): Result<User> = attempt {
        val changesOnlyCase = username.equals(user.username, ignoreCase = true)
        if (!changesOnlyCase && !isUsernameAvailable(username)) {
            throw AuthFailure(AuthFailure.Reason.USERNAME_TAKEN)
        }
        val profile = supabase.postgrest.from(PROFILES).update({ set("username", username) }) {
            select()
            filter { eq("id", user.id) }
        }.decodeSingle<ProfileDto>()
        profile.toUser(user.email)
    }

    override suspend fun logout(): Result<Unit> = attempt {
        try {
            auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            auth.clearSession()
        }
    }

    override suspend fun deleteAccount(user: User): Result<Unit> = attempt {
        supabase.functions.invoke(DELETE_ACCOUNT_FUNCTION)
        auth.clearSession()
    }

    private fun currentUser(): UserInfo =
        auth.currentUserOrNull() ?: throw AuthFailure(AuthFailure.Reason.NOT_SIGNED_IN)

    private suspend fun fetchUser(info: UserInfo): User =
        supabase.postgrest.from(PROFILES).select {
            filter { eq("id", info.id) }
        }.decodeSingle<ProfileDto>().toUser(info.email.orEmpty())

    private fun fallbackUser(info: UserInfo) = User(
        id = info.id,
        username = info.userMetadata?.get(USERNAME_METADATA)?.jsonPrimitive?.content.orEmpty(),
        email = info.email.orEmpty(),
    )

    private suspend fun isUsernameAvailable(username: String): Boolean =
        supabase.postgrest.rpc(
            "is_username_available",
            buildJsonObject { put("candidate", username) },
        ).decodeAs<Boolean>()

    private suspend fun <T : Any> attempt(block: suspend () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toAuthFailure())
    }

    private fun Exception.toAuthFailure(): AuthFailure = when (this) {
        is AuthFailure -> this
        is AuthWeakPasswordException -> AuthFailure(AuthFailure.Reason.WEAK_PASSWORD, this)
        is AuthRestException -> AuthFailure(
            when (errorCode) {
                AuthErrorCode.InvalidCredentials -> AuthFailure.Reason.INVALID_CREDENTIALS
                AuthErrorCode.EmailExists, AuthErrorCode.UserAlreadyExists -> AuthFailure.Reason.EMAIL_TAKEN
                AuthErrorCode.WeakPassword -> AuthFailure.Reason.WEAK_PASSWORD
                AuthErrorCode.EmailNotConfirmed -> AuthFailure.Reason.EMAIL_NOT_CONFIRMED
                else -> AuthFailure.Reason.UNKNOWN
            },
            this,
        )
        is PostgrestRestException -> AuthFailure(
            if (code == UNIQUE_VIOLATION) AuthFailure.Reason.USERNAME_TAKEN else AuthFailure.Reason.UNKNOWN,
            this,
        )
        is HttpRequestException, is HttpRequestTimeoutException, is IOException ->
            AuthFailure(AuthFailure.Reason.NETWORK, this)
        else -> AuthFailure(AuthFailure.Reason.UNKNOWN, this)
    }

    @Serializable
    private data class ProfileDto(
        val id: String,
        val username: String,
        @SerialName("owned_dice") val ownedDice: List<String> = emptyList(),
        @SerialName("owned_pawns") val ownedPawns: List<String> = emptyList(),
    ) {
        fun toUser(email: String) = User(
            id = id,
            username = username,
            email = email,
            ownedDice = ownedDice,
            ownedPawns = ownedPawns,
        )
    }

    private companion object {
        const val PROFILES = "profiles"
        const val DELETE_ACCOUNT_FUNCTION = "delete-account"
        const val USERNAME_METADATA = "username"
        const val UNIQUE_VIOLATION = "23505"
    }
}
