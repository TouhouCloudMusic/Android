package net.hearnsoft.tcm.compose.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.moriafly.salt.ui.UnstableSaltUiApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.BuildConfig
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.MainActivity
import net.hearnsoft.tcm.compose.data.repository.MusicRepository
import net.hearnsoft.tcm.compose.pref.PlayerSeekToPreviousAction
import net.hearnsoft.tcm.compose.pref.SettingsDataStore
import net.hearnsoft.tcm.compose.utils.Logger
import net.hearnsoft.tcm.compose.utils.LyricsExtractor
import net.hearnsoft.tcm.compose.utils.LyricsExtractor.LyricsFormat
import net.hearnsoft.tcm.compose.utils.PlayerFavoriteBridge
import net.hearnsoft.tcm.compose.utils.PlayerLyricsBridge
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
@UnstableApi
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
class MusicPlaybackService: MediaLibraryService(), AnalyticsListener {

    @Inject
    lateinit var musicRepository: MusicRepository

    companion object {
        const val CUSTOM_COMMAND_CLOSE = "${BuildConfig.APPLICATION_ID}.COMMAND_CLOSE"
        const val CUSTOM_COMMAND_FAVORITE = "${BuildConfig.APPLICATION_ID}.COMMAND_FAVORITE"

        // 自定义action
        const val ACTION_EXIT_APP = "${BuildConfig.APPLICATION_ID}.ACTION_EXIT_APP"
    }

    // 设置数据存储
    private lateinit var settingsDataStore: SettingsDataStore

    var mediaLibrarySession: MediaLibrarySession? = null
    var player: ExoPlayer? = null
    private val playlist: MutableList<MediaItem> = ArrayList()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val closeCommandButton = CommandButton.Builder(CommandButton.ICON_UNDEFINED)
        .setDisplayName("Close")
        .setCustomIconResId(R.drawable.ic_close_24px)
        .setSessionCommand(SessionCommand(CUSTOM_COMMAND_CLOSE, Bundle.EMPTY))
        .build()

    private lateinit var favoriteCommandButton: CommandButton

    // 当前媒体的歌词信息
    private var currentLyrics: String? = ""

    private var lyricsFormat = LyricsFormat.UNKNOWN

    override fun onCreate() {
        super.onCreate()
        settingsDataStore = SettingsDataStore(this)

        initializePlayer()
        initializeSession()

        player?.addAnalyticsListener(this)
        observeFavoriteStatus()
    }

    private fun observeFavoriteStatus() {
        serviceScope.launch {
            player?.let { exoPlayer ->
                exoPlayer.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.let { item ->
                            val mediaId = item.mediaId.toLongOrNull()
                            if (mediaId != null) {
                                serviceScope.launch {
                                    val song = musicRepository.getSongByMediaStoreId(mediaItem.mediaId.toLong())
                                    val isFavorite = song?.isFavorite ?: false
                                    PlayerFavoriteBridge.update(isFavorite)
                                    updateFavoriteCommandStatus(isFavorite)
                                }
                            }
                        }
                    }
                })
            }
            PlayerFavoriteBridge.isFavorite.collect { isFavorite ->
                updateFavoriteCommandStatus(isFavorite)
            }
        }
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

        val favoriteCommand = CommandButton.Builder(CommandButton.ICON_UNDEFINED)
            .setDisplayName("Favorite")
            .setCustomIconResId(R.drawable.ic_favorite_border)
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_FAVORITE, Bundle.EMPTY))
            .build()

        // 设置自定义Player包装类
        val wrapperPlayer = MyAppPlayerWrapper(
            exoPlayer = player!!,
            settingsDataStore = settingsDataStore
        )

        mediaLibrarySession = MediaLibrarySession.Builder(this, wrapperPlayer, LibrarySessionCallback())
            .setCustomLayout(ImmutableList.of(favoriteCommand, closeCommandButton))
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

    private fun updateFavoriteCommandStatus(status: Boolean) {
        val iconResId = if (status) {
            R.drawable.ic_favorite
        } else {
            R.drawable.ic_favorite_border
        }
        favoriteCommandButton = CommandButton.Builder(CommandButton.ICON_UNDEFINED)
            .setDisplayName("Favorite")
            .setCustomIconResId(iconResId)
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_FAVORITE, Bundle.EMPTY))
            .build()
        mediaLibrarySession?.setCustomLayout(
            ImmutableList.of(favoriteCommandButton, closeCommandButton)
        )
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

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableCommands = connectionResult.availableSessionCommands.buildUpon()
                // 添加自定义关闭命令
                .add(SessionCommand(CUSTOM_COMMAND_FAVORITE, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_CLOSE, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.accept(
                availableCommands,
                connectionResult.availablePlayerCommands
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Logger.debug(
                "LibrarySessionCallback",
                "Received custom command: ${customCommand.customAction}"
            )
            if (customCommand.customAction == CUSTOM_COMMAND_CLOSE) {
                // 发送广播以退出应用
                val exitIntent = Intent(ACTION_EXIT_APP)
                LocalBroadcastManager.getInstance(this@MusicPlaybackService)
                    .sendBroadcast(exitIntent)

                // 停止播放和服务
                player?.stop()
                player?.clearMediaItems()

                // 延迟停止服务
                serviceScope.launch {
                    delay(500)
                    stopSelf()
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            if (customCommand.customAction == CUSTOM_COMMAND_FAVORITE) {
                // 处理收藏命令
                val currentItem = player?.currentMediaItem
                if (currentItem != null) {
                    val mediaId = currentItem.mediaId.toLongOrNull()
                    if (mediaId != null) {
                        serviceScope.launch {
                            try {
                                // 切换收藏状态
                                val newStatus = PlayerFavoriteBridge.toggle()
                                musicRepository.updateFavoriteStatus(
                                    songId = mediaId,
                                    isFavorite = newStatus
                                )
                                updateFavoriteCommandStatus(newStatus)
                            } catch (e: Exception) {
                                Logger.err(
                                    "LibrarySessionCallback",
                                    "Error updating favorite status: ${e.message}"
                                )
                            }
                        }
                    }
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    // 我们的自定义Player包装类
    @UnstableApi
    private class MyAppPlayerWrapper(
        exoPlayer: ExoPlayer,
        private val settingsDataStore: SettingsDataStore
    ) : ForwardingPlayer(exoPlayer) {

        private var previousAction: PlayerSeekToPreviousAction = PlayerSeekToPreviousAction.DEFAULT

        init {
            CoroutineScope(Dispatchers.Main).launch {
                settingsDataStore.playerSeekToPreviousAction.collect { actionOrdinal ->
                    previousAction = PlayerSeekToPreviousAction.entries.getOrNull(actionOrdinal)
                        ?: PlayerSeekToPreviousAction.DEFAULT
                }
            }
        }

        // 修改过的上一曲方法
        override fun seekToPrevious() {
            when (previousAction) {
                PlayerSeekToPreviousAction.DEFAULT -> super.seekToPrevious()
                PlayerSeekToPreviousAction.ALWAYS_PREVIOUS -> seekToPreviousMediaItem()
                PlayerSeekToPreviousAction.ALWAYS_RESTART -> seekTo(0L)
            }
        }

        override fun getAvailableCommands(): Player.Commands {
            return super.getAvailableCommands()
                .buildUpon()
                .add(COMMAND_SEEK_TO_PREVIOUS)
                .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .build()
        }
    }
}