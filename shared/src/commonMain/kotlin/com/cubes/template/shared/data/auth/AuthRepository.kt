package com.cubes.template.shared.data.auth

import com.cubes.template.shared.data.remote.Result
import kotlinx.coroutines.delay
import kotlin.random.Random

interface AuthRepository {
    suspend fun restoreSession(cached: User?): User?
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(username: String, email: String, password: String): Result<User>
    suspend fun updateUsername(user: User, username: String): Result<User>
    suspend fun changePassword(user: User, currentPassword: String, newPassword: String): Result<Unit>
    suspend fun sendPasswordResetCode(email: String): Result<Unit>
    suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit>
    suspend fun setNewPassword(newPassword: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun deleteAccount(user: User): Result<Unit>
}

class PlaceholderAuthRepository : AuthRepository {

    private var resetEmail: String? = null

    override suspend fun restoreSession(cached: User?): User? = cached

    override suspend fun login(email: String, password: String): Result<User> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(User(id = fakeId(), username = email.substringBefore('@'), email = email))
    }

    override suspend fun register(username: String, email: String, password: String): Result<User> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(User(id = fakeId(), username = username, email = email))
    }

    override suspend fun updateUsername(user: User, username: String): Result<User> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(user.copy(username = username))
    }

    override suspend fun changePassword(user: User, currentPassword: String, newPassword: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(Unit)
    }

    override suspend fun sendPasswordResetCode(email: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        resetEmail = email
        return Result.Success(Unit)
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(Unit)
    }

    override suspend fun setNewPassword(newPassword: String): Result<User> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        val email = resetEmail.orEmpty()
        return Result.Success(User(id = fakeId(), username = email.substringBefore('@'), email = email))
    }

    override suspend fun logout(): Result<Unit> = Result.Success(Unit)

    override suspend fun deleteAccount(user: User): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MILLIS)
        return Result.Success(Unit)
    }

    private fun fakeId(): String = Random.nextLong().toULong().toString(16)

    private companion object {
        const val FAKE_NETWORK_DELAY_MILLIS = 800L
    }
}
