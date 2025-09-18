package net.hearnsoft.tcm.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
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
