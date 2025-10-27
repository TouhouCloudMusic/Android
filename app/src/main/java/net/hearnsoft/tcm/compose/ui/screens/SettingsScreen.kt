package net.hearnsoft.tcm.compose.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemPopup
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.pref.PlayerCoverType
import net.hearnsoft.tcm.compose.pref.PlayerSeekToPreviousAction
import net.hearnsoft.tcm.compose.pref.SettingsDataStore

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    // 设置界面内容

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsDataStore = remember { SettingsDataStore(context) }
    val isAppDynamicColorEnabled by settingsDataStore.appDynamicColorEnabled.collectAsState(initial = false)
    val isPlayerSquigglyWaveEnabled by settingsDataStore.isPlayerSquigglyWaveEnabled.collectAsState(initial = true)
    val isPlayerShowMusicTagsEnabled by settingsDataStore.isPlayerShowMusicTagsEnabled.collectAsState(initial = true)
    val playerCoverType by settingsDataStore.playerCoverType
        .collectAsState(initial = PlayerCoverType.DEFAULT.ordinal)

    val playerCoverTypeLabels = listOf(
        stringResource(R.string.settings_cover_square),
        stringResource(R.string.settings_cover_circle)
    )

    val seekToPreviousActionLabels = listOf(
        stringResource(R.string.settings_action_default),
        stringResource(R.string.settings_action_previous),
        stringResource(R.string.settings_action_restart)
    )

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            val popupState = rememberPopupState()
            val isAndroid12OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ItemOuterTitle(text = stringResource(R.string.settings_ui_title))
            RoundedColumn {
                ItemSwitcher(
                    text = stringResource(R.string.settings_dynamic_color),
                    state = isAppDynamicColorEnabled,
                    enabled = isAndroid12OrAbove,
                    onChange = { state ->
                        coroutineScope.launch {
                            settingsDataStore.setAppDynamicColorEnabled(state)
                        }
                    }
                )
                ItemSwitcher(
                    text = stringResource(R.string.settings_player_wave),
                    state = isPlayerSquigglyWaveEnabled,
                    onChange = { state ->
                        coroutineScope.launch {
                            settingsDataStore.setPlayerSquigglyWaveEnabled(state)
                        }
                    }
                )
                ItemSwitcher(
                    text = stringResource(R.string.settings_show_tags),
                    state = isPlayerShowMusicTagsEnabled,
                    onChange = { state ->
                        coroutineScope.launch {
                            settingsDataStore.setPlayerShowMusicTagsEnabled(state)
                        }
                    },
                )
                ItemPopup(
                    state = popupState,
                    text = stringResource(R.string.settings_player_cover_type),
                    sub = playerCoverTypeLabels[playerCoverType]
                ) {
                    playerCoverTypeLabels.forEachIndexed { index, label ->
                        PopupMenuItem(
                            text = label,
                            selected = playerCoverType == index,
                            onClick = {
                                coroutineScope.launch {
                                    settingsDataStore.setPlayerCoverType(
                                        PlayerCoverType.entries[index]
                                    )
                                }
                                popupState.dismiss()
                            }
                        )
                    }
                }
            }
            ItemOuterTitle(text = stringResource(R.string.settings_player_behavior))
            RoundedColumn {
                val popupState = rememberPopupState()
                // 上一曲行为的设置
                val currentAction by settingsDataStore.playerSeekToPreviousAction
                    .collectAsState(initial = PlayerSeekToPreviousAction.DEFAULT.ordinal)
                ItemPopup(
                    state = popupState,
                    text = stringResource(R.string.settings_seek_previous_action),
                    sub = seekToPreviousActionLabels[currentAction]
                ) {
                    seekToPreviousActionLabels.forEachIndexed { index, label ->
                        PopupMenuItem(
                            text = label,
                            selected = currentAction == index,
                            onClick = {
                                coroutineScope.launch {
                                    setPlayerSeekToPreviousAction(
                                        dataStore = settingsDataStore,
                                        action = PlayerSeekToPreviousAction.entries[index]
                                    )
                                }
                                popupState.dismiss()
                            }
                        )
                    }
                }
            }
        }
    }

}

private suspend fun setPlayerSeekToPreviousAction(
    dataStore: SettingsDataStore,
    action: PlayerSeekToPreviousAction
) {
    dataStore.setPlayerSeekToPreviousAction(action)
}