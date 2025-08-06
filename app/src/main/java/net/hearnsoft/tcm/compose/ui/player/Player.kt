package net.hearnsoft.tcm.compose.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Surface
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import me.saket.squiggles.SquigglySlider
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding
import net.hearnsoft.tcm.compose.ui.theme.Theme
import net.hearnsoft.tcm.compose.ui.uiwidgets.ResizableIconButton
import net.hearnsoft.tcm.compose.utils.formatTimeString


@Composable
@ExperimentalMaterial3Api
@UnstableSaltUiApi
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    var position by remember {
        mutableLongStateOf(1000)
    }

    var duration by remember {
        mutableLongStateOf(2000000)
    }

    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    var favorite by remember {
        mutableStateOf(false)
    }

    // 评论数量小数字
    var commentCount by remember {
        mutableIntStateOf(9)
    }

    BottomSheet(
        state = state,
        modifier = modifier,
        collapsedContent = {
            MiniPlayer(modifier = modifier)
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
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
                                contentDescription = "收起抽屉"
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row {
                            // 投送按钮
                            IconButton(
                                onClick = {},
                                modifier = modifier.padding(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_cast_24px),
                                    contentDescription = "投送"
                                )
                            }
                            // 分享按钮
                            IconButton(
                                onClick = {},
                                modifier = modifier.padding(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_share),
                                    contentDescription = "分享"
                                )
                            }
                        }
                    }
                    // 这里是播放器内容
                    Column {
                        // 封面图片
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://example.com/cover.jpg") // 替换为实际的封面图片URL
                                .crossfade(true)
                                .crossfade(1000)
                                .build(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(16.dp)
                                .scale(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentDescription = "Cover Art",
                        )
                        // 歌曲信息
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 4.dp),
                        ) {
                            // 歌曲标题和艺术家
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = "Sample Song Title",
                                    style = SaltTheme.textStyles.main,
                                    modifier = Modifier.padding(4.dp)
                                )
                                Text(
                                    text = "Artist",
                                    style = SaltTheme.textStyles.sub,
                                    modifier = Modifier.padding(4.dp)
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
                                            contentDescription = "评论"
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
                                                color = SaltTheme.colors.text,
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
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            value = (sliderPosition ?: position).toFloat(),
                            valueRange = 0f..100f, // 假设进度范围为0到100
                            onValueChange = { value ->
                                sliderPosition = value.toLong()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                            squigglesSpec =
                                SquigglySlider.SquigglesSpec(
                                    amplitude = /*if (isPlaying) (2.dp).coerceAtLeast(2.dp) else*/ (2.dp).coerceAtLeast(2.dp),
                                    strokeWidth = 3.dp,
                                    wavelength = (24.dp).coerceAtLeast(16.dp),
                                ),
                            colors = SliderDefaults.colors(
                                thumbColor = SaltTheme.colors.highlight,
                                activeTrackColor = SaltTheme.colors.highlight,
                                inactiveTrackColor = SaltTheme.colors.subBackground,
                            )
                        )

                        // 时间显示
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = PlayerHorizontalPadding + 4.dp),
                        ) {
                            Text(
                                text = formatTimeString(sliderPosition ?: position),
                                style = SaltTheme.textStyles.sub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Text(
                                text = formatTimeString(duration),
                                style = SaltTheme.textStyles.sub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // 播放控制按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 循环模式切换
                            Box(modifier = Modifier.weight(1f)) {
                                ResizableIconButton(
                                    icon = R.drawable.ic_play_cycle, // 后续实现切换
                                    color = SaltTheme.colors.text,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp)
                                        .align(Alignment.Center),
                                    onClick = {}
                                )
                            }
                            // 上一首按钮
                            Box(modifier = Modifier.weight(1f)) {
                                ResizableIconButton(
                                    icon = R.drawable.ic_music_prev,
                                    color = SaltTheme.colors.text,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp)
                                        .align(Alignment.Center),
                                    onClick = {}
                                )
                            }

                            Spacer(Modifier.width(8.dp))
                            // 播放/暂停按钮
                            Box(modifier = Modifier.weight(1f)) {
                                var isPlaying by remember { mutableStateOf(false) }

                                val icon = if (isPlaying) {
                                    AnimatedImageVector.animatedVectorResource(R.drawable.avd_play_to_pause)
                                } else {
                                    AnimatedImageVector.animatedVectorResource(R.drawable.avd_pause_to_play)
                                }

                                val animatedCornerRadius by animateFloatAsState(
                                    targetValue = if (!isPlaying) 16f else 50f,
                                    animationSpec = tween(500),
                                    label = "corner_radius"
                                )

                                FloatingActionButton(
                                    onClick = {
                                        isPlaying = !isPlaying
                                    },
                                    shape = RoundedCornerShape(animatedCornerRadius.dp),
                                    containerColor = SaltTheme.colors.highlight,
                                ) {
                                    Icon(
                                        painter = rememberAnimatedVectorPainter(
                                            icon, isPlaying
                                        ),
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(24.dp),
                                        tint = SaltTheme.colors.onHighlight
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))

                            // 下一首按钮
                            Box(modifier = Modifier.weight(1f)) {
                                ResizableIconButton(
                                    icon = R.drawable.ic_music_next,
                                    color = SaltTheme.colors.text,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(4.dp)
                                        .align(Alignment.Center),
                                    onClick = {}
                                )
                            }

                            // 播放列表按钮
                            Box(modifier = Modifier.weight(1f)) {
                                ResizableIconButton(
                                    icon = R.drawable.ic_music_list,
                                    color = SaltTheme.colors.text,
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
