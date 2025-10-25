package net.hearnsoft.tcm.compose.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.MainActivity
import net.hearnsoft.tcm.compose.utils.Logger
import net.hearnsoft.tcm.compose.utils.LyricsExtractor
import net.hearnsoft.tcm.compose.utils.LyricsExtractor.LyricsFormat
import net.hearnsoft.tcm.compose.utils.PlayerLyricsBridge
import kotlin.math.min

@UnstableApi
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
class MusicPlaybackService : MediaLibraryService(), AnalyticsListener {
    var mediaLibrarySession: MediaLibrarySession? = null
    var player: ExoPlayer? = null
    private val playlist: MutableList<MediaItem> = ArrayList()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 当前媒体的歌词信息
    private var currentLyrics: String? = ""

    private var lyricsFormat = LyricsFormat.UNKNOWN

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeSession()

        player?.addAnalyticsListener(this)
    }

    private fun initializePlayer() {
        // 创建ExoPlayer实例
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(USAGE_MEDIA)
                    .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),true)
            .build()
        // 默认初始化使用循环播放
        player?.repeatMode = Player.REPEAT_MODE_ALL
    }

    @OptIn(dagger.hilt.android.UnstableApi::class)
    private fun initializeSession() {
        // 创建PendingIntent用于处理媒体控制
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaLibrarySession = MediaLibrarySession.Builder(this, player!!, LibrarySessionCallback())
            .setSessionActivity(sessionActivity)
            .build()

        val provider : DefaultMediaNotificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .build()
        provider.setSmallIcon(R.drawable.ic_app_logo)
        setMediaNotificationProvider(provider)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTracksChanged(eventTime: AnalyticsListener.EventTime, tracks: Tracks) {
        serviceScope.launch {
            var extractedLyrics: String? = null
            var format = LyricsFormat.UNKNOWN

            // 遍历所有轨道组来查找歌词
            for (group in tracks.groups) {
                for (i in 0..<group.length) {
                    if (!group.isTrackSelected(i)) continue

                    val trackFormat = group.getTrackFormat(i)
                    if (trackFormat.metadata != null) {
                        // 尝试从元数据中提取歌词
                        val options =
                            LyricsExtractor.LyricsOptions(true, "Unable to extract lyrics")

                        val result =
                            LyricsExtractor.extractLyricsFromMetadata(
                                trackFormat.metadata!!,
                                options
                            )

                        if (result != null) {
                            extractedLyrics = result.lyricsText
                            format = result.format
                            Logger.debug(
                                "MusicPlaybackService",
                                "Lyrics extracted successfully, format: $format"
                            )
                            break
                        }
                    }
                }
                // 找到歌词后退出循环
                if (extractedLyrics != null) break
            }

            // 更新歌词状态
            currentLyrics = extractedLyrics
            lyricsFormat = format

            PlayerLyricsBridge.update(
                currentLyrics,
                lyricsFormat
            )

            if (extractedLyrics != null) {
                Logger.debug(
                    "MusicPlaybackService",
                    "Lyrics found: " +
                            extractedLyrics.substring(
                                0, min(100.0, extractedLyrics.length.toDouble()).toInt()
                            ) +
                            "..."
                )
            } else {
                Logger.debug("MusicPlaybackService", "No lyrics found for current track")
            }
        }
    }

    fun setPlaylist(mediaItems: List<MediaItem>) {
        playlist.clear()
        playlist.addAll(mediaItems)
        // 更新播放器的媒体项
        player?.setMediaItems(ImmutableList.copyOf(playlist))
        player?.prepare()
    }

    override fun onDestroy() {
        if (mediaLibrarySession != null) {
            mediaLibrarySession?.release()
            mediaLibrarySession = null
        }
        if (player != null) {
            player?.release()
            player = null
        }
        super.onDestroy()
    }

    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {
        @UnstableApi
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaItemsWithStartPosition> {
            return super.onPlaybackResumption(mediaSession, controller)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            // 根据ID查找媒体项
            for (item in playlist) {
                if (mediaId == item.mediaId) {
                    return Futures.immediateFuture(LibraryResult.ofItem(item, null))
                }
            }
            return super.onGetItem(session, browser, mediaId)
        }
    }
}