package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import my.nanihadesuka.compose.ScrollbarSettings
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.uicomponent.MusicListItem
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
    val currentPlaylist by playerViewModel.currentPlaylist.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()

    // 当前播放的媒体
    val currentPlaying = playerViewModel.currentMediaItem.collectAsState().value

    // 当前排序规则
    val currentSortingRule = playerViewModel.currentSortingRule.collectAsState().value

    var showSortDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf(allSongs.firstOrNull()) }

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

    if (showSortDialog) {
        MusicSortSheetDialog(
            currentRule = currentSortingRule,
            onSortRuleSelected = { rule ->
                playerViewModel.updateSortingRule(rule)
                showSortDialog = false
            },
            onDismissRequest = {
                showSortDialog = false
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
                    var selectedType by remember { mutableStateOf(MusicType.SONG) }
                    val typeList = listOf(
                        MusicType.SONG,
                        MusicType.ALBUM,
                        MusicType.ARTIST,
                        MusicType.FOLDER
                    )
                    val scrollState = rememberLazyListState()


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
                                    when (type) {
                                        MusicType.SONG -> {
                                        }
                                        MusicType.ALBUM -> {
                                        }
                                        MusicType.ARTIST -> {
                                        }
                                        MusicType.FOLDER -> {
                                        }
                                    }
                                },
                                label = {
                                    Text(text = type.displayName)
                                },
                                modifier = Modifier.padding(end = 8.dp),
                                leadingIcon = if (selectedType == type) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Done icon",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    {
                                        when (type) {
                                            MusicType.SONG -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_nav_music),
                                                    contentDescription = "歌曲",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                            MusicType.ALBUM -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_album_24px),
                                                    contentDescription = "专辑",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                            MusicType.ARTIST -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_artist_24px),
                                                    contentDescription = "艺术家",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                            MusicType.FOLDER -> {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_folder_24px),
                                                    contentDescription = "文件夹",
                                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                )
                                            }
                                        }
                                    }
                                },
                            )
                        }


                    }

                    IconButton(
                        onClick = {
                            showSortDialog = true
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sort_24px),
                            contentDescription = "排序"
                        )
                    }
                }

                // 歌曲数量显示
                if (allSongs.isNotEmpty()) {
                    Text(
                        text = "共 ${allSongs.size} 首歌曲",
                        style = SaltTheme.textStyles.sub
                    )
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

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumnScrollbar(
                        settings = ScrollbarSettings(
                            thumbSelectedColor = SaltTheme.colors.highlight,
                            thumbUnselectedColor = SaltTheme.colors.highlight.copy(alpha = 0.5f),
                        ),
                        state = lazyListState,

                    ) {
                        // 音乐列表
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            state = lazyListState,
                        ) {
                            if (!isLoading && allSongs.isEmpty()) {
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
                                                style = SaltTheme.textStyles.main
                                            )
                                            Text(
                                                text = "点击 扫描音乐 按钮来扫描设备中的音乐文件",
                                                style = SaltTheme.textStyles.sub,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(
                                    items = allSongs,
                                    key = { it.mediaStoreId.toString() } // 使用 mediaStoreId 作为唯一标识
                                ) { songEntity ->
                                    // 显示音乐列表项
                                    MusicListItem(
                                        songEntity = songEntity,
                                        currentPlaying = currentPlaying,
                                        onClick = {
                                            playerViewModel.playSong(songEntity)
                                        },
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

private enum class MusicType(val displayName: String) {
    SONG("歌曲"),
    ALBUM("专辑"),
    ARTIST("艺术家"),
    FOLDER("文件夹")
}