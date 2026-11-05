package com.cubes.template.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Typed access to DataStore preferences. Add one key + getter/setter pair per stored value.
 */
class DataStoreRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val lastOpenedDemoItemId = stringPreferencesKey("last_opened_demo_item_id")
    }

    fun isOnboardingDone(): Boolean = runBlocking {
        dataStore.data.first()[Keys.onboardingDone] ?: false
    }

    fun setOnboardingDone(done: Boolean) = runBlocking {
        dataStore.edit { it[Keys.onboardingDone] = done }
    }

    fun getLastOpenedDemoItemId(): String? = runBlocking {
        dataStore.data.first()[Keys.lastOpenedDemoItemId]
    }

    fun saveLastOpenedDemoItemId(id: String) = runBlocking {
        dataStore.edit { it[Keys.lastOpenedDemoItemId] = id }
    }
}
