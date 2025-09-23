package net.hearnsoft.tcm.compose.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import net.hearnsoft.tcm.compose.utils.Logger
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        // 用户界面
        val PLAYER_SQUIGGLY_WAVE_ENABLED = booleanPreferencesKey("player_squiggly_wave_enabled")
        val PLAYER_SHOW_MUSIC_TAGS_ENABLED = booleanPreferencesKey("player_show_music_tags_enabled")
        val PLAYER_COVER_TYPE = intPreferencesKey("player_cover_type")

        // 播放器行为
        val PLAYER_SEEK_TO_PREVIOUS_ACTION = intPreferencesKey("player_seek_to_previous_action")
    }

    // 读取设置项目
    // 用户界面的设置
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

    val playerCoverType: Flow<Int> = dataStore.data
        .map { preferences ->
            // 默认值为 0，即方形封面
            preferences[PLAYER_COVER_TYPE] ?: 0
        }

    // 播放器行为的设置
    val playerSeekToPreviousAction: Flow<Int> = dataStore.data.catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            // 默认值为 0，即 Media3 默认行为
            preferences[PLAYER_SEEK_TO_PREVIOUS_ACTION] ?: PlayerSeekToPreviousAction.DEFAULT.ordinal
        }


    // 保存设置
    // 用户界面的设置
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

    suspend fun setPlayerCoverType(type: PlayerCoverType) {
        dataStore.edit { settings ->
            settings[PLAYER_COVER_TYPE] = type.ordinal
        }
    }

    // 播放器行为的设置
    suspend fun setPlayerSeekToPreviousAction(action: PlayerSeekToPreviousAction) {
        Logger.debug("SettingsDataStore", "setPlayerSeekToPreviousAction: ${action.name}")
        dataStore.edit { settings ->
            settings[PLAYER_SEEK_TO_PREVIOUS_ACTION] = action.ordinal
        }
    }


}