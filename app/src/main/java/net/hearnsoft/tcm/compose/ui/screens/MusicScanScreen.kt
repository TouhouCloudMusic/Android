package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.outerPadding
import kotlinx.coroutines.delay
import net.hearnsoft.tcm.compose.R
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

    val scanProgress by playerViewModel.scanProgress.collectAsState()
    val scanCompleted by playerViewModel.scanCompleted.collectAsState()

    LaunchedEffect(scanCompleted) {
        if (scanCompleted) {
            delay(3000) // 延时3秒
            playerViewModel.resetScanCompleted()
        }
    }

    Column(modifier.fillMaxSize()) {
        var showDialog by remember { mutableStateOf(false) }

        if (showDialog) {
            ScanDialog(
                title = "正在扫描音乐",
                content = if (scanProgress == null && !scanCompleted) {
                    "正在准备扫描音乐库，请稍候..."
                } else if (scanProgress != null) {
                    scanProgress!!
                } else run {
                    "扫描完成！"
                },
                confirmText = "确定",
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(
                    dismissOnClickOutside = false
                )
            )
        }


        RoundedColumn {
            Item(
                text = "扫描音乐",
                onClick = {
                    showDialog = true
                    playerViewModel.scanAndUpdateMusicLibrary()
                },
                arrowType = ItemArrowType.None,
                iconPainter = painterResource(R.drawable.ic_sync_24px),
                enabled = scanProgress == null && !scanCompleted
            )
        }

    }

}

@Composable
private fun ScanDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String,
    content: String,
    confirmText: String,
    dialogButtonEnabled: Boolean = true
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        DialogTitle(text = title)
        ItemOuterTip(text = content)
        TextButton(
            enabled = dialogButtonEnabled,
            onClick = {
                onDismissRequest()
            },
            modifier = Modifier
                .fillMaxWidth()
                .outerPadding(),
            text = confirmText
        )
    }
}