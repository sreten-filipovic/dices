package com.cubes.template.shared.data.auth

import com.cubes.template.shared.data.remote.Result
import kotlinx.coroutines.delay

data class User(
    val username: String,
    val email: String,
)

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(username: String, email: String, password: String): Result<User>
    suspend fun updateUsername(user: User, username: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun deleteAccount(user: User): Result<Unit>
}

/**
 * Stand-in until online multiplayer has a backend: accepts any credentials that passed validation
 * and never stores the password. Replace it with a real implementation in AppCompositionRoot.
 */
class PlaceholderAuthRepository : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(User(username = email.substringBefore('@'), email = email))
    }

    override suspend fun register(username: String, email: String, password: String): Result<User> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(User(username = username, email = email))
    }

    override suspend fun updateUsername(user: User, username: String): Result<User> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(user.copy(username = username))
    }

    override suspend fun logout(): Result<Unit> = Result.Success(Unit)

    override suspend fun deleteAccount(user: User): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(Unit)
    }

    private companion object {
        const val FAKE_NETWORK_DELAY_MILLIS = 800L
    }
}
