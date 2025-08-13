package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.constants.PlayerCoverVerticalPadding
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding
import net.hearnsoft.tcm.compose.ui.uicomponent.PlaylistItem
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerBackgroundColor
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

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
    // 当前播放列表
    val playlist = playerViewModel.currentPlaylist.collectAsState().value
    // 当前播放的媒体
    val currentPlaying = playerViewModel.currentMediaItem.collectAsState().value

    val listState = rememberLazyListState()
    val targetIndex = remember(playlist) {
        playlist.indexOfFirst { it.mediaId == currentPlaying?.mediaId }
    }

    // 由LocalPlayerBackgroundColor提供颜色
    val backgroundColor = LocalPlayerBackgroundColor.current

    LaunchedEffect(targetIndex, listState) {
        if (targetIndex != -1) {
            // 如果当前播放的媒体在列表中，滚动到该项
            listState.scrollToItem(index = targetIndex, scrollOffset = 0)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PlayerHorizontalPadding, vertical = PlayerCoverVerticalPadding)
            .sizeIn(maxHeight = 600.dp, maxWidth = 600.dp)
    ) {
        RoundedColumn(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Text(
                text = "播放列表",
                style = SaltTheme.textStyles.main,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = backgroundColor
            )

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
                                    color = backgroundColor
                                )
                                Text(
                                    text = "添加媒体文件到播放列表",
                                    style = SaltTheme.textStyles.sub,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = backgroundColor
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = playlist,
                        key = { it.mediaId }
                    ) {playlistItem ->
                        PlaylistItem(
                            currentPlaying = currentPlaying,
                            mediaItem = playlistItem,
                            onClick = { playerViewModel.playSong(playlistItem) },
                            onRemoveClick = {
                                playerViewModel.removeFromPlaylist(it)
                            },
                            textColor = backgroundColor
                        )
                    }
                }
            }
        }
    }
}