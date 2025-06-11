package net.hearnsoft.tcm.application

import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import net.hearnsoft.tcm.R
import net.hearnsoft.tcm.ui.activity.MainActivity
import net.hearnsoft.tcm.infrastructure.logger.Logger.debug
import net.hearnsoft.tcm.utils.LyricsExtractor
import net.hearnsoft.tcm.utils.LyricsExtractor.LyricsFormat
import net.hearnsoft.tcm.utils.LyricsExtractor.LyricsOptions
import kotlin.math.min

@UnstableApi
class MusicPlaybackService : MediaLibraryService(), AnalyticsListener {
    private var mediaLibrarySession: MediaLibrarySession? = null
    private var player: ExoPlayer? = null
    private val playlist: MutableList<MediaItem> = ArrayList()

    private var currentLyrics: String? = ""

    private var lyricsFormat = LyricsFormat.UNKNOWN

    // 添加歌词回调接口
    interface LyricsUpdateListener {
        fun onLyricsUpdated(lyrics: String?, format: LyricsFormat?)
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeSession()

        // Add listener to track playback state
        player!!.addListener(
            object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // Optional: log or handle transitions
                    if (mediaItem != null) {
                        debug("MusicPlaybackService", "Now playing: " + mediaItem.mediaMetadata.title)
                    }
                }
            })

        player!!.addAnalyticsListener(this)
    }

    private fun initializePlayer() {
        // 创建ExoPlayer实例
        player =
            ExoPlayer.Builder(this)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                .build()

        // 设置为循环播放
        player!!.repeatMode = Player.REPEAT_MODE_ALL
    }

    @UnstableApi
    private fun initializeSession() {
        // 创建PendingIntent用于通知点击动作
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val sessionActivity =
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        // 创建媒体库会话
        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player!!, LibrarySessionCallback())
                .setSessionActivity(sessionActivity)
                .build()

        // 设置通知管理器
        val provider = DefaultMediaNotificationProvider.Builder(this).build()
        provider.setSmallIcon(R.drawable.ic_launcher_foreground)
        setMediaNotificationProvider(provider)
    }

    override fun onTracksChanged(eventTime: EventTime, tracks: Tracks) {
        /*AnalyticsListener.super.onTracksChanged(eventTime, tracks);*/
        val currentItem = player!!.currentMediaItem ?: return

        debug(
            "MusicPlaybackService",
            "Tracks changed, extracting lyrics for: " + currentItem.mediaMetadata.title
        )

        Thread {
            var extractedLyrics: String? = null
            var format = LyricsFormat.UNKNOWN

            // 遍历所有轨道组来查找歌词
            for (group in tracks.groups) {
                for (i in 0..<group.length) {
                    if (!group.isTrackSelected(i)) continue

                    val trackFormat = group.getTrackFormat(i)
                    if (trackFormat.metadata != null) {
                        // 尝试从元数据中提取歌词
                        val options = LyricsOptions(true, "Unable to extract lyrics")

                        val result =
                            LyricsExtractor.extractLyricsFromMetadata(
                                trackFormat.metadata!!,
                                options
                            )

                        if (result != null) {
                            extractedLyrics = result.lyricsText
                            format = result.format
                            debug(
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

            if (lyricsUpdateListener != null) {
                Handler(mainLooper).post {
                    lyricsUpdateListener!!.onLyricsUpdated(currentLyrics, lyricsFormat)
                }
            }
            if (extractedLyrics != null) {
                debug(
                    "MusicPlaybackService",
                    "Lyrics found: " +
                            extractedLyrics.substring(
                                0, min(100.0, extractedLyrics.length.toDouble()).toInt()
                            ) +
                            "..."
                )
            } else {
                debug("MusicPlaybackService", "No lyrics found for current track")
            }
        }
            .start()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        // 释放播放器和会话资源
        if (mediaLibrarySession != null) {
            mediaLibrarySession!!.release()
            mediaLibrarySession = null
        }
        if (player != null) {
            player!!.release()
            player = null
        }
        lyricsUpdateListener = null // 清除歌词更新监听器
        super.onDestroy()
    }

    /** 添加单个歌曲到播放列表 */
    fun addSongToPlaylist(item: MediaItem) {
        playlist.add(item)
        player!!.addMediaItem(item)
    }

    /** 设置播放列表并开始播放 */
    fun setPlaylistAndPlay(items: List<MediaItem>, startIndex: Int) {
        playlist.clear()
        playlist.addAll(items)

        player!!.clearMediaItems()
        player!!.setMediaItems(items)
        player!!.seekTo(startIndex, 0)
        player!!.prepare()
        player!!.play()
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

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            // 实现媒体库浏览逻辑
            if (parentId == MediaItemTree.ROOT) {
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(ImmutableList.copyOf(playlist), /* params= */ null)
                )
            }
            // 其他媒体树节点的处理...
            return Futures.immediateFuture(
                LibraryResult.ofItemList(ImmutableList.of(), /* params= */ null)
            )
        }
    }

    /** 简单的媒体项树结构管理 */
    private object MediaItemTree {
        const val ROOT: String = "root"
    }

    companion object {
        @JvmStatic
        fun setLyricsUpdateListener(lyricsUpdateListener: LyricsUpdateListener?) {
            Companion.lyricsUpdateListener = lyricsUpdateListener
        }

        // 提供静态方法供ViewModel注册监听器
        var lyricsUpdateListener: LyricsUpdateListener? = null

    }
}
