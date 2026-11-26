package com.cubes.template.shared.data.local

import com.cubes.template.shared.data.auth.User
import kotlinx.serialization.json.Json

class LocalDataSource(
    private val dataStoreRepository: DataStoreRepository,
) {

    fun isOnboardingDone(): Boolean = dataStoreRepository.isOnboardingDone()
    fun setOnboardingDone(done: Boolean) = dataStoreRepository.setOnboardingDone(done)

    fun getLastOpenedDemoItemId(): String? = dataStoreRepository.getLastOpenedDemoItemId()
    fun saveLastOpenedDemoItemId(id: String) = dataStoreRepository.saveLastOpenedDemoItemId(id)

    fun getPlayerDieStyles(): List<String> = dataStoreRepository.getPlayerDieStyles()
    fun savePlayerDieStyles(styles: List<String>) = dataStoreRepository.savePlayerDieStyles(styles)

    fun getPlayerPawnColors(): List<String> = dataStoreRepository.getPlayerPawnColors()
    fun savePlayerPawnColors(colors: List<String>) = dataStoreRepository.savePlayerPawnColors(colors)

    fun getPlayerNames(): List<String> = dataStoreRepository.getPlayerNames()
    fun savePlayerNames(names: List<String>) = dataStoreRepository.savePlayerNames(names)

    fun getThemeMode(): String? = dataStoreRepository.getThemeMode()
    fun saveThemeMode(mode: String) = dataStoreRepository.saveThemeMode(mode)

    fun getUser(): User? = dataStoreRepository.getUserJson()?.let { json ->
        runCatching { Json.decodeFromString<User>(json) }.getOrNull()
    }

    fun saveUser(user: User) = dataStoreRepository.saveUserJson(Json.encodeToString(user))
    fun clearUser() = dataStoreRepository.clearUser()
}
