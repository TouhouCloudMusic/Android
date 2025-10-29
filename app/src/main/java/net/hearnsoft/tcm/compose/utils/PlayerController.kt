package net.hearnsoft.tcm.compose.utils

import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.moriafly.salt.ui.UnstableSaltUiApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.pref.PlayerSeekToPreviousAction
import net.hearnsoft.tcm.compose.pref.SettingsDataStore
import net.hearnsoft.tcm.compose.service.MusicPlaybackService
import net.hearnsoft.tcm.compose.utils.LyricsExtractor.LyricsFormat
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.let

// 全局桥接对象
object PlayerLyricsBridge {
    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()

    private val _lyricsFormat = MutableStateFlow(LyricsFormat.UNKNOWN)
    val lyricsFormat: StateFlow<LyricsFormat> = _lyricsFormat.asStateFlow()

    fun update(lyrics: String?, format: LyricsFormat) {
        _lyrics.value = lyrics
        _lyricsFormat.value = format
    }

    fun clear() {
        _lyrics.value = null
        _lyricsFormat.value = LyricsFormat.UNKNOWN
    }
}


/**
 * 播放器控制器
 * 负责与 MusicPlaybackService 通信
 */
@Singleton
@UnstableApi
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    val scope = CoroutineScope(Dispatchers.Main)

    // 连接状态
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // 播放状态
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 当前媒体项
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    // 当前媒体播放列表
    private val _currentPlaylist = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentPlaylist: StateFlow<List<MediaItem>> = _currentPlaylist.asStateFlow()

    // 当前媒体位置索引
    private val _currentMediaItemIndex = MutableStateFlow(-1)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    // 播放模式
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    // 随机播放状态
    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    // === 歌词相关 ===
    val lyrics: StateFlow<String?> = PlayerLyricsBridge.lyrics
    val lyricsFormat: StateFlow<LyricsFormat> = PlayerLyricsBridge.lyricsFormat

    // === 播放速度与音高 ===
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaItem.value = mediaItem
            _currentMediaItemIndex.value = mediaController?.currentMediaItemIndex ?: -1
            PlayerLyricsBridge.clear()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleModeEnabled.value = shuffleModeEnabled
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            _currentPlaylist.value = getCurrentPlaylist()
            _currentMediaItemIndex.value = mediaController?.currentMediaItemIndex ?: -1
            Logger.debug("PlayerController", "播放列表已更新: ${_currentPlaylist.value.size} 首歌曲")
        }
    }

    /**
     * 连接到播放服务
     */
    fun connect() {
        try {
            // 直接使用 SessionToken 连接
            val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))

            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture?.addListener({
                try {
                    mediaController = controllerFuture?.get()
                    mediaController?.addListener(playerListener)
                    _isConnected.value = true

                    // 初始化状态
                    mediaController?.let { controller ->
                        _isPlaying.value = controller.isPlaying
                        _currentMediaItem.value = controller.currentMediaItem
                        _repeatMode.value = controller.repeatMode
                        _shuffleModeEnabled.value = controller.shuffleModeEnabled
                        _playbackSpeed.value = controller.playbackParameters.speed
                        _pitch.value = controller.playbackParameters.pitch
                    }

                    Logger.debug("PlayerController", "成功连接到媒体控制器")
                } catch (e: Exception) {
                    Logger.err("PlayerController", "连接媒体控制器失败: ${e.message}")
                    _isConnected.value = false
                }
            }, context.mainExecutor)

        } catch (e: Exception) {
            Logger.err("PlayerController", "创建媒体控制器失败: ${e.message}")
            _isConnected.value = false
        }
    }

    /**
     * 断开与播放服务的连接
     */
    fun disconnect() {
        try {
            mediaController?.removeListener(playerListener)
            controllerFuture?.let { MediaController.releaseFuture(it) }
        } catch (e: Exception) {
            Logger.err("PlayerController", "断开连接时出错: ${e.message}")
        } finally {
            mediaController = null
            controllerFuture = null
            _isConnected.value = false
        }
    }

    // === 播放控制方法 ===

    /**
     * 设置播放列表
     */
    fun setPlaylist(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        mediaController?.let { controller ->
            try {
                controller.setMediaItems(mediaItems, startIndex, 0L)
                controller.prepare()
                _currentPlaylist.value = mediaItems
                Logger.debug("PlayerController", "设置播放列表: ${mediaItems.size} 首歌曲，起始索引: $startIndex")
            } catch (e: Exception) {
                Logger.err("PlayerController", "设置播放列表失败: ${e.message}")
            }
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法设置播放列表")
        }
    }

    /**
     * 添加媒体项到播放列表
     * @param mediaItem 要添加的媒体项
     * @param index 插入位置，如果为 -1 则添加到末尾
     * @return 添加后的完整播放列表
     */
    fun addMediaItemToPlaylist(mediaItem: MediaItem, index: Int = -1): List<MediaItem> {
        mediaController?.let { controller ->
            try {
                if (index >= 0 && index <= controller.mediaItemCount) {
                    controller.addMediaItem(index, mediaItem)
                    Logger.debug("PlayerController", "在位置 $index 添加歌曲: ${mediaItem.mediaMetadata.title}")
                } else {
                    controller.addMediaItem(mediaItem)
                    Logger.debug("PlayerController", "在末尾添加歌曲: ${mediaItem.mediaMetadata.title}")
                }

                // 返回更新后的播放列表
                return getCurrentPlaylist()
            } catch (e: Exception) {
                Logger.err("PlayerController", "添加歌曲失败: ${e.message}")
            }
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法添加歌曲")
        }

        return emptyList()
    }

    /**
     * 从播放列表中移除媒体项
     * @param index 要移除的媒体项索引
     */
    fun removeMediaItemFromPlaylist(index: Int) {
        mediaController?.let { controller ->
            try {
                controller.removeMediaItem(index)
                Logger.debug("PlayerController", "移除播放列表中索引 $index 的歌曲")
            } catch (e: Exception) {
                Logger.err("PlayerController", "移除歌曲失败: ${e.message}")
            }
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法移除歌曲")
        }
    }

    /**
     * 清空播放列表
     */
    fun clearPlaylist() {
        mediaController?.let { controller ->
            try {
                controller.stop()
                controller.clearMediaItems()
                _currentPlaylist.value = emptyList()
                Logger.debug("PlayerController", "清空播放列表")
            } catch (e: Exception) {
                Logger.err("PlayerController", "清空播放列表失败: ${e.message}")
            }
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法清空播放列表")
        }
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法切换播放状态")
        }
    }

    /**
     * 播放
     */
    fun play() {
        mediaController?.play() ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法播放")
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        mediaController?.pause() ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法暂停")
        }
    }

    /**
     * 停止
     */
    fun stop() {
        mediaController?.stop() ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法停止")
        }
    }

    /**
     * 下一首
     */
    fun skipToNext() {
        mediaController?.seekToNext() ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法跳到下一首")
        }
    }

    /**
     * 上一首
     */
    fun skipToPrevious() {
        scope.launch {
            mediaController?.seekToPrevious() ?: run {
                Logger.warn("PlayerController", "媒体控制器未连接，无法跳到上一首")
            }
        }
    }

    /**
     * 跳转到指定位置
     */
    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs) ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法跳转位置")
        }
    }

    /**
     * 跳转到指定媒体项
     */
    fun seekToMediaItem(index: Int) {
        mediaController?.seekToDefaultPosition(index) ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法跳转到媒体项")
        }
    }

    /**
     * 设置重复模式
     */
    fun setRepeatMode(repeatMode: Int) {
        mediaController?.let { controller ->
            controller.repeatMode = repeatMode
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法设置重复模式")
        }
    }

    /**
     * 切换重复模式
     */
    fun toggleRepeatMode() {
        mediaController?.let { controller ->
            val nextMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
            setRepeatMode(nextMode)
        }
    }

    /**
     * 设置随机播放
     */
    fun setShuffleModeEnabled(enabled: Boolean) {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = enabled
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法设置随机播放")
        }
    }

    /**
     * 切换随机播放
     */
    fun toggleShuffle() {
        mediaController?.let { controller ->
            setShuffleModeEnabled(!controller.shuffleModeEnabled)
        }
    }

    /**
     * 获取当前播放位置
     */
    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    /**
     * 获取当前媒体时长
     */
    fun getDuration(): Long {
        return mediaController?.duration ?: 0L
    }

    /**
     * 获取当前播放列表
     */
    fun getCurrentPlaylist(): List<MediaItem> {
        val playlist = mutableListOf<MediaItem>()
        mediaController?.let { controller ->
            for (i in 0 until controller.mediaItemCount) {
                controller.getMediaItemAt(i).let { playlist.add(it) }
            }
        }
        return playlist
    }

    /**
     * 获取当前播放索引
     */
    fun getCurrentMediaItemIndex(): Int {
        return mediaController?.currentMediaItemIndex ?: -1
    }

    /**
     * 检查播放器是否可用
     */
    fun isPlayerAvailable(): Boolean {
        return mediaController != null && _isConnected.value
    }

    // 额外播放器功能设置

    /**
     * 设置播放器播放速度
     * @param speed 播放速度，范围通常为 0.5 到 2.0
     */
    fun setPlaybackSpeed(speed: Float) {
        mediaController?.let { controller ->
            val params = controller.playbackParameters
            controller.playbackParameters = params.withSpeed(speed)
            _playbackSpeed.value = speed
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法设置播放速度")
        }
    }

    /**
     * 设置播放器音高
     * @param pitch 音高值，范围通常为 0.5 到 2.0
     */
    fun setPitch(pitch: Float) {
        mediaController?.let { controller ->
            val params = controller.playbackParameters
            controller.playbackParameters = params.withPitch(pitch)
            _pitch.value = pitch
        } ?: run {
            Logger.warn("PlayerController", "媒体控制器未连接，无法设置音高")
        }
    }
}