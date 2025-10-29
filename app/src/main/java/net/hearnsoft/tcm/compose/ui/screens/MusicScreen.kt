package net.hearnsoft.tcm.compose.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.ui.uicomponent.AlbumListItem
import net.hearnsoft.tcm.compose.ui.uicomponent.MusicListItem
import net.hearnsoft.tcm.compose.ui.uicomponent.sheet.AlbumSortSheetDialog
import net.hearnsoft.tcm.compose.ui.uicomponent.sheet.MusicSortSheetDialog
import net.hearnsoft.tcm.compose.ui.uicomponent.sheet.SongActionSheetDialog
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerAwareWindowInsets
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableApi
@Composable
fun MusicScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    // 收集 ViewModel 状态
    val allSongs by playerViewModel.allSongs.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()

    // 收集专辑数据
    val allAlbums by playerViewModel.allAlbums.collectAsState()
    val currentAlbumSortingRule = playerViewModel.currentAlbumSortingRule.collectAsState().value

    // 当前排序规则
    val currentSortingRule = playerViewModel.currentSongSortingRule.collectAsState().value

    // 对话框显示状态
    var showSongSortDialog by remember { mutableStateOf(false) }
    var showAlbumSortDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf(allSongs.firstOrNull()) }

    // 选择的音乐类型
    var selectedType by rememberSaveable { mutableStateOf(MusicType.SONG) }
    val typeList = listOf(
        MusicType.SONG,
        MusicType.ALBUM,
        MusicType.ARTIST,
        MusicType.FOLDER
    )

    // 歌曲排序对话框
    if (showSongSortDialog) {
        MusicSortSheetDialog(
            currentRule = currentSortingRule,
            onSortRuleSelected = { rule ->
                playerViewModel.updateSongSortingRule(rule)
                showSongSortDialog = false
            },
            onDismissRequest = {
                showSongSortDialog = false
            }
        )
    }
    // 专辑排序对话框
    if (showAlbumSortDialog) {
        AlbumSortSheetDialog(
            currentRule = currentAlbumSortingRule,
            onSortRuleSelected = { rule ->
                playerViewModel.updateAlbumSortingRule(rule)
                showAlbumSortDialog = false
            },
            onDismissRequest = {
                showAlbumSortDialog = false
            }
        )
    }

    if (showActionDialog) {
        selectedSong?.let {
            SongActionSheetDialog(
                onDismissRequest = {
                    showActionDialog = false
                },
                playerViewModel = playerViewModel,
                songEntity = it,
                navController = navController
            )
        }
    }

    // 顶部间距
    Spacer(
        Modifier.height(
            LocalPlayerAwareWindowInsets.current
                .asPaddingValues()
                .calculateTopPadding()
        )
    )
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 主界面内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                // 操作按钮行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 类型选择行
                    TabRow(
                        selectedTabIndex = typeList.indexOf(selectedType),
                        modifier = Modifier.weight(1f),
                        containerColor = SaltTheme.colors.background,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(it[typeList.indexOf(selectedType)]),
                                color = SaltTheme.colors.highlight
                            )
                        },
                        divider = { /* 不显示分隔线 */ },
                    ) {
                        LeadingIconTab(
                            selected = selectedType == MusicType.SONG,
                            onClick = { selectedType = MusicType.SONG },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_nav_music),
                                    contentDescription = stringResource(R.string.music_type_song),
                                )
                            },
                            text = {  },
                        )
                        LeadingIconTab(
                            selected = selectedType == MusicType.ALBUM,
                            onClick = { selectedType = MusicType.ALBUM },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_album_24px),
                                    contentDescription = stringResource(R.string.music_type_album),
                                )
                            },
                            text = {  }
                        )
                        LeadingIconTab(
                            selected = selectedType == MusicType.ARTIST,
                            onClick = { selectedType = MusicType.ARTIST },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_artist_24px),
                                    contentDescription = stringResource(R.string.music_type_artist),
                                )
                            },
                            text = {  }
                        )
                        LeadingIconTab(
                            selected = selectedType == MusicType.FOLDER,
                            onClick = { selectedType = MusicType.FOLDER },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_folder_24px),
                                    contentDescription = stringResource(R.string.music_type_folder),
                                )
                            },
                            text = {  }
                        )
                    }

                    IconButton(
                        onClick = {
                            when (selectedType) {
                                MusicType.SONG ->
                                    showSongSortDialog = true
                                MusicType.ALBUM ->
                                    showAlbumSortDialog = true
                                else -> {}
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sort_24px),
                            contentDescription = "排序"
                        )
                    }
                }

                // 歌曲/专辑数量显示
                Row(Modifier.fillMaxWidth().padding(6.dp)) {
                    when (selectedType) {
                        MusicType.SONG -> {
                            if (allSongs.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.total_songs, allSongs.size),
                                    style = SaltTheme.textStyles.sub
                                )
                            }
                        }
                        MusicType.ALBUM -> {
                            if (allAlbums.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.total_albums, allAlbums.size),
                                    style = SaltTheme.textStyles.sub
                                )
                            }
                        }
                        else -> {}
                    }
                }

                // 加载指示器
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearProgressIndicator(
                            color = SaltTheme.colors.highlight,
                            trackColor = SaltTheme.colors.subBackground
                        )
                    }
                }

                // 内容列表
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedType,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "contentTypeChange"
                    ) { type ->
                        when (type) {
                            MusicType.SONG -> {
                                MusicList(
                                    playerViewModel = playerViewModel,
                                    onActionClick = { songEntity ->
                                        showActionDialog = true
                                        selectedSong = songEntity
                                    }
                                )
                            }

                            MusicType.ALBUM -> {
                                AlbumList(
                                    navController = navController,
                                    allAlbums = allAlbums,
                                    isLoading = isLoading
                                )
                            }
                            // 其他类型待实现
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    item {
                                        Text(stringResource(R.string.feature_in_development))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 底部间距
        Spacer(
            Modifier.height(
                LocalPlayerAwareWindowInsets.current
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
        )
    }

}

@Composable
fun EmptyMusicList() {
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
            Image(
                painter = painterResource(R.drawable.no_item),
                contentDescription = "No Music",
                modifier = Modifier.size(120.dp).aspectRatio(1f)
            )
            Text(
                text = stringResource(R.string.empty_music_list),
                style = SaltTheme.textStyles.main
            )
            Text(
                text = stringResource(R.string.empty_music_description),
                style = SaltTheme.textStyles.sub,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@UnstableSaltUiApi
@UnstableApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun MusicList(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    onActionClick: (SongEntity) -> Unit = { }
) {
    Box(Modifier.fillMaxSize()) {
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val isLoading by playerViewModel.isLoading.collectAsState()
        val allSongs by playerViewModel.allSongs.collectAsState()
        val currentPlaying by playerViewModel.currentMediaItem.collectAsState()
        // 播放器连接状态
        val isConnected by playerViewModel.isConnected.collectAsState()
        val context = LocalContext.current

        // 定位到当前播放歌曲的函数
        fun scrollToCurrentPlaying() {
            currentPlaying?.let { playing ->
                val currentIndex = allSongs.indexOfFirst {
                    it.mediaStoreId.toString() == playing.mediaId
                }
                if (currentIndex >= 0) {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(currentIndex)
                    }
                }
            }
        }

        // 歌曲列表滚动条
        LazyColumnScrollbar(
            settings = ScrollbarSettings(
                thumbSelectedColor = SaltTheme.colors.highlight,
                thumbUnselectedColor = SaltTheme.colors.highlight.copy(alpha = 0.5f),
            ),
            state = lazyListState,
        ) {
            // 歌曲列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = lazyListState,
            ) {
                if (!isLoading && allSongs.isEmpty()) {
                    item {
                        EmptyMusicList()
                    }
                } else {
                    items(
                        items = allSongs,
                        key = { it.mediaStoreId.toString() }
                    ) { songEntity ->
                        MusicListItem(
                            songEntity = songEntity,
                            currentPlaying = currentPlaying,
                            onClick = {
                                val songIndex = allSongs.indexOf(songEntity)
                                if (isConnected) {
                                    if (songIndex >= 0) {
                                        playerViewModel.setAndPlayPlaylist(allSongs, songIndex)
                                    }
                                } else {
                                    // 提示未连接播放器
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.player_not_connected),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onActionClick = {
                                onActionClick(songEntity)
                            }
                        )
                    }
                }
            }
        }
        SmallFloatingActionButton(
            onClick = {
                scrollToCurrentPlaying()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = SaltTheme.colors.subBackground,
            contentColor = SaltTheme.colors.highlight
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_24px),
                contentDescription = "定位当前播放歌曲",
            )
        }
    }
}

@UnstableSaltUiApi
@Composable
fun AlbumList(
    modifier: Modifier = Modifier,
    navController: NavController,
    allAlbums: List<AlbumEntity>,
    isLoading: Boolean
) {
    val gridState = rememberLazyGridState()
    var gridColumns by remember { mutableIntStateOf(2) } // 默认2列

    LazyVerticalGridScrollbar(
        settings = ScrollbarSettings(
            thumbSelectedColor = SaltTheme.colors.highlight,
            thumbUnselectedColor = SaltTheme.colors.highlight.copy(alpha = 0.5f),
        ),
        state = gridState,
    ) {
        // 专辑列表
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                4.dp
            ),
            state = gridState
        ) {
            if (!isLoading && allAlbums.isEmpty()) {
                item(span = {
                    GridItemSpan(gridColumns)
                }) {
                    EmptyMusicList()
                }
            } else {
                items(
                    items = allAlbums,
                    key = { it.albumId.toString() }
                ) { albumEntity ->
                    AlbumListItem(
                        albumEntity = albumEntity,
                        onClick = { albumId ->
                            navController.navigate(ScreenRoute.Album.createRoute(albumId))
                        }
                    )
                }
            }
        }
    }
}

private enum class MusicType() {
    SONG(),
    ALBUM(),
    ARTIST(),
    FOLDER()
}