package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.YesNoDialog
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.constants.PlayerCoverVerticalPadding
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding
import net.hearnsoft.tcm.compose.ui.uicomponent.PlaylistItem
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerUIColor
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.utils.Logger

@Composable
@UnstableApi
@UnstableSaltUiApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@SuppressLint("UnusedBoxWithConstraintsScope")
fun PlaylistPager(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
) {
    val uiColor = LocalPlayerUIColor.current

    // 当前播放列表
    val playlist = playerViewModel.currentPlaylist.collectAsState().value
    // 当前播放的媒体
    val currentPlaying = playerViewModel.currentMediaItem.collectAsState().value
    // 获取当前播放的索引
    val currentPlayingIndex = playerViewModel.currentMediaItemIndex.collectAsState().value

    val listState = rememberLazyListState()
    val targetIndex = remember(playlist) {
        playlist.indexOfFirst { it.mediaId == currentPlaying?.mediaId }
    }

    var showClearPlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(targetIndex, listState) {
        if (targetIndex != -1) {
            // 如果当前播放的媒体在列表中，滚动到该项
            listState.scrollToItem(index = targetIndex, scrollOffset = 0)
        }
    }

    if (showClearPlaylist && playlist.isNotEmpty()) {
        YesNoDialog(
            title = "清空播放列表",
            content = "是否要清空当前播放列表？",
            onConfirm = {
                playerViewModel.clearPlaylist()
                showClearPlaylist = false
            },
            onDismissRequest = {
                showClearPlaylist = false
            },
            confirmText = "确定",
            cancelText = "取消"
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PlayerHorizontalPadding, vertical = PlayerCoverVerticalPadding)
            .sizeIn(maxHeight = 600.dp, maxWidth = 600.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentPlayingIndex >= 0) {
                            "${currentPlayingIndex + 1}/${playlist.size}"
                        } else {
                            "${playlist.size}"
                        },
                        style = SaltTheme.textStyles.sub,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        color = uiColor
                    )
                }

                Text(
                    text = "播放列表",
                    style = SaltTheme.textStyles.main,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    color = uiColor
                )

                IconButton(
                    onClick = {
                        showClearPlaylist = true
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .size(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_24px),
                        contentDescription = "clear playlist",
                        tint = uiColor
                    )
                }
            }
            RoundedColumn(
                modifier = Modifier
                    .fillMaxSize(),
                color = Color.Transparent
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    state = listState,
                ) {
                    if (playlist.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "暂无音乐",
                                        style = SaltTheme.textStyles.main,
                                        color = uiColor
                                    )
                                    Text(
                                        text = "添加媒体文件到播放列表",
                                        style = SaltTheme.textStyles.sub,
                                        modifier = Modifier.padding(top = 8.dp),
                                        color = uiColor
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = playlist,
                            key = { index, _ -> index }
                        ) { index, playlistItem ->
                            PlaylistItem(
                                currentPlaying = currentPlaying,
                                currentPlayingIndex = currentPlayingIndex,
                                itemIndex = index,
                                mediaItem = playlistItem,
                                onClick = {
                                    playerViewModel.playAtIndex(index)
                                },
                                onRemoveClick = {
                                    playerViewModel.removeFromPlaylist(it)
                                },
                                textColor = uiColor
                            )
                        }
                    }
                }
            }
        }
    }
}