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
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.launch
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
        }
    }

}