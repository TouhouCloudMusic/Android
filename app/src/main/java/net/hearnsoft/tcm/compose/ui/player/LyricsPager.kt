package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.parser.AutoParser
import com.mocharealm.accompanist.lyrics.ui.composable.lyrics.KaraokeLyricsView
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.android.awaitFrame
import net.hearnsoft.tcm.compose.constants.PlayerCoverVerticalPadding
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerUIColor
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.utils.Logger

@SuppressLint("UnusedBoxWithConstraintsScope")
@UnstableSaltUiApi
@UnstableApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun LyricsPager(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel
) {
    val lyrics = playerViewModel.lyrics.collectAsState().value
    val currentPosition = playerViewModel.currentPosition.collectAsState().value
    val isPlaying = playerViewModel.isPlaying.collectAsState().value

    // 使用 remember 保存解析后的歌词，避免重复解析
    var parsedLyrics by remember { mutableStateOf<SyncedLyrics?>(null) }
    val listState = rememberLazyListState()
    var animatedPosition by remember { mutableLongStateOf(0L) }

    // 缓存 AutoParser 实例
    val autoParser = remember { AutoParser.Builder().build() }

    // 获取最新的播放状态，用于平滑位置更新
    val latestPosition by rememberUpdatedState(currentPosition)

    val duration by playerViewModel.duration.collectAsState()

    // 解析歌词
    LaunchedEffect(lyrics) {
        parsedLyrics = lyrics?.let {
            try {
                autoParser.parse(it)
            } catch (e: Exception) {
                Logger.err("LyricsPager", "解析歌词失败: ${e.message}")
                null
            }
        }
        Logger.debug("LyricsPager", "解析歌词完成: ${parsedLyrics != null}")
    }

    // 平滑的位置更新动画
    LaunchedEffect(isPlaying, latestPosition) {
        if (isPlaying) {
            val startTime = System.currentTimeMillis()
            val startPosition = latestPosition

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                animatedPosition = (startPosition + elapsed).coerceAtMost(
                    duration
                )
                awaitFrame()
            }
        } else {
            animatedPosition = latestPosition
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = PlayerHorizontalPadding,
                vertical = PlayerCoverVerticalPadding
            )
    ) {
        parsedLyrics?.let { syncedLyrics ->
            if (syncedLyrics.lines.isNotEmpty()) {
                KaraokeLyricsView(
                    listState = listState,
                    lyrics = syncedLyrics,
                    currentPosition = animatedPosition,
                    onLineClicked = { line ->
                        playerViewModel.seekTo(line.start.toLong())
                    },
                    onLinePressed = { line ->
                        // 可以在这里添加长按功能，比如分享歌词等
                        Logger.debug("LyricsPager", "长按歌词行: ${line.start}")
                    },
                    normalLineTextStyle = SaltTheme.textStyles.paragraph,
                    accompanimentLineTextStyle = SaltTheme.textStyles.main,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                            blendMode = BlendMode.Plus
                        }
                )
            } else {
                // 显示无歌词状态,这里是防止部分歌词只有一行的情况下解析失败的问题
                NoLyricsPlaceholder(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } ?: run {
            // 显示无歌词状态
            NoLyricsPlaceholder(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun NoLyricsPlaceholder(
    modifier: Modifier = Modifier
) {
    val uiColor = LocalPlayerUIColor.current

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "暂无歌词",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = uiColor,
        )
        Text(
            text = "享受纯音乐的美妙时光",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = uiColor,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}