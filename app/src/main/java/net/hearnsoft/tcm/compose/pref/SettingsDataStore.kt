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
        val PLAYER_SHOW_MUSIC_TAGS_ENABLED = booleanPreferencesKey("player_show_music_tags_enabled")
    }

    // 读取设置项目
    val isPlayerSquigglyWaveEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            // 默认启用
            preferences[PLAYER_SQUIGGLY_WAVE_ENABLED] ?: true
        }

    val isPlayerShowMusicTagsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            // 默认启用
            preferences[PLAYER_SHOW_MUSIC_TAGS_ENABLED] ?: true
        }


    // 保存设置
    suspend fun setPlayerSquigglyWaveEnabled(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[PLAYER_SQUIGGLY_WAVE_ENABLED] = isEnabled
        }
    }

    suspend fun setPlayerShowMusicTagsEnabled(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[PLAYER_SHOW_MUSIC_TAGS_ENABLED] = isEnabled
        }
    }


}