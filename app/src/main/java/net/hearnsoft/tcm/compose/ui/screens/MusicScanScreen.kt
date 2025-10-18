package net.hearnsoft.tcm.compose.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonType
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.outerPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.pref.SettingsDataStore
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableSaltUiApi
@UnstableApi
@Composable
fun MusicScanScreen(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val scanProgress by playerViewModel.scanProgress.collectAsState()
    val scanCompleted by playerViewModel.scanCompleted.collectAsState()

    // 设置项目读取
    val settingsDataStore = remember { SettingsDataStore(context) }
    val musicScanNotInclude60sMedia by settingsDataStore.isMusicScanNotInclude60sMedia.collectAsState(initial = false)

    var showDialog by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {

        if (showDialog) {
            val dialogContent = when {
                scanProgress == null && !scanCompleted -> "正在准备扫描音乐库，请稍候..."
                scanProgress != null -> scanProgress!!
                else -> "扫描完成！"
            }

            ScanDialog(
                title = "正在扫描音乐",
                content = dialogContent,
                confirmText = "确定",
                onDismissRequest = {
                    showDialog = false
                    // 只在对话框关闭时重置扫描状态
                    if (scanCompleted) {
                        playerViewModel.resetScanCompleted()
                    }
                },
                dialogButtonEnabled = scanCompleted,
            )
        }


        RoundedColumn {
            Item(
                text = "扫描音乐",
                onClick = {
                    if (checkHasPermission(context)) {
                        showDialog = true
                        playerViewModel.scanAndUpdateMusicLibrary()
                    } else {
                        grantScanPermission(
                            context = context,
                            onGranted = {
                                showDialog = true
                                playerViewModel.scanAndUpdateMusicLibrary()
                            },
                            onDenied = {
                                Toast.makeText(
                                    context,
                                    "未授予存储权限，无法扫描音乐",
                                    Toast.LENGTH_SHORT
                                ).show()
                                goToAppPermissionSettings(context)
                            }
                        )
                    }
                },
                arrowType = ItemArrowType.None,
                iconPainter = painterResource(R.drawable.ic_sync_24px),
                enabled = scanProgress == null && !scanCompleted
            )
        }

        ItemOuterTitle("扫描选项")
        RoundedColumn {
            ItemSwitcher(
                text = "不扫描60秒以下的媒体文件",
                state = musicScanNotInclude60sMedia,
                onChange = { checked ->
                    coroutineScope.launch {
                        settingsDataStore.setMusicScanNotInclude60sMedia(checked)
                    }
                }
            )
        }
    }

}

@Composable
private fun ScanDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: String,
    confirmText: String,
    dialogButtonEnabled: Boolean = true
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dialogButtonEnabled,
            dismissOnClickOutside = dialogButtonEnabled
        )
    ) {
        DialogTitle(text = title)
        ItemOuterTip(text = content)
        Button(onClick = {
            onDismissRequest()
        },
            text = confirmText,
            modifier = Modifier
                .fillMaxWidth()
                .outerPadding(),
            enabled = dialogButtonEnabled,
            type = if (dialogButtonEnabled)
                ButtonType.Highlight else ButtonType.Sub
        )
    }
}

private fun checkHasPermission(context: Context) : Boolean {
    return XXPermissions.isGrantedPermission(
        context,
        PermissionLists.getReadMediaAudioPermission()
    )
}

private fun grantScanPermission(
    context: Context,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    XXPermissions.with(context)
        .permission(PermissionLists.getReadMediaAudioPermission())
        .request { _, deniedList ->
            if (deniedList.isNotEmpty()) {
                onDenied()
            } else {
                onGranted()
            }
        }
}

private fun goToAppPermissionSettings(context: Context) {
    XXPermissions.startPermissionActivity(context, PermissionLists.getReadMediaAudioPermission())
}