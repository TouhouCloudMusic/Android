package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.ItemInfo
import com.moriafly.salt.ui.ItemInfoType
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
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
        RoundedColumn {
            Item(
                text = "扫描音乐",
                onClick = {
                    playerViewModel.scanAndUpdateMusicLibrary()
                },
                arrowType = ItemArrowType.None,
                iconPainter = painterResource(R.drawable.ic_sync_24px),
                enabled = scanProgress == null && !scanCompleted
            )
        }

        AnimatedVisibility(
            visible = scanProgress != null
        ) {
            RoundedColumn {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = scanProgress ?: "正在处理...",
                        modifier = Modifier.padding(start = 12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = scanCompleted
        ) {
            RoundedColumn {
                ItemInfo(
                    text = "扫描完成",
                    infoType = ItemInfoType.Success
                )
            }
        }

    }

}