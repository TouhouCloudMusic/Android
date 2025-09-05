package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.theme.TouhouCloudMusicTheme
import net.hearnsoft.tcm.compose.ui.uicomponent.MusicListItem
import net.hearnsoft.tcm.compose.ui.uicomponent.sheet.BottomSheetDialog
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
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (allSongs.isNotEmpty()) {
                        Button(
                            onClick = {
                                playerViewModel.playCurrentPlaylist()
                            },
                            text = "播放全部",
                            enabled = !isLoading && currentPlaylist.isNotEmpty()
                        )
                    }

                    Button(
                        onClick = {
                            showSortDialog = true
                        },
                        text = "排序",
                    )
                }

                // 歌曲数量显示
                if (allSongs.isNotEmpty()) {
                    Text(
                        text = "共 ${allSongs.size} 首歌曲",
                        style = SaltTheme.textStyles.sub,
                        modifier = Modifier.
                        padding(vertical = 8.dp)
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