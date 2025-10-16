package net.hearnsoft.tcm.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.view.WindowCompat
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.moriafly.salt.ui.UnstableSaltUiApi
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.UnstableApi
import net.hearnsoft.tcm.compose.ui.theme.TouhouCloudMusicTheme
import net.hearnsoft.tcm.compose.ui.views.AppRootView
import net.hearnsoft.tcm.compose.utils.BitmapUtils

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
        // 应用结束时，清理所有临时文件
        BitmapUtils.cleanupTempFiles(this)
        super.onDestroy()
    }
}
