package com.cubes.template.shared.data.local

/**
 * Facade over all local storages (preferences, token storage, cache...).
 * Use delegation when adding more storages: `class LocalDataSource(...) : TokenStorage by tokenStorage`.
 */
class LocalDataSource(
    private val dataStoreRepository: DataStoreRepository,
) {

    fun isOnboardingDone(): Boolean = dataStoreRepository.isOnboardingDone()
    fun setOnboardingDone(done: Boolean) = dataStoreRepository.setOnboardingDone(done)

    fun getLastOpenedDemoItemId(): String? = dataStoreRepository.getLastOpenedDemoItemId()
    fun saveLastOpenedDemoItemId(id: String) = dataStoreRepository.saveLastOpenedDemoItemId(id)
}
