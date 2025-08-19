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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.data.repository.MusicRepository
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingRule
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy
import net.hearnsoft.tcm.compose.utils.LocalMusicScanner
import net.hearnsoft.tcm.compose.utils.Logger
import net.hearnsoft.tcm.compose.utils.PlayerController
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
    private val _allSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val allSongs: StateFlow<List<SongEntity>> = _allSongs.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentPlaylist: StateFlow<List<MediaItem>> = _currentPlaylist.asStateFlow()

    private val _filteredSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val filteredSongs: StateFlow<List<SongEntity>> = _filteredSongs.asStateFlow()

    // === 排序和过滤状态 ===
    private val _currentSortingRule = MutableStateFlow(
        SongSortingRule(SongSortingStrategy.Title, false)
    )
    val currentSortingRule: StateFlow<SongSortingRule> = _currentSortingRule.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // === 播放器状态（从 PlayerController 获取） ===
    val isConnected = playerController.isConnected
    val isPlaying = playerController.isPlaying
    val currentMediaItem = playerController.currentMediaItem
    val repeatMode = playerController.repeatMode
    val shuffleModeEnabled = playerController.shuffleModeEnabled

    // 歌词
    val lyrics = playerController.lyrics
    val lyricsFormat = playerController.lyricsFormat

    // === UI 状态 ===
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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

        // 监听数据变化并应用排序
        observeDataChanges()

        // 加载所有歌曲
        loadAllSongs()
    }

    private fun observeDataChanges() {
        viewModelScope.launch {
            combine(
                _allSongs,
                _currentSortingRule,
                _searchQuery
            ) { songs, sortingRule, query ->
                Triple(songs, sortingRule, query)
            }.collectLatest { (songs, sortingRule, query) ->
                applyFilterAndSort(songs, sortingRule, query)
            }
        }
    }

    private fun applyFilterAndSort(
        songs: List<SongEntity>,
        sortingRule: SongSortingRule,
        query: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // 将CPU密集型任务切换到后台线程
                val (filteredSongs, sortedMediaItems) = withContext(Dispatchers.Default) {
                    // 先过滤
                    val filtered = if (query.isBlank()) {
                        songs
                    } else {
                        songs.filter { song ->
                            song.title.contains(query, ignoreCase = true) ||
                                    song.artistName.contains(query, ignoreCase = true) ||
                                    song.albumName.contains(query, ignoreCase = true)
                        }
                    }

                    // 转换为 MediaItem 进行排序
                    val mediaItems = filtered.map { songEntity ->
                        convertSongEntityToMediaItem(songEntity)
                    }

                    // 应用排序
                    val sorted = LocalMusicScanner.sortMusicList(mediaItems, sortingRule)
                    Pair(filtered, sorted)
                }

                // 更新状态
                _filteredSongs.value = filteredSongs
                _currentPlaylist.value = sortedMediaItems

                Logger.debug(TAG, "应用过滤和排序: ${filteredSongs.size} 首歌曲")
            } catch (e: Exception) {
                Logger.err(TAG, "应用过滤和排序时出错: ${e.message}")
                _errorMessage.value = "排序失败: ${e.message}"
            } finally {
                _isLoading.value = false // 结束时关闭加载状态
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
                delay(100) // 每秒更新一次
            }
        }
    }

    // === 数据加载方法 ===

    /**
     * 加载所有歌曲
     */
    fun loadAllSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            musicRepository.getAllSongs()
                .catch { e ->
                    Logger.err(TAG, "加载歌曲失败: ${e.message}")
                    _errorMessage.value = "加载歌曲失败: ${e.message}"
                    _isLoading.value = false // 确保在出错时也更新状态
                }
                .collectLatest { songs ->
                    _allSongs.value = songs
                    Logger.debug(TAG, "加载了 ${songs.size} 首歌曲")

                    // 收到第一次数据后，就认为加载完成
                    if (_isLoading.value) {
                        _isLoading.value = false
                    }
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
                musicRepository.scanAndUpdateLibrary()
                Logger.debug(TAG, "音乐库扫描更新完成")
            } catch (e: Exception) {
                Logger.err(TAG, "扫描音乐库失败: ${e.message}")
                _errorMessage.value = "扫描失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === 播放控制方法 ===

    /**
     * 播放当前播放列表
     */
    fun playCurrentPlaylist(startIndex: Int = 0) {
        val playlist = _currentPlaylist.value
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
        val currentPlaylist = _currentPlaylist.value.toMutableList()

        // 如果歌曲不在当前播放列表中，添加它
        val existingIndex = currentPlaylist.indexOfFirst { it.mediaId == mediaItem.mediaId }
        val playIndex = if (existingIndex >= 0) {
            existingIndex
        } else {
            currentPlaylist.add(0, mediaItem)
            _currentPlaylist.value = currentPlaylist
            0
        }

        playerController.setPlaylist(currentPlaylist, playIndex)
        playerController.play()
    }

    /**
     * 播放特定歌曲
     */
    fun playSong(mediaItem: MediaItem) {
        val currentPlaylist = _currentPlaylist.value.toMutableList()

        // 如果歌曲不在当前播放列表中，添加它
        val existingIndex = currentPlaylist.indexOfFirst { it.mediaId == mediaItem.mediaId }
        val playIndex = if (existingIndex >= 0) {
            existingIndex
        } else {
            currentPlaylist.add(0, mediaItem)
            _currentPlaylist.value = currentPlaylist
            0
        }

        playerController.setPlaylist(currentPlaylist, playIndex)
        playerController.play()
    }

    /**
     * 从播放列表移除指定歌曲
     */
    fun removeFromPlaylist(mediaItem: MediaItem) {
        val currentPlaylist = _currentPlaylist.value.toMutableList()
        val indexToRemove = currentPlaylist.indexOfFirst { it.mediaId == mediaItem.mediaId }

        if (indexToRemove >= 0) {
            currentPlaylist.removeAt(indexToRemove)
            _currentPlaylist.value = currentPlaylist

            // 如果当前播放的歌曲被移除，尝试播放下一首
            if (playerController.currentMediaItem.value?.mediaId == mediaItem.mediaId) {
                playerController.skipToNext()
            }
        } else {
            Logger.warn(TAG, "尝试移除不存在的歌曲: ${mediaItem.mediaId}")
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
    fun updateSortingRule(rule: SongSortingRule) {
        _currentSortingRule.value = rule
        Logger.debug(TAG, "更新排序规则: ${rule.strategy}, 倒序: ${rule.reverse}")
    }

    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        Logger.debug(TAG, "更新搜索查询: $query")
    }

    /**
     * 清除搜索
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * 重新加载所有歌曲
     */
    fun reloadAllSongs() {
        viewModelScope.launch {
            loadAllSongs()
        }
    }

    // === 播放统计方法 ===

    /**
     * 增加播放次数
     */
    fun incrementPlayCount(songId: Long) {
        viewModelScope.launch {
            try {
                musicRepository.incrementPlayCount(songId)
                Logger.debug(TAG, "更新播放次数: $songId")
            } catch (e: Exception) {
                Logger.err(TAG, "更新播放次数失败: ${e.message}")
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
                Logger.debug(TAG, "更新收藏状态: $songId -> $isFavorite")
            } catch (e: Exception) {
                Logger.err(TAG, "更新收藏状态失败: ${e.message}")
            }
        }
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
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        positionUpdateJob?.cancel()
        playerController.disconnect()
    }
}