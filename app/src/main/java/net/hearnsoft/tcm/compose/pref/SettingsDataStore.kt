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
        /*val PREDICTIVE_BACK_GESTURE_ENABLED = booleanPreferencesKey("predictive_back_gesture_enabled")*/
    }

    /*val isPredictiveBackGestureEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            // 默认启用
            preferences[PREDICTIVE_BACK_GESTURE_ENABLED] ?: true
        }

    suspend fun setPredictiveBackGestureEnabled(isEnabled: Boolean) {
        dataStore.edit { settings ->
            settings[PREDICTIVE_BACK_GESTURE_ENABLED] = isEnabled
        }
    }*/
}