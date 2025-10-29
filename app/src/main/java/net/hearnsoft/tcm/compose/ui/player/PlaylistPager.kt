package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.ItemDivider
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

@Composable
@UnstableApi
@UnstableSaltUiApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@SuppressLint("UnusedBoxWithConstraintsScope")
fun PlaylistPager(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    onCollapseTextClick: () -> Unit = {},
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
            title = stringResource(R.string.clear_playlist),
            content = stringResource(R.string.clear_playlist_confirm),
            onConfirm = {
                playerViewModel.clearPlaylist()
                showClearPlaylist = false
            },
            onDismissRequest = {
                showClearPlaylist = false
            },
            confirmText = stringResource(R.string.confirm),
            cancelText = stringResource(R.string.cancel)
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PlayerHorizontalPadding, vertical = PlayerCoverVerticalPadding)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = {
                        onCollapseTextClick()
                    })
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.collapse_player_hint),
                    textAlign = TextAlign.Center,
                    style = SaltTheme.textStyles.sub,
                    fontSize = 12.sp,
                    color = uiColor,
                    modifier = Modifier.weight(1f)
                        .fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            if (currentPlaying != null) {
                PlaylistNowPlayingHeader(
                    mediaItem = currentPlaying,
                    color = uiColor,
                    onClick = {
                        onCollapseTextClick()
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
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
                        color = uiColor.copy(0.7f)
                    )
                }

                Text(
                    text = stringResource(R.string.playlist_title),
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
                        tint = uiColor.copy(0.7f)
                    )
                }
            }
            ItemDivider(
                modifier = Modifier.fillMaxWidth(),
                color = uiColor.copy(alpha = 0.8f),
                startIndent = 0.dp
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
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
                                    text = stringResource(R.string.empty_playlist),
                                    style = SaltTheme.textStyles.main,
                                    color = uiColor
                                )
                                Text(
                                    text = stringResource(R.string.empty_playlist_description),
                                    style = SaltTheme.textStyles.sub,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = uiColor.copy(0.7f)
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
            Spacer(Modifier.height(1.dp))
            ItemDivider(
                modifier = Modifier.fillMaxWidth(),
                color = uiColor.copy(alpha = 0.8f),
                startIndent = 0.dp
            )
            Spacer(Modifier.height(1.dp))
        }
    }
}

@Composable
private fun PlaylistNowPlayingHeader(
    mediaItem: MediaItem?,
    color: Color,
    onClick: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = {
                onClick()
            })
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mediaItem?.mediaMetadata?.artworkUri)
                .crossfade(true)
                .placeholder(R.drawable.ic_nav_music)
                .build(),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp))
                .align(Alignment.CenterVertically),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = mediaItem?.mediaMetadata?.title.toString(),
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = color
            )

            val artist = mediaItem?.mediaMetadata?.artist.toString()
            val album = mediaItem?.mediaMetadata?.albumTitle.toString()

            val subTitle = "$artist - $album"
            Text(
                text = subTitle,
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = color.copy(alpha = 0.5f)
            )
        }
    }

}