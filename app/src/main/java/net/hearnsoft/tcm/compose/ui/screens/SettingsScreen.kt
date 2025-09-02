package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    // 设置界面内容

    Box(Modifier.fillMaxSize()) {
        Text(text = "wip")
    }

}