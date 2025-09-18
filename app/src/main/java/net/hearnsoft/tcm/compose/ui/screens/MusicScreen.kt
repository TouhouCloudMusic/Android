package net.hearnsoft.tcm.compose.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    // 列表状态和协程作用域
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 收集 ViewModel 状态
    val allSongs by playerViewModel.allSongs.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()

    // 收集专辑数据
    val allAlbums by playerViewModel.allAlbums.collectAsState()
    val currentAlbumSortingRule = playerViewModel.currentAlbumSortingRule.collectAsState().value

    // 当前播放的媒体
    val currentPlaying = playerViewModel.currentMediaItem.collectAsState().value

    // 当前排序规则
    val currentSortingRule = playerViewModel.currentSongSortingRule.collectAsState().value

    // 网格列表数配置
    var gridColumns by remember { mutableIntStateOf(2) } // 默认2列
    val gridState = rememberLazyGridState()

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
    val scrollState = rememberLazyListState()

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

                    LazyRow(
                        modifier = Modifier
                            .weight(1f),
                        state = scrollState,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = typeList,
                            key = { it.displayName }
                        ) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                },
                                label = {
                                    Text(
                                        text = type.displayName,
                                        color = if (selectedType == type) Color.White else SaltTheme.colors.text
                                    )
                                },
                                modifier = Modifier.padding(end = 8.dp),
                                leadingIcon = if (selectedType == type) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Done icon",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                                            tint = Color.White
                                        )
                                    }
                                } else {
                                    {
                                        when (type) {
                                            MusicType.SONG -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_nav_music),
                                                    contentDescription = "歌曲",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                                    tint = SaltTheme.colors.text
                                                )
                                            }
                                            MusicType.ALBUM -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_album_24px),
                                                    contentDescription = "专辑",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                                    tint = SaltTheme.colors.text
                                                )
                                            }
                                            MusicType.ARTIST -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_artist_24px),
                                                    contentDescription = "艺术家",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                                    tint = SaltTheme.colors.text
                                                )
                                            }
                                            MusicType.FOLDER -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_folder_24px),
                                                    contentDescription = "文件夹",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                                    tint = SaltTheme.colors.text
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = SelectableChipColors(
                                    containerColor = SaltTheme.colors.subBackground,
                                    labelColor = SaltTheme.colors.text,
                                    leadingIconColor = SaltTheme.colors.text,
                                    trailingIconColor = Color.Unspecified,
                                    disabledContainerColor = Color.Unspecified,
                                    disabledLabelColor = Color.Unspecified,
                                    disabledLeadingIconColor = Color.Unspecified,
                                    disabledTrailingIconColor = Color.Unspecified,
                                    selectedContainerColor = SaltTheme.colors.highlight,
                                    disabledSelectedContainerColor = Color.Unspecified,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    selectedTrailingIconColor = Color.Unspecified
                                ),
                            )
                        }
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
                when (selectedType) {
                    MusicType.SONG -> {
                        if (allSongs.isNotEmpty()) {
                            Text(
                                text = "共 ${allSongs.size} 首歌曲",
                                style = SaltTheme.textStyles.sub
                            )
                        }
                    }
                    MusicType.ALBUM -> {
                        if (allAlbums.isNotEmpty()) {
                            Text(
                                text = "共 ${allAlbums.size} 张专辑",
                                style = SaltTheme.textStyles.sub
                            )
                        }
                    }
                    else -> {}
                }

                // 加载指示器
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearProgressIndicator()
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
                                Box(Modifier.fillMaxSize()) {
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
                                                        onClick = { playerViewModel.playSong(songEntity) },
                                                        onActionClick = {
                                                            showActionDialog = true
                                                            selectedSong = songEntity
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

                            MusicType.ALBUM -> {
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
                            // 其他类型待实现
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = lazyListState,
                                ) {
                                    item {
                                        Text("该功能正在开发中...")
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
                text = "暂无内容",
                style = SaltTheme.textStyles.main
            )
            Text(
                text = "在侧边菜单里找到 扫描媒体 来扫描设备中的音乐文件",
                style = SaltTheme.textStyles.sub,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private enum class MusicType(val displayName: String) {
    SONG("歌曲"),
    ALBUM("专辑"),
    ARTIST("艺术家"),
    FOLDER("文件夹")
}