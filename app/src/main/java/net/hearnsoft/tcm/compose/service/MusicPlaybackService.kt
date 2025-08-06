package net.hearnsoft.tcm.compose.service

import android.app.PendingIntent
import android.content.Intent
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
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.MainActivity

@UnstableApi
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
class MusicPlaybackService : MediaLibraryService(), AnalyticsListener {
    var mediaLibrarySession: MediaLibrarySession? = null
    var player: ExoPlayer? = null
    private val playlist: MutableList<MediaItem> = ArrayList()

    // 当前媒体的歌词信息
    var currentLyrics = ""

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeSession()

        // 添加播放器监听器
        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

            }
        })
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
        provider.setSmallIcon(R.drawable.ic_launcher_foreground)
        setMediaNotificationProvider(provider)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTracksChanged(eventTime: AnalyticsListener.EventTime, tracks: Tracks) {
        super.onTracksChanged(eventTime, tracks)
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