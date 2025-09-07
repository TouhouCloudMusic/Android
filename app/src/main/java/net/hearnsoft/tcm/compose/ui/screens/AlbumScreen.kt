package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.ui.theme.Theme
import net.hearnsoft.tcm.compose.ui.uicomponent.sheet.SongActionSheetDialog
import net.hearnsoft.tcm.compose.ui.viewmodel.AlbumViewModel
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@UnstableApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
@UnstableSaltUiApi
fun AlbumScreen(
    modifier: Modifier = Modifier,
    albumId: Long,
    navController: NavController,
    albumViewModel: AlbumViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val album by albumViewModel.currentAlbum.collectAsState()
    val albumSongs by albumViewModel.albumSongs.collectAsState()
    val isLoading by albumViewModel.isLoading.collectAsState()

    val currentPlaying = playerViewModel.currentMediaItem.collectAsState().value

    var showActionDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongEntity?>(null) }

    LaunchedEffect(albumId) {
        albumViewModel.loadAlbum(albumId)
        albumViewModel.loadAlbumSongs(albumId)
    }

    if (showActionDialog) {
        selectedSong?.let {
            SongActionSheetDialog(
                onDismissRequest = {
                    showActionDialog = false
                },
                songEntity = it,
                navController = navController
            )
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        album?.let { albumEntity ->
            // 按碟号分组
            val groupedSongs = albumSongs.groupBy { it.discNumber }
            val hasMultipleDiscs = groupedSongs.size > 1 || groupedSongs.keys.any { it != null }

            LazyColumn(
                modifier = modifier.fillMaxSize().padding(horizontal = 8.dp)
            ) {
                item {
                    AlbumHeader(album = albumEntity)
                }

                if (hasMultipleDiscs) {
                    // 显示分碟列表
                    groupedSongs.toSortedMap(compareBy<Int?> { it ?: Int.MAX_VALUE }).forEach { (discNumber, songs) ->
                        item {
                            DiscHeader(discNumber = discNumber)
                        }

                        items(
                            items = songs,
                            key = { "${it.discNumber}_${it.songId}" }
                        ) { song ->
                            AlbumSongItem(
                                songEntity = song,
                                currentPlaying = currentPlaying,
                                onClick = {
                                    playerViewModel.playSong(song)
                                },
                                onActionClick = {
                                    showActionDialog = true
                                    selectedSong = song
                                }
                            )
                        }
                    }
                } else {
                    // 不分碟显示
                    items(
                        items = albumSongs,
                        key = { it.songId }
                    ) { song ->
                        AlbumSongItem(
                            songEntity = song,
                            currentPlaying = currentPlaying,
                            onClick = {
                                playerViewModel.playSong(song)
                            },
                            onActionClick = {
                                showActionDialog = true
                                selectedSong = song
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@UnstableSaltUiApi
fun DiscHeader(discNumber: Int?) {
    val discText = when (discNumber) {
        null -> "未定义碟号"
        else -> "Disc $discNumber"
    }

    Text(
        text = discText,
        style = SaltTheme.textStyles.main,
        color = SaltTheme.colors.highlight,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
    )
}


@Composable
fun AlbumHeader(album: AlbumEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        val subTitle = if (album.albumYear == null || album.albumYear <= 0) {
            "${album.songCount} 首歌曲"
        } else {
            "${album.albumYear}年 - ${album.songCount}首"
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(album.artworkUri)
                .crossfade(true)
                .placeholder(R.drawable.ic_album_24px)
                .build(),
            contentDescription = album.albumName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterVertically)
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = album.albumName,
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.text,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = album.albumArtist,
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.subText,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = subTitle,
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.subText
            )
        }
    }
}

@Composable
@UnstableSaltUiApi
fun AlbumSongItem(
    modifier: Modifier = Modifier,
    songEntity: SongEntity,
    currentPlaying: MediaItem? = null,
    onClick: () -> Unit = {},
    onActionClick: () -> Unit = {}
) {
    val isCurrentPlaying = currentPlaying?.mediaId == songEntity.mediaStoreId.toString()

    Row(
        modifier = modifier
            .background(SaltTheme.colors.background)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        // 显示音轨号或占位符
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = songEntity.trackNumber?.toString() ?: "-",
                style = SaltTheme.textStyles.main,
                color = if (isCurrentPlaying) Theme.colors.primary else SaltTheme.colors.text,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = songEntity.title ?: "未知歌曲",
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentPlaying) Theme.colors.primary else SaltTheme.colors.text
            )

            val artist = songEntity.artistName ?: "未知艺术家"
            Text(
                text = artist,
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentPlaying) Theme.colors.primary else SaltTheme.colors.subText
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(8.dp),
            onClick = { onActionClick() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert_24px),
                contentDescription = "More Options",
            )
        }
    }
}