package net.hearnsoft.tcm.compose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.moriafly.salt.ui.UnstableSaltUiApi
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.UnstableApi
import net.hearnsoft.tcm.compose.pref.SettingsDataStore
import net.hearnsoft.tcm.compose.service.MusicPlaybackService
import net.hearnsoft.tcm.compose.ui.theme.TouhouCloudMusicTheme
import net.hearnsoft.tcm.compose.ui.views.AppRootView
import net.hearnsoft.tcm.compose.utils.BitmapUtils

@OptIn(androidx.media3.common.util.UnstableApi::class)
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@AndroidEntryPoint
@UnstableApi
@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {

    private val appExitReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MusicPlaybackService.ACTION_EXIT_APP) {
                finishAffinity()
            }
        }
    }

    private val context: Context
        get() = this@MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        // 注册应用退出广播接收器
        val filter = android.content.IntentFilter().apply {
            addAction(MusicPlaybackService.ACTION_EXIT_APP)
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(appExitReceiver, filter)

        setContent {
            // 设置项目读取
            val settingsDataStore = remember { SettingsDataStore(context) }
            // 是否启用动态颜色
            val useDynamicColor = settingsDataStore.appDynamicColorEnabled.collectAsState(
                initial = false
            ).value

            TouhouCloudMusicTheme(
                useDynamicColor = useDynamicColor
            ) {
                AppRootView(
                    context = this@MainActivity
                )
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (!checkPermissions()) {
            XXPermissions.with(this@MainActivity)
                .permission(PermissionLists.getPostNotificationsPermission())
                .request { req, den ->
                    if (den.isNotEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "需要通知权限以便发送通知",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun checkPermissions() : Boolean {
        return XXPermissions.isGrantedPermission(
            this@MainActivity,
            PermissionLists.getPostNotificationsPermission()
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(appExitReceiver)
        // 应用结束时，清理所有临时文件
        BitmapUtils.cleanupTempFiles(this)
        super.onDestroy()
    }
}
