package net.hearnsoft.tcm.compose.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val PLAYER_SQUIGGLY_WAVE_ENABLED = booleanPreferencesKey("player_squiggly_wave_enabled")
    }

    val isPlayerSquigglyWaveEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            // 默认启用
            preferences[PLAYER_SQUIGGLY_WAVE_ENABLED] ?: true
        }

    suspend fun setPlayerSquigglyWaveEnabled(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[PLAYER_SQUIGGLY_WAVE_ENABLED] = isEnabled
        }
    }

}