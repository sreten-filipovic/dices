package com.cubes.template.shared.data.local

import com.cubes.template.shared.data.auth.User

class LocalDataSource(
    private val dataStoreRepository: DataStoreRepository,
) {

    fun isOnboardingDone(): Boolean = dataStoreRepository.isOnboardingDone()
    fun setOnboardingDone(done: Boolean) = dataStoreRepository.setOnboardingDone(done)

    fun getLastOpenedDemoItemId(): String? = dataStoreRepository.getLastOpenedDemoItemId()
    fun saveLastOpenedDemoItemId(id: String) = dataStoreRepository.saveLastOpenedDemoItemId(id)

    fun getPlayerDieStyles(): List<String> = dataStoreRepository.getPlayerDieStyles()
    fun savePlayerDieStyles(styles: List<String>) = dataStoreRepository.savePlayerDieStyles(styles)

    fun getPlayerNames(): List<String> = dataStoreRepository.getPlayerNames()
    fun savePlayerNames(names: List<String>) = dataStoreRepository.savePlayerNames(names)

    fun getUser(): User? = dataStoreRepository.getUser()?.let { (username, email) -> User(username, email) }
    fun saveUser(user: User) = dataStoreRepository.saveUser(user.username, user.email)
    fun clearUser() = dataStoreRepository.clearUser()
}
