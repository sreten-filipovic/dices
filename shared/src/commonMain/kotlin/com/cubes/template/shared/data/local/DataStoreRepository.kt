package com.cubes.template.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val lastOpenedDemoItemId = stringPreferencesKey("last_opened_demo_item_id")
        val playerDieStyles = stringPreferencesKey("player_die_styles")
        val playerNames = stringPreferencesKey("player_names")
        val playerPawnColors = stringPreferencesKey("player_pawn_colors")
        val userName = stringPreferencesKey("user_name")
        val userEmail = stringPreferencesKey("user_email")
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

    fun getPlayerDieStyles(): List<String> = runBlocking {
        dataStore.data.first()[Keys.playerDieStyles]?.split(SEPARATOR)?.filter { it.isNotEmpty() } ?: emptyList()
    }

    fun savePlayerDieStyles(styles: List<String>) = runBlocking {
        dataStore.edit { it[Keys.playerDieStyles] = styles.joinToString(SEPARATOR) }
    }

    fun getPlayerPawnColors(): List<String> = runBlocking {
        dataStore.data.first()[Keys.playerPawnColors]?.split(SEPARATOR) ?: emptyList()
    }

    fun savePlayerPawnColors(colors: List<String>) = runBlocking {
        dataStore.edit { it[Keys.playerPawnColors] = colors.joinToString(SEPARATOR) }
    }

    fun getPlayerNames(): List<String> = runBlocking {
        dataStore.data.first()[Keys.playerNames]?.split(NAME_SEPARATOR) ?: emptyList()
    }

    fun savePlayerNames(names: List<String>) = runBlocking {
        dataStore.edit { it[Keys.playerNames] = names.joinToString(NAME_SEPARATOR) { name -> name.replace(NAME_SEPARATOR, " ") } }
    }

    /** Returns the saved username and email, or null when nobody is signed in. */
    fun getUser(): Pair<String, String>? = runBlocking {
        val prefs = dataStore.data.first()
        val name = prefs[Keys.userName] ?: return@runBlocking null
        val email = prefs[Keys.userEmail] ?: return@runBlocking null
        name to email
    }

    fun saveUser(username: String, email: String) = runBlocking {
        dataStore.edit {
            it[Keys.userName] = username
            it[Keys.userEmail] = email
        }
    }

    fun clearUser() = runBlocking {
        dataStore.edit {
            it.remove(Keys.userName)
            it.remove(Keys.userEmail)
        }
    }

    private companion object {
        const val SEPARATOR = ","
        const val NAME_SEPARATOR = "\n"
    }
}
