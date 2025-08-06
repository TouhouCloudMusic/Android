package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.moriafly.salt.ui.Text
import net.hearnsoft.tcm.compose.ui.theme.TouhouCloudMusicTheme
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerAwareWindowInsets

@ExperimentalMaterial3Api
@Composable
fun MusicScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 顶部间距
        Spacer(
            Modifier.height(
                LocalPlayerAwareWindowInsets.current
                    .asPaddingValues()
                    .calculateTopPadding()
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            repeat(100) { index ->
                // 这里可以替换为实际的音乐列表项
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // 示例内容
                    Text(text = "Music Item #$index")
                }

            }
        }

        // 底部间距
        Spacer(
            Modifier.height(
                LocalPlayerAwareWindowInsets.current
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MusicScreenPreview() {
    TouhouCloudMusicTheme {
        MusicScreen(
            modifier = Modifier.fillMaxSize(),
            navController = NavController(context = LocalContext.current),
        )
    }
}