package net.hearnsoft.tcm.compose.ui.screens

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
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemPopup
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import kotlinx.coroutines.launch
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
    val isPlayerSquigglyWaveEnabled by settingsDataStore.isPlayerSquigglyWaveEnabled.collectAsState(initial = true)
    val isPlayerShowMusicTagsEnabled by settingsDataStore.isPlayerShowMusicTagsEnabled.collectAsState(initial = true)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ItemOuterTitle(text = "用户界面")
            RoundedColumn {
                ItemSwitcher(
                    text = "启用播放器进度条波形动画",
                    state = isPlayerSquigglyWaveEnabled,
                    onChange = { state ->
                        coroutineScope.launch {
                            settingsDataStore.setPlayerSquigglyWaveEnabled(state)
                        }
                    }
                )
                ItemSwitcher(
                    text = "播放器界面显示当前媒体标签",
                    state = isPlayerShowMusicTagsEnabled,
                    onChange = { state ->
                        coroutineScope.launch {
                            settingsDataStore.setPlayerShowMusicTagsEnabled(state)
                        }
                    },
                )
            }
            ItemOuterTitle(text = "播放器行为")
            RoundedColumn {
                val popupState = rememberPopupState()
                ItemPopup(
                    state = popupState,
                    text = "上一曲行为",
                    sub = "点击“上一曲”按钮时的行为"
                ) {
                    val currentAction by settingsDataStore.playerSeekToPreviousAction.collectAsState(initial = PlayerSeekToPreviousAction.DEFAULT.ordinal)
                    PopupMenuItem(
                        text = "默认",
                        onClick = {
                            coroutineScope.launch {
                                setPlayerSeekToPreviousAction(settingsDataStore,
                                    PlayerSeekToPreviousAction.DEFAULT)
                            }
                            popupState.dismiss()
                        },
                        selected = currentAction == PlayerSeekToPreviousAction.DEFAULT.ordinal
                    )
                    PopupMenuItem(
                        text = "上一首",
                        onClick = {
                            coroutineScope.launch {
                                setPlayerSeekToPreviousAction(settingsDataStore,
                                    PlayerSeekToPreviousAction.ALWAYS_PREVIOUS)
                            }
                            popupState.dismiss()
                        },
                        selected = currentAction == PlayerSeekToPreviousAction.ALWAYS_PREVIOUS.ordinal
                    )
                    PopupMenuItem(
                        text = "回到开头",
                        onClick = {
                            coroutineScope.launch {
                                setPlayerSeekToPreviousAction(settingsDataStore,
                                    PlayerSeekToPreviousAction.ALWAYS_RESTART)
                            }
                            popupState.dismiss()
                        },
                        selected = currentAction == PlayerSeekToPreviousAction.ALWAYS_RESTART.ordinal
                    )
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