package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Window
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Surface
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.squiggles.SquigglySlider
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding
import net.hearnsoft.tcm.compose.ui.uicomponent.HashTag
import net.hearnsoft.tcm.compose.ui.uicomponent.ResizableIconButton
import net.hearnsoft.tcm.compose.ui.uicomponent.flowing.FlowingLightBackground
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerUIColor
import net.hearnsoft.tcm.compose.ui.utils.PlayerForegroundColorDark
import net.hearnsoft.tcm.compose.ui.utils.PlayerForegroundColorLight
import net.hearnsoft.tcm.compose.ui.utils.getPlayerUIColor
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.utils.Logger
import net.hearnsoft.tcm.compose.utils.SystemMediaDialogUtils
import net.hearnsoft.tcm.compose.utils.formatTimeString


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableSaltUiApi
@UnstableApi
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    context: Activity,
    modifier: Modifier = Modifier
) {

    // 协程作用域
    val coroutineScope = rememberCoroutineScope()

    // 当前播放
    val currentPlaying = playerViewModel.currentMediaItem.collectAsState().value
    // 封面
    val artworkUri = currentPlaying?.mediaMetadata?.artworkUri
    // 标题
    val title = currentPlaying?.mediaMetadata?.title ?: "未知歌曲"
    // 艺术家
    val artist = currentPlaying?.mediaMetadata?.artist ?: "未知艺术家"

    // 进度
    val currentPosition = playerViewModel.currentPosition.collectAsState().value
    val duration = playerViewModel.duration.collectAsState().value

    // 播放状态
    val isPlaying = playerViewModel.isPlaying.collectAsState().value

    // 播放模式状态
    val repeatMode = playerViewModel.repeatMode.collectAsState().value
    val shuffleModeEnabled = playerViewModel.shuffleModeEnabled.collectAsState().value

    val isSystemInDarkTheme = isSystemInDarkTheme()

    // Pager状态
    val pagerState = rememberPagerState(
        pageCount = { 3 },
        initialPage = 1
    )

    // 进度条位置
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    // 收藏状态
    var favorite by remember {
        mutableStateOf(false)
    }

    // 评论数量小数字
    var commentCount by remember {
        mutableIntStateOf(9)
    }

    // 播放器UI部分前景染色
    var playerUIColor by remember {
        mutableStateOf(getPlayerUIColor(isSystemInDarkTheme))
    }

    // 封面是否加载完成
    var coverLoaded by remember {
        mutableStateOf(false)
    }

    // 状态栏颜色控制和全局前景色控制
    LaunchedEffect(state.isExpanded, currentPlaying, coverLoaded, isSystemInDarkTheme) {
        // 更新播放器UI颜色逻辑
        playerUIColor = when {
            // 当没有播放内容或封面未加载完成时，根据系统主题决定颜色
            currentPlaying == null || !coverLoaded -> getPlayerUIColor(isSystemInDarkTheme)
            // 当有播放内容且封面加载完成时，使用浅色前景
            else -> PlayerForegroundColorLight
        }

        Logger.debug("BottomSheetPlayer",
            "状态栏颜色控制: isExpanded=${state.isExpanded}," +
                    " currentPlaying=${currentPlaying?.mediaId ?: "null"}," +
                    " coverLoaded=$coverLoaded," +
                    " isSystemInDarkTheme=$isSystemInDarkTheme")

        val window: Window = context.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (state.isExpanded) {
            // 展开时的状态栏逻辑，与 playerUIColor 逻辑保持一致
            when {
                // 当没有播放内容或封面未加载完成时，根据系统主题决定状态栏颜色
                currentPlaying == null || !coverLoaded -> {
                    withContext(Dispatchers.Main) {
                        insetsController.isAppearanceLightStatusBars = !isSystemInDarkTheme
                    }
                }
                // 当有播放内容且封面加载完成时，使用浅色状态栏（因为背景是流光溢彩效果，较暗）
                else -> {
                    withContext(Dispatchers.Main) {
                        insetsController.isAppearanceLightStatusBars = false
                    }
                }
            }
        } else {
            // 折叠时始终根据系统主题决定状态栏颜色
            withContext(Dispatchers.Main) {
                insetsController.isAppearanceLightStatusBars = !isSystemInDarkTheme
            }
        }
    }

    BottomSheet(
        state = state,
        modifier = modifier,
        collapsedContent = {
            MiniPlayer(
                modifier = modifier,
                playerViewModel = playerViewModel,
                onPlaylistClick = {
                    // 先展开 BottomSheet
                    state.expandSoft()
                    // 然后跳转到播放列表页面（第3页，index为1）
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            )
        },
        backgroundContent = {
            // 流光溢彩背景
            FlowingLightBackground(
                imageUrl = artworkUri,
                modifier = Modifier.fillMaxSize(),
                onImageLoadResult = { result ->
                    coverLoaded = result
                },
            )
        }
    ) {
        CompositionLocalProvider(
            LocalPlayerUIColor provides playerUIColor
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // 视图根布局
                Box(
                    modifier = modifier.safeMainPadding()
                ) {
                    // 这里是主要视图
                    Column {
                        // 这里是顶栏
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .systemBarsPadding(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 折叠按钮
                            IconButton(
                                onClick = {
                                    state.collapseSoft()
                                },
                                modifier = modifier.padding(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_collapse),
                                    contentDescription = "收起抽屉",
                                    tint = LocalPlayerUIColor.current
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row {
                                // 投送按钮
                                IconButton(
                                    onClick = {
                                        SystemMediaDialogUtils.getInstance(context).showSystemMediaDialog()
                                    },
                                    modifier = modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_cast_24px),
                                        contentDescription = "投送",
                                        tint = LocalPlayerUIColor.current
                                    )
                                }
                                IconButton(
                                    onClick = {},
                                    modifier = modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_share),
                                        contentDescription = "分享",
                                        tint = LocalPlayerUIColor.current
                                    )
                                }
                            }
                        }

                        // 横向Pager
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .sizeIn(maxHeight = 600.dp, maxWidth = 600.dp)
                                .align(Alignment.CenterHorizontally)
                        ) { page ->
                            when (page) {
                                0 -> LyricsPager(playerViewModel = playerViewModel)
                                1 -> CoverPager(artworkUri = artworkUri)
                                2 -> PlaylistPager(playerViewModel = playerViewModel)
                            }
                        }


                        // 控制器和信息区域
                        Column(
                            modifier = Modifier
                                .padding(horizontal = PlayerHorizontalPadding, vertical = 16.dp)
                        ) {
                            // 歌曲信息
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            ) {
                                // 歌曲标题和艺术家
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = title.toString(),
                                        style = SaltTheme.textStyles.main,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                            .basicMarquee(iterations = Int.MAX_VALUE),
                                        maxLines = 1,
                                        color = LocalPlayerUIColor.current
                                    )
                                    Text(
                                        text = artist.toString(),
                                        style = SaltTheme.textStyles.sub,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        color = LocalPlayerUIColor.current
                                    )
                                }
                                // 部分控制按钮
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                ) {
                                    // 收藏按钮
                                    IconButton(
                                        onClick = {
                                            // TODO: 添加收藏逻辑
                                            favorite = !favorite
                                        },
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(
                                            painter = if (favorite) {
                                                painterResource(id = R.drawable.ic_favorite)
                                            } else {
                                                painterResource(id = R.drawable.ic_favorite_border)
                                            },
                                            contentDescription = "收藏",
                                            tint = Color.Unspecified // 使用默认颜色
                                        )
                                    }
                                    // 评论按钮
                                    Box {
                                        IconButton(
                                            onClick = {},
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_chat_bubble_count),
                                                contentDescription = "评论",
                                                tint = LocalPlayerUIColor.current,
                                            )
                                        }
                                        if (commentCount > 0) {
                                            Text(
                                                text = if (commentCount > 99) "99+" else commentCount.toString(),
                                                style = SaltTheme.textStyles.sub,
                                                color = LocalPlayerUIColor.current,
                                                modifier = Modifier
                                                    .padding(end = 4.dp, top = 4.dp)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                }
                            }

                            // Tags
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // 测试10个标签
                                items(10) { index ->
                                    HashTag(
                                        label = "Tag ${index + 1}"
                                    )
                                }
                            }

                            // 进度条
                            SquigglySlider(
                                value = (sliderPosition ?: currentPosition).toFloat(),
                                valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                                onValueChange = { value ->
                                    sliderPosition = value.toLong()
                                },
                                onValueChangeFinished = {
                                    sliderPosition?.let {
                                        playerViewModel.seekTo(it)
                                    }
                                    sliderPosition = null
                                },
                                modifier = Modifier,
                                squigglesSpec =
                                    SquigglySlider.SquigglesSpec(
                                        amplitude = if (isPlaying) (2.dp).coerceAtLeast(2.dp) else 0.dp,
                                        strokeWidth = 3.dp,
                                        wavelength = (24.dp).coerceAtLeast(16.dp),
                                    ),
                                colors = SliderDefaults.colors(
                                    thumbColor = SaltTheme.colors.highlight,
                                    activeTrackColor = SaltTheme.colors.highlight,
                                    inactiveTrackColor = SaltTheme.colors.stroke,
                                )
                            )

                            // 时间显示
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxWidth(),
                            ) {
                                Text(
                                    text = formatTimeString(sliderPosition ?: currentPosition),
                                    style = SaltTheme.textStyles.sub,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = LocalPlayerUIColor.current,
                                    modifier = Modifier
                                )

                                Text(
                                    text = formatTimeString(duration),
                                    style = SaltTheme.textStyles.sub,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = LocalPlayerUIColor.current,
                                    modifier = Modifier
                                )
                            }

                            // 播放控制按钮
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 循环模式切换
                                Box(modifier = Modifier.weight(1f)) {
                                    val iconRes = when {
                                        shuffleModeEnabled -> R.drawable.ic_shuffle_one
                                        repeatMode == Player.REPEAT_MODE_ONE -> R.drawable.ic_play_once
                                        else -> R.drawable.ic_play_cycle
                                    }
                                    ResizableIconButton(
                                        icon = iconRes,
                                        color = LocalPlayerUIColor.current,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(4.dp)
                                            .align(Alignment.Center),
                                        onClick = {
                                            // 切换播放模式的逻辑
                                            if (shuffleModeEnabled) {
                                                // 从随机切换到列表循环
                                                playerViewModel.toggleShuffle() // 关闭随机
                                                // PlayerController 中会自动将 repeatMode 设为 REPEAT_MODE_ALL
                                            } else {
                                                // 在列表循环和单曲循环间切换
                                                playerViewModel.toggleRepeatMode()
                                                // 如果是从单曲循环切换，则变为随机播放
                                                if (repeatMode == Player.REPEAT_MODE_ONE) {
                                                    playerViewModel.toggleShuffle() // 开启随机
                                                }
                                            }
                                        }
                                    )
                                }
                                // 上一首按钮
                                Box(modifier = Modifier.weight(1f)) {
                                    ResizableIconButton(
                                        icon = R.drawable.ic_music_prev,
                                        color = LocalPlayerUIColor.current,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(4.dp)
                                            .align(Alignment.Center),
                                        onClick = {
                                            playerViewModel.skipToPrevious()
                                        }
                                    )
                                }

                                // 播放/暂停按钮
                                Box(modifier = Modifier.weight(1f)) {
                                    ResizableIconButton(
                                        icon = if (isPlaying) {
                                            R.drawable.pause
                                        } else {
                                            R.drawable.play
                                        },
                                        color = LocalPlayerUIColor.current,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .align(Alignment.Center),
                                        onClick = {
                                            playerViewModel.togglePlayPause()
                                        },
                                    )
                                }

                                // 下一首按钮
                                Box(modifier = Modifier.weight(1f)) {
                                    ResizableIconButton(
                                        icon = R.drawable.ic_music_next,
                                        color = LocalPlayerUIColor.current,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(4.dp)
                                            .align(Alignment.Center),
                                        onClick = {
                                            playerViewModel.skipToNext()
                                        }
                                    )
                                }

                                // 播放列表按钮
                                Box(modifier = Modifier.weight(1f)) {
                                    ResizableIconButton(
                                        icon = R.drawable.ic_music_list,
                                        color = LocalPlayerUIColor.current,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(4.dp)
                                            .align(Alignment.Center),
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(2)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}