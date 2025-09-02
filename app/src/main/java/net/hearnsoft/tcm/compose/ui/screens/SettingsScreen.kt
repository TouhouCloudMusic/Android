package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.layout.Box
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
    /*val isPredictiveBackEnabled by settingsDataStore.isPredictiveBackGestureEnabled.collectAsState(initial = true)*/

    Box(Modifier.fillMaxSize()) {
        RoundedColumn(Modifier.fillMaxSize()) {
            ItemOuterTitle(text = "通用")
            /*ItemSwitcher(
                text = "启用预测性返回手势",
                state = isPredictiveBackEnabled,
                onChange = { enabled ->
                    coroutineScope.launch {
                        settingsDataStore.setPredictiveBackGestureEnabled(enabled)
                    }

                }
            )*/
        }
    }

}