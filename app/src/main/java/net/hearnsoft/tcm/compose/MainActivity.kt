package net.hearnsoft.tcm.compose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.moriafly.salt.ui.UnstableSaltUiApi
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.UnstableApi
import net.hearnsoft.tcm.compose.ui.theme.TouhouCloudMusicTheme
import net.hearnsoft.tcm.compose.ui.views.AppRootView

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@AndroidEntryPoint
@UnstableApi
@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            TouhouCloudMusicTheme {
                AppRootView(
                    context = this@MainActivity
                )
            }
        }
    }
}

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@UnstableApi
@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    TouhouCloudMusicTheme {
        AppRootView(context = Activity())
    }
}