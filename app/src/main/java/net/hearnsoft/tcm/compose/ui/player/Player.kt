package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Surface
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.saket.squiggles.SquigglySlider
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.constants.PlayerCoverVerticalPadding
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding
import net.hearnsoft.tcm.compose.ui.theme.extractGradientColors
import net.hearnsoft.tcm.compose.ui.uicomponent.ResizableIconButton
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
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

    // 渐变颜色状态管理
    var gradientColors by remember {
        mutableStateOf<List<Color>>(emptyList())
    }

    val hasGradientColours : Boolean = gradientColors.size >= 2
    val whiteContrastThreshold = 2.0
    val blackContrastThreshold = 2.0

    val whiteGradientContrast : Double = when {
        hasGradientColours -> {
            ColorUtils.calculateContrast(
                gradientColors.first().toArgb(),
                Color.White.toArgb(),
            )
        }
        else -> whiteContrastThreshold
    }

    val blackGradientContrast : Double = when {
        hasGradientColours -> {
            ColorUtils.calculateContrast(
                gradientColors.last().toArgb(),
                Color.Black.toArgb(),
            )
        }
        else -> blackContrastThreshold
    }

    val onBackgroundColor =
        when {
            hasGradientColours -> {
                // 计算渐变色的平均亮度
                val averageLuminance = gradientColors.map { color ->
                    ColorUtils.calculateLuminance(color.toArgb())
                }.average()

                // 亮度阈值：0.5
                if (averageLuminance > 0.5) {
                    Color.Black // 背景亮，用黑色文字
                } else {
                    Color.White // 背景暗，用白色文字
                }
            }
            else -> {
                SaltTheme.colors.text // 默认颜色
            }
        }

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

    LaunchedEffect(currentPlaying, artworkUri) {
        if (artworkUri != null) {
            withContext(Dispatchers.IO) {
                val result =
                    (
                            ImageLoader(context)
                                .execute(
                                    ImageRequest
                                        .Builder(context)
                                        .data(artworkUri)
                                        .allowHardware(false)
                                        .build(),
                                ).image as? BitmapImage
                            )?.bitmap?.extractGradientColors(
                            darkTheme = isSystemInDarkTheme,
                        )

                result?.let {
                    gradientColors = it
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    BottomSheet(
        state = state,
        modifier = modifier,
        collapsedContent = {
            MiniPlayer(
                modifier = modifier,
                playerViewModel = playerViewModel,
            )
        },
        brushBackgroundColor =
            if (hasGradientColours) {
                Brush.verticalGradient(gradientColors)
            } else {
                Brush.verticalGradient(
                    listOf(
                        SaltTheme.colors.subBackground,
                        SaltTheme.colors.subBackground,
                    ),
                )
            },
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
                                tint = onBackgroundColor
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
                                    tint = onBackgroundColor
                                )
                            }
                            // 分享按钮
                            IconButton(
                                onClick = {},
                                modifier = modifier.padding(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_share),
                                    contentDescription = "分享",
                                    tint = onBackgroundColor
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
                            0 -> Box(modifier = Modifier.fillMaxSize()) {  }
                            1 -> CoverPager(artworkUri = artworkUri)
                            2 -> Box(modifier = Modifier.fillMaxSize()) {  }
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
                                    color = onBackgroundColor
                                )
                                Text(
                                    text = artist.toString(),
                                    style = SaltTheme.textStyles.sub,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    color = onBackgroundColor
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
                                            tint = onBackgroundColor
                                        )
                                    }
                                    if (commentCount > 0) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 2.dp, end = 2.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                        ) {
                                            Text(
                                                text = if (commentCount > 99) "99+" else commentCount.toString(),
                                                style = SaltTheme.textStyles.sub,
                                                color = onBackgroundColor,
                                                modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // TAG 区域
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                                color = onBackgroundColor
                            )

                            Text(
                                text = formatTimeString(duration),
                                style = SaltTheme.textStyles.sub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = onBackgroundColor
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
                                    color = onBackgroundColor,
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
                                    color = onBackgroundColor,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp)
                                        .align(Alignment.Center),
                                    onClick = {
                                        playerViewModel.skipToPrevious()
                                    }
                                )
                            }

                            /*Spacer(Modifier.width(16.dp))*/
                            // 播放/暂停按钮
                            Box(modifier = Modifier.weight(1f)) {
                                var isPressed by remember { mutableStateOf(false) }

                                val scale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.9f else 1f,
                                    animationSpec = tween(100),
                                    label = "fab_scale"
                                )

                                val animatedCornerRadius by animateFloatAsState(
                                    targetValue = if (isPlaying) 16f else 50f,
                                    animationSpec = tween(500),
                                    label = "corner_radius"
                                )

                                FloatingActionButton(
                                    onClick = {
                                        playerViewModel.togglePlayPause()
                                    },
                                    shape = RoundedCornerShape(animatedCornerRadius.dp),
                                    containerColor = SaltTheme.colors.highlight,
                                    modifier = Modifier
                                        .scale(scale)
                                        .align(Alignment.Center),
                                    interactionSource = remember { MutableInteractionSource() }
                                        .also { interactionSource ->
                                            LaunchedEffect(interactionSource) {
                                                interactionSource.interactions.collect { interaction ->
                                                    when (interaction) {
                                                        is PressInteraction.Press -> isPressed = true
                                                        is PressInteraction.Release -> isPressed = false
                                                        is PressInteraction.Cancel -> isPressed = false
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    val stateScale by animateFloatAsState(
                                        targetValue = if (isPlaying) 1.1f else 1f,
                                        animationSpec = tween(300),
                                        label = "icon_state_scale"
                                    )

                                    Icon(
                                        painter = painterResource(
                                            if (isPlaying) {
                                                R.drawable.pause
                                            } else {
                                                R.drawable.play
                                            }
                                        ),
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .scale(stateScale),
                                        tint = SaltTheme.colors.onHighlight
                                    )
                                }
                            }
                            /*Spacer(Modifier.width(16.dp))*/

                            // 下一首按钮
                            Box(modifier = Modifier.weight(1f)) {
                                ResizableIconButton(
                                    icon = R.drawable.ic_music_next,
                                    color = onBackgroundColor,
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
                                    color = onBackgroundColor,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp)
                                        .align(Alignment.Center),
                                    onClick = {}
                                )
                            }

                        }
                    }

                }


            }
        }
    }
}

@Composable
fun HashTag(
    label: String,
) {
    ElevatedAssistChip(
        modifier = Modifier.padding(end = 4.dp),
        colors = ChipColors(
            containerColor = SaltTheme.colors.subBackground,
            leadingIconContentColor = SaltTheme.colors.text,
            labelColor = SaltTheme.colors.text,
            trailingIconContentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified,
            disabledLabelColor = Color.Unspecified,
            disabledLeadingIconContentColor = Color.Unspecified,
            disabledTrailingIconContentColor = Color.Unspecified,
        ),
        label = {
            Text(
                text = label,
                style = SaltTheme.textStyles.sub
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_tag_24px),
                contentDescription = "Tag Icon",
                modifier = Modifier.size(16.dp)
            )
        },
        onClick = {}
    )
}
