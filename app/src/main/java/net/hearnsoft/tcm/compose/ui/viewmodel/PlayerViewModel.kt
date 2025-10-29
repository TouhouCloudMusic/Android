package net.hearnsoft.tcm.compose.ui.viewmodel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.UnstableSaltUiApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.data.repository.MusicRepository
import net.hearnsoft.tcm.compose.domain.model.album.AlbumSortingRule
import net.hearnsoft.tcm.compose.domain.model.album.AlbumSortingStrategy
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingRule
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy
import net.hearnsoft.tcm.compose.utils.LocalMusicSorter
import net.hearnsoft.tcm.compose.utils.Logger
import net.hearnsoft.tcm.compose.utils.PlayerController
import net.hearnsoft.tcm.compose.utils.PlayerFavoriteBridge
import javax.inject.Inject

/**
 * 播放器视图模型
 * 负责管理播放列表数据、排序逻辑和与 PlayerController 的交互
 */
@HiltViewModel
@UnstableApi
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
class PlayerViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val TAG = "PlayerViewModel"

    // === 播放列表数据 ===
    // 原始数据源，直接从数据库获取，不参与排序逻辑
    private val _rawSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    // 给UI层使用的排序和过滤后的数据
    private val _allSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val allSongs: StateFlow<List<SongEntity>> = _allSongs.asStateFlow()

    // 当前播放列表
    val currentPlaylist: StateFlow<List<MediaItem>> = playerController.currentPlaylist

    // === 专辑数据 ===
    private val _rawAlbums = MutableStateFlow<List<AlbumEntity>>(emptyList())
    private val _allAlbums = MutableStateFlow<List<AlbumEntity>>(emptyList())
    val allAlbums: StateFlow<List<AlbumEntity>> = _allAlbums.asStateFlow()

    // === 排序和过滤状态 ===
    private val _currentSongSortingRule = MutableStateFlow(
        SongSortingRule(SongSortingStrategy.Title, false)
    )
    val currentSongSortingRule: StateFlow<SongSortingRule> = _currentSongSortingRule.asStateFlow()

    // === 专辑排序状态 ===
    private val _currentAlbumSortingRule = MutableStateFlow(
        AlbumSortingRule(AlbumSortingStrategy.AlbumName, false)
    )
    val currentAlbumSortingRule: StateFlow<AlbumSortingRule> = _currentAlbumSortingRule.asStateFlow()

    // === 播放器状态（从 PlayerController 获取） ===
    val isConnected = playerController.isConnected
    val isPlaying = playerController.isPlaying
    val currentMediaItem = playerController.currentMediaItem
    val currentMediaItemIndex = playerController.currentMediaItemIndex
    val repeatMode = playerController.repeatMode
    val shuffleModeEnabled = playerController.shuffleModeEnabled
    val isFavorite = playerController.isFavorite
    // 播放器音高和速度
    val playbackSpeed = playerController.playbackSpeed
    val pitch = playerController.pitch

    // 歌词
    val lyrics = playerController.lyrics
    val lyricsFormat = playerController.lyricsFormat

    // === UI 状态 ===
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // === 扫描进度状态 ===
    private val _scanProgress = MutableStateFlow<String?>(null)
    val scanProgress: StateFlow<String?> = _scanProgress.asStateFlow()

    private val _scanCompleted = MutableStateFlow(false)
    val scanCompleted: StateFlow<Boolean> = _scanCompleted.asStateFlow()

    // === 播放进度相关 ===
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var positionUpdateJob: Job? = null

    init {
        // 连接播放器控制器
        playerController.connect()

        // 开始位置更新
        startPositionUpdates()

        // 监听排序规则变化
        observeSortingRuleChanges()

        // 加载所有歌曲
        loadAllSongs()

        // 加载所有专辑
        loadAllAlbums()

        // 监听当前歌曲，并进行播放次数增加
        observeToIncrementPlayCount()
    }

    private fun observeSortingRuleChanges() {
        // 歌曲排序规则监听
        viewModelScope.launch(Dispatchers.Default) {
            _currentSongSortingRule.collectLatest { sortingRule ->
                // 每当排序规则变化时，重新应用排序和过滤
                val currentSongs = _rawSongs.value
                if (currentSongs.isNotEmpty()) {
                    applySongSort(currentSongs, sortingRule)
                }
            }
        }

        // 专辑排序规则监听
        viewModelScope.launch(Dispatchers.Default) {
            _currentAlbumSortingRule.collectLatest { sortingRule ->
                val currentAlbums = _rawAlbums.value
                if (currentAlbums.isNotEmpty()) {
                    applyAlbumSort(currentAlbums, sortingRule)
                }
            }
        }
    }

    /**
     * 应用排序逻辑
     * @param songs 要排序的歌曲列表
     * @param sortingRule 排序规则
     */
    private fun applySongSort(
        songs: List<SongEntity>,
        sortingRule: SongSortingRule
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // 将CPU密集型任务切换到后台线程
                val sortedSongs = withContext(Dispatchers.Default) {
                    // 使用 LocalMusicSorter 的 SongEntity 版本进行排序
                    LocalMusicSorter.sortMusicList(songs, sortingRule)
                }
                // 更新状态
                _allSongs.value = sortedSongs
                Logger.debug(TAG, "应用排序: ${sortedSongs.size} 首歌曲")
            } catch (e: Exception) {
                Logger.err(TAG, "应用排序时出错: ${e.message}")
            } finally {
                _isLoading.value = false // 结束时关闭加载状态
            }
        }
    }

    // 专辑排序方法
    private fun applyAlbumSort(
        albums: List<AlbumEntity>,
        sortingRule: AlbumSortingRule
    ) {
        viewModelScope.launch {
            try {
                val sortedAlbums = withContext(Dispatchers.Default) {
                    when (sortingRule.strategy) {
                        AlbumSortingStrategy.AlbumName -> {
                            if (sortingRule.reverse) {
                                albums.sortedByDescending { it.albumName }
                            } else {
                                albums.sortedBy { it.albumName }
                            }
                        }
                        AlbumSortingStrategy.SongCount -> {
                            if (sortingRule.reverse) {
                                albums.sortedByDescending { it.songCount }
                            } else {
                                albums.sortedBy { it.songCount }
                            }
                        }
                        AlbumSortingStrategy.AlbumYear -> {
                            if (sortingRule.reverse) {
                                albums.sortedByDescending { it.albumYear }
                            } else {
                                albums.sortedBy { it.albumYear }
                            }
                        }
                    }
                }

                _allAlbums.value = sortedAlbums
                Logger.debug(TAG, "应用专辑排序: ${sortedAlbums.size} 张专辑")
            } catch (e: Exception) {
                Logger.err(TAG, "应用专辑排序时出错: ${e.message}")
            }
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                if (playerController.isPlayerAvailable()) {
                    _currentPosition.value = playerController.getCurrentPosition()
                    _duration.value = playerController.getDuration()
                }
                delay(100) // 每100毫秒更新一次
            }
        }
    }

    // === 数据加载方法 ===

    /**
     * 加载所有歌曲
     */
    fun loadAllSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val songs = musicRepository.getAllSongs().first()
                _rawSongs.value = songs
                Logger.debug(TAG, "加载了 ${songs.size} 首歌曲")

                // 应用当前排序规则
                applySongSort(songs, _currentSongSortingRule.value)
            } catch (e: Exception) {
                Logger.err(TAG, "加载歌曲失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载所有专辑
     */
    fun loadAllAlbums() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val albums = musicRepository.getAllAlbums().first()
                _rawAlbums.value = albums
                Logger.debug(TAG, "加载了 ${albums.size} 张专辑")
                // 应用当前专辑排序规则
                applyAlbumSort(albums, _currentAlbumSortingRule.value)
            } catch (e: Exception) {
                Logger.err(TAG, "加载专辑失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 扫描并更新音乐库
     */
    fun scanAndUpdateMusicLibrary() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _scanCompleted.value = false

                // 调用仓库层方法，传入进度回调
                musicRepository.scanAndUpdateLibrary { progress ->
                    _scanProgress.value = progress
                }
                Logger.debug(TAG, "音乐库扫描更新完成")

                _scanProgress.value = null
                _scanCompleted.value = true

                // 重新加载歌曲列表
                loadAllSongs()
                loadAllAlbums()
            } catch (e: Exception) {
                Logger.err(TAG, "扫描音乐库失败: ${e.message}")
                _scanProgress.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 重置扫描完成状态
     */
    fun resetScanCompleted() {
        _scanCompleted.value = false
    }

    // === 播放控制方法 ===

    /**
     * 播放当前播放列表
     */
    fun playCurrentPlaylist(startIndex: Int = 0) {
        val playlist = playerController.currentPlaylist.value
        if (playlist.isNotEmpty()) {
            playerController.setPlaylist(playlist, startIndex)
            playerController.play()
        }
    }

    /**
     * 播放特定歌曲
     */
    fun playSong(songEntity: SongEntity) {
        val mediaItem = convertSongEntityToMediaItem(songEntity)
        val actualPlaylist = playerController.currentPlaylist.value.toMutableList()
        val currentIndex = playerController.getCurrentMediaItemIndex()

        // 检查歌曲是否已在当前播放列表内
        val existingIndex = actualPlaylist.indexOfFirst { it.mediaId == mediaItem.mediaId }

        if (existingIndex >= 0) {
            // 歌曲已存在，直接跳转到该歌曲
            playerController.seekToMediaItem(existingIndex)
            playerController.play()
            Logger.debug(TAG, "歌曲已在播放列表中，跳转到索引 $existingIndex")
        } else {
            // 歌曲不存在，添加到当前播放歌曲的下一个位置
            val insertIndex : Int

            // 插入歌曲
            if (actualPlaylist.isEmpty()) {
                // 播放列表为空，直接添加
                insertIndex = 0
                actualPlaylist.add(mediaItem)
            } else {
                // 播放列表不为空，插入到当前播放位置的下一个位置
                insertIndex = if (currentIndex >= 0) {
                    currentIndex + 1
                } else {
                    0
                }
                actualPlaylist.add(insertIndex, mediaItem)
            }

            // 重新设置播放列表并跳转到新添加的歌曲
            playerController.setPlaylist(actualPlaylist, insertIndex)
            playerController.play()

            Logger.debug(TAG, "插入歌曲到位置 $insertIndex 并播放: ${songEntity.title}")
        }
        
        Logger.debug(TAG, "播放歌曲: ${songEntity.title}, 播放列表大小: ${actualPlaylist.size}")
    }

    /**
     * 播放特定歌曲（MediaItem 版本）
     */
    fun playSong(mediaItem: MediaItem) {
        val actualPlaylist = playerController.currentPlaylist.value.toMutableList()
        val currentIndex = playerController.getCurrentMediaItemIndex()

        // 检查歌曲是否已在播放列表中
        val existingIndex = actualPlaylist.indexOfFirst { it.mediaId == mediaItem.mediaId }

        if (existingIndex >= 0) {
            // 歌曲已存在，直接跳转播放
            playerController.seekToMediaItem(existingIndex)
            playerController.play()
            Logger.debug(TAG, "歌曲已存在于播放列表，跳转到索引 $existingIndex 播放")
        } else {
            // 歌曲不存在，添加到当前播放歌曲的下一个位置
            val insertIndex : Int

            // 插入歌曲
            if (actualPlaylist.isEmpty()) {
                // 播放列表为空，直接添加
                insertIndex = 0
                actualPlaylist.add(mediaItem)
            } else {
                // 播放列表不为空，插入到当前播放位置的下一个位置
                insertIndex = if (currentIndex >= 0) {
                    currentIndex + 1
                } else {
                    0
                }
                actualPlaylist.add(insertIndex, mediaItem)
            }

            // 重新设置播放列表，并从插入的位置开始播放
            playerController.setPlaylist(actualPlaylist, insertIndex)
            playerController.play()

            Logger.debug(TAG, "插入歌曲到位置 $insertIndex 并播放: ${mediaItem.mediaMetadata.title}")
        }
    }

    /**
     * 播放指定索引的歌曲
     */
    fun playAtIndex(index: Int) {
        playerController.seekToMediaItem(index)
        playerController.play()
        Logger.debug(TAG, "播放索引 $index 的歌曲")
    }

    /**
     * 添加歌曲到当前播放列表中正在播放的下一个位置（无缝添加）
     * @param songEntity 要添加的歌曲实体
     */
    fun addToPlayNext(songEntity: SongEntity) {
        val mediaItem = convertSongEntityToMediaItem(songEntity)
        val currentIndex = playerController.getCurrentMediaItemIndex()

        if (currentIndex < 0) {
            // 没有正在播放的歌曲，添加到末尾
            Logger.debug(TAG, "没有正在播放的歌曲，添加到播放列表末尾")
            playerController.addMediaItemToPlaylist(mediaItem, -1)
            return
        }

        val insertIndex = currentIndex + 1

        playerController.addMediaItemToPlaylist(mediaItem, insertIndex)

        Logger.debug(TAG, "添加歌曲到下一首播放（位置 $insertIndex）: ${mediaItem.mediaMetadata.title}")
    }

    /**
     * 从播放列表移除指定歌曲
     */
    fun removeFromPlaylist(mediaItem: MediaItem) {
        val actualPlaylist = playerController.currentPlaylist.value.toMutableList()
        val indexToRemove = actualPlaylist.indexOfFirst { it.mediaId == mediaItem.mediaId }

        if (indexToRemove >= 0) {
            // 检查是否正在播放要移除的歌曲
            val isCurrentlyPlaying = playerController.currentMediaItem.value?.mediaId == mediaItem.mediaId
            
            if (isCurrentlyPlaying) {
                // 如果移除的是当前播放的歌曲，先跳到下一首
                playerController.skipToNext()
            }
            
            // 移除歌曲
            playerController.removeMediaItemFromPlaylist(indexToRemove)
            Logger.debug(TAG, "移除歌曲（位置 $indexToRemove），播放列表大小: ${actualPlaylist.size}")
        } else {
            Logger.warn(TAG, "尝试移除不存在的歌曲: ${mediaItem.mediaId}")
        }
    }

    /**
     * 清空播放列表
     */
    fun clearPlaylist() {
        playerController.clearPlaylist()
        Logger.debug(TAG, "播放列表已清空")
    }

    /**
     * 设置并播放指定的播放列表
     * @param songs 要播放的歌曲列表
     * @param startIndex 从列表中的哪个位置开始播放
     */
    fun setAndPlayPlaylist(songs: List<SongEntity>, startIndex: Int = 0) {
        if (songs.isEmpty()) {
            Logger.warn(TAG, "尝试设置空的播放列表")
            return
        }

        viewModelScope.launch {
            try {
                // 转换歌曲实体为 MediaItem
                val mediaItems = songs.map { convertSongEntityToMediaItem(it) }

                if (startIndex < mediaItems.size) {
                    playerController.setPlaylist(mediaItems, startIndex)
                    playerController.play()
                    Logger.debug(TAG, "设置并播放新的播放列表，包含 ${mediaItems.size} 首歌曲，从索引 $startIndex 开始播放")
                } else {
                    Logger.warn(TAG, "起始索引无效: $startIndex >= ${mediaItems.size}")
                }
            } catch (e: Exception) {
                Logger.err(TAG, "设置播放列表失败: ${e.message}")
            }
        }
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    /**
     * 下一首
     */
    fun skipToNext() {
        playerController.skipToNext()
    }

    /**
     * 上一首
     */
    fun skipToPrevious() {
        playerController.skipToPrevious()
    }

    /**
     * 跳转到指定位置
     */
    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    /**
     * 切换重复模式
     */
    fun toggleRepeatMode() {
        playerController.toggleRepeatMode()
    }

    /**
     * 切换随机播放
     */
    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    // === 排序和过滤方法 ===

    /**
     * 更新排序规则
     */
    fun updateSongSortingRule(rule: SongSortingRule) {
        _currentSongSortingRule.value = rule
        Logger.debug(TAG, "更新排序规则: ${rule.strategy}, 倒序: ${rule.reverse}")
    }

    // 专辑排序规则更新方法
    fun updateAlbumSortingRule(rule: AlbumSortingRule) {
        _currentAlbumSortingRule.value = rule
        Logger.debug(TAG, "更新专辑排序规则: ${rule.strategy}, 倒序: ${rule.reverse}")
    }

    /**
     * 重新加载所有歌曲
     */
    fun reloadAllSongs() {
        loadAllSongs()
    }

    // === 播放统计方法 ===

    /**
     * 增加播放次数
     */
    fun observeToIncrementPlayCount() {
        viewModelScope.launch {
            currentMediaItem.collectLatest { mediaItem ->
                mediaItem?.let {
                    val mediaId = it.mediaId.toLongOrNull()
                    if (mediaId != null) {
                        try {
                            musicRepository.incrementPlayCount(mediaId)
                            Logger.debug(TAG, "增加播放次数: $mediaId")
                        } catch (e: Exception) {
                            Logger.err(TAG, "增加播放次数失败: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    fun toggleCurrentSongFavorite() {
        viewModelScope.launch {
            val mediaItem = currentMediaItem.value
            if (mediaItem != null) {
                val mediaId = mediaItem.mediaId.toLongOrNull()
                if (mediaId != null) {
                    val newStatus = !isFavorite.value
                    updateFavoriteStatus(mediaId, newStatus)
                }
            }
        }
    }

    /**
     * 更新收藏状态
     */
    fun updateFavoriteStatus(songId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                musicRepository.updateFavoriteStatus(songId, isFavorite)
                PlayerFavoriteBridge.update(isFavorite)
                Logger.debug(TAG, "更新收藏状态: $songId -> $isFavorite")
            } catch (e: Exception) {
                Logger.err(TAG, "更新收藏状态失败: ${e.message}")
            }
        }
    }

    // === 播放器播放状态方法 ===
    /**
     * 设置播放器音高
     * @param pitch 音高值，1.0 为正常音高
     */
    fun setPlayerPitch(pitch: Float) {
        playerController.setPitch(pitch)
        Logger.debug(TAG, "设置播放器音高: $pitch")
    }

    /**
     * 设置播放器播放速度
     * @param speed 播放速度，1.0 为正常速度
     */
    fun setPlayerSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
        Logger.debug(TAG, "设置播放器播放速度: $speed")
    }


    // === 辅助方法 ===

    /**
     * 将 SongEntity 转换为 MediaItem
     */
    private fun convertSongEntityToMediaItem(songEntity: SongEntity): MediaItem {
        return MediaItem.Builder()
            .setMediaId(songEntity.mediaStoreId.toString())
            .setUri(songEntity.contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(songEntity.title)
                    .setArtist(songEntity.artistName ?: "Unknown Artist")
                    .setAlbumTitle(songEntity.albumName ?: "Unknown Album")
                    .setArtworkUri(songEntity.artworkUri)
                    .setDurationMs(songEntity.duration)
                    .build()
            )
            .build()
    }

    /**
     * 根据 MediaItem 查找对应的 SongEntity
     * @param mediaItem 媒体项
     * @return 对应的歌曲实体，如果未找到返回 null
     */
    suspend fun getSongEntityByMediaItem(mediaItem: MediaItem) : SongEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val mediaId = mediaItem.mediaId.toLongOrNull()
                if (mediaId == null) {
                    Logger.err(TAG, "无效的 MediaItem ID: ${mediaItem.mediaId}")
                    return@withContext null
                }
                musicRepository.getSongByMediaStoreId(mediaId)
            } catch (e: Exception) {
                Logger.err(TAG, "查询歌曲实体失败: ${e.message}")
                null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionUpdateJob?.cancel()
        playerController.disconnect()
    }
}