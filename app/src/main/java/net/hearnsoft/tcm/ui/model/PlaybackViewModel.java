package net.hearnsoft.tcm.ui.model;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;

import net.hearnsoft.tcm.infrastructure.db.LocalMusicDatabase;
import net.hearnsoft.tcm.domain.repository.MusicRepository;
import net.hearnsoft.tcm.domain.model.song.SongSortingRule;
import net.hearnsoft.tcm.domain.model.song.SongSortingStrategy;
import net.hearnsoft.tcm.application.MusicPlaybackService;
import net.hearnsoft.tcm.utils.LocalMusicScanner;
import net.hearnsoft.tcm.infrastructure.logger.Logger;
import net.hearnsoft.tcm.utils.MusicPlayerController;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class PlaybackViewModel extends AndroidViewModel {
    private MusicPlayerController playerController;
    private MusicRepository musicRepository;
    
    private final MutableLiveData<MediaItem> currentMediaItem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    // 设备媒体库播放列表，默认为不要改动
    private final MutableLiveData<List<MediaItem>> playlist = new MutableLiveData<>();
    private final MutableLiveData<Long> currentPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> duration = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSorting = new MutableLiveData<>(false);
    private final MutableLiveData<SongSortingRule> currentSortRule = new MutableLiveData<>(new SongSortingRule(SongSortingStrategy.Title, false));
    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
    //当前PlayerController的播放列表，通常是由UI控件增删监控这个列表
    private final MutableLiveData<List<MediaItem>> currentPlaylist = new MutableLiveData<>();
    private final MutableLiveData<Integer> repeatMode = new MutableLiveData<>(0);
    // 随机播放状态标志
    private final MutableLiveData<Boolean> isShuffleMode = new MutableLiveData<>(false);
    // 歌词
    private final MutableLiveData<String> currentLyrics = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> showLyricsTranslate = new MutableLiveData<>(true);
    
    // 新增：数据库加载状态
    private final MutableLiveData<Boolean> isLoadingFromDatabase = new MutableLiveData<>(false);
    private final MutableLiveData<String> loadingStatus = new MutableLiveData<>("");

    // 标记控制器是否已连接
    private boolean isControllerActive = false;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updatePosition();
            progressHandler.postDelayed(this, 100); // 每100毫秒更新一次
        }
    };

    public PlaybackViewModel(@NonNull Application application) {
        super(application);
        playerController = MusicPlayerController.getInstance(application);

        // 初始化数据库和仓库
        LocalMusicDatabase database = LocalMusicDatabase.Companion.getDatabase(application);
        musicRepository = new MusicRepository(database.musicDao());
        MusicPlaybackService.setLyricsUpdateListener((lyrics, format) -> {
            currentLyrics.postValue(lyrics);
            Logger.debug("PlaybackViewModel", "Lyrics updated: " + lyrics);
        });

        connectToService();
    }

    private void connectToService() {
        if (isControllerActive) {
            return; // 如果已连接，避免重复连接
        }

        isControllerActive = true;

        playerController.connect(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@NonNull MediaItem mediaItem, int reason) {
                currentMediaItem.postValue(mediaItem);

                // 获取并更新当前索引
                if (playerController.getMediaController() != null) {
                    int currentIdx = playerController.getMediaController().getCurrentMediaItemIndex();
                    currentIndex.postValue(currentIdx);
                    
                    // 当媒体项切换时，重新获取duration
                    long durationValue = playerController.getMediaController().getDuration();
                    // 确保duration不为负值
                    if (durationValue < 0) {
                        durationValue = 0L;
                    }
                    duration.postValue(durationValue);
                    // 如果之前有进度更新任务，先移除
                    progressHandler.removeCallbacks(progressUpdateRunnable);
                    // 启动新的进度更新任务
                    progressHandler.post(progressUpdateRunnable);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playerController.getMediaController() != null) {
                    long durationValue = playerController.getMediaController().getDuration();
                    // 确保duration不为负值
                    if (durationValue < 0) {
                        durationValue = 0L;
                    }
                    duration.postValue(durationValue);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean playing) {
                isPlaying.postValue(playing);
            }

            @Override
            public void onPlaylistMetadataChanged(MediaMetadata mediaMetadata) {
                Player.Listener.super.onPlaylistMetadataChanged(mediaMetadata);
                if (playerController.getMediaController() != null) {
                    updatePlaylist();
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                PlaybackViewModel.this.repeatMode.postValue(repeatMode);
            }
        });

        // 初始化状态
        if (playerController.getMediaController() != null) {
            MediaItem item = playerController.getMediaController().getCurrentMediaItem();
            if (item != null) {
                currentMediaItem.postValue(item);
            }
            isPlaying.postValue(playerController.getMediaController().isPlaying());
            
            long durationValue = playerController.getMediaController().getDuration();
            // 确保duration不为负值
            if (durationValue < 0) {
                durationValue = 0L;
            }
            duration.postValue(durationValue);

            // 初始化播放模式
            repeatMode.postValue(playerController.getMediaController().getRepeatMode());

            // 初始化当前播放列表
            updatePlaylist();
        }
    }

    // 确保控制器已连接
    public void ensureControllerConnected() {
        if (playerController.getMediaController() == null) {
            connectToService();
        }
    }

    public void scanAndLoadMusic(Context context) {
        // 保持向后兼容，但建议使用 loadMusicLibrary
        loadMusicLibrary(context);
    }

    /**
     * 检查是否有存储权限
     */
    public boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) 
                == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * 只有在有权限的情况下才加载音乐库
     */
    public void loadMusicLibraryIfPermitted(Context context) {
        if (hasStoragePermission(context)) {
            loadMusicLibrary(context);
        } else {
            Logger.warn("PlaybackViewModel", "存储权限未授予，无法加载音乐库");
            loadingStatus.postValue("需要存储权限才能加载音乐");
        }
    }

    /**
     * 加载音乐库 - 首先尝试从数据库加载，如果为空则扫描设备
     */
    public void loadMusicLibrary(Context context) {
        isLoadingFromDatabase.postValue(true);
        loadingStatus.postValue("正在加载音乐库...");
        
        // 首先检查数据库中是否有音乐数据
        musicRepository.getMusicCountAsync()
            .thenCompose(musicCount -> {
                if (musicCount > 0) {
                    // 从数据库加载
                    loadingStatus.postValue("从数据库加载音乐...");
                    return musicRepository.loadMusicFromDatabaseAsync();
                } else {
                    // 数据库为空，执行首次扫描
                    loadingStatus.postValue("首次启动，正在扫描设备音乐...");
                    isScanning.postValue(true);
                    return musicRepository.scanAndSaveMusicAsync(context);
                }
            })
            .thenAccept(musicList -> {
                playlist.postValue(musicList);
                loadingStatus.postValue("音乐库加载完成");
                Logger.debug("PlaybackViewModel", "加载了 " + musicList.size() + " 首音乐");
            })
            .exceptionally(throwable -> {
                Logger.err("PlaybackViewModel", "加载音乐库时出错:", throwable);
                loadingStatus.postValue("加载音乐库失败: " + throwable.getMessage());
                return null;
            })
            .whenComplete((result, throwable) -> {
                isLoadingFromDatabase.postValue(false);
                isScanning.postValue(false);
            });
    }

    /**
     * 刷新音乐库 - 重新扫描设备并更新数据库
     */
    public void refreshMusicLibrary(Context context) {
        isScanning.postValue(true);
        loadingStatus.postValue("正在刷新音乐库...");
        
        musicRepository.refreshMusicLibraryAsync(context)
            .thenAccept(refreshedMusic -> {
                playlist.postValue(refreshedMusic);
                loadingStatus.postValue("音乐库刷新完成");
                Logger.debug("PlaybackViewModel", "刷新了 " + refreshedMusic.size() + " 首音乐");
            })
            .exceptionally(throwable -> {
                Logger.err("PlaybackViewModel", "刷新音乐库时出错", throwable);
                loadingStatus.postValue("刷新音乐库失败: " + throwable.getMessage());
                return null;
            })
            .whenComplete((result, throwable) -> {
                isScanning.postValue(false);
            });
    }

    // 排序方法
    public void sortMusic(SongSortingRule rule) {
        // 记录当前排序规则
        currentSortRule.postValue(rule);

        // 如果没有音乐项目，直接返回
        if (playlist.getValue() == null || playlist.getValue().isEmpty()) {
            return;
        }

        isSorting.postValue(true);

        // 在后台线程执行排序
        new Thread(() -> {
            List<MediaItem> currentList = playlist.getValue();
            List<MediaItem> sortedList = LocalMusicScanner.sortMusicList(currentList, rule);

            // 更新排序后的列表
            if (sortedList != null) {
                playlist.postValue(sortedList);
            }

            isSorting.postValue(false);
        }).start();
    }

    private void updatePlaylist() {
        if (playerController.getMediaController() == null) return;

        int count = playerController.getMediaController().getMediaItemCount();
        if (count > 0) {
            // 获取当前播放列表
            List<MediaItem> items = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                items.add(playerController.getMediaController().getMediaItemAt(i));
            }
            playlist.postValue(items);
        }
    }

    public void updatePosition() {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            long position = playerController.getMediaController().getCurrentPosition();
            // 确保position不为负值
            if (position < 0) {
                position = 0L;
            }
            currentPosition.postValue(position);

            // 同时更新当前索引
            int index = playerController.getMediaController().getCurrentMediaItemIndex();
            if (currentIndex.getValue() == null || currentIndex.getValue() != index) {
                currentIndex.postValue(index);
            }
        }
    }

    // 更新当前播放的媒体索引
    public void updateCurrentIndex() {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            int index = playerController.getMediaController().getCurrentMediaItemIndex();
            currentIndex.postValue(index);
        }
    }

    public void clearCurrentPlaylist() {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            // 清空当前播放列表
            playerController.getMediaController().clearMediaItems();
            // 更新LiveData
            currentPlaylist.postValue(new ArrayList<>());
            // 重置当前索引
            currentIndex.postValue(0);
        }
    }

    public void setRepeatMode(int repeatMode) {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            playerController.getMediaController().setRepeatMode(repeatMode);
            this.repeatMode.postValue(repeatMode);
        }
    }

    public void toggleRepeatMode() {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            int currentMode = playerController.getMediaController().getRepeatMode();
            Boolean currentShuffleState = isShuffleMode.getValue();
            boolean isCurrentlyShuffle = currentShuffleState != null && currentShuffleState;
            
            int newMode;
            boolean newShuffleState = false;
            
            // 如果当前是随机播放模式
            if (isCurrentlyShuffle) {
                // 从随机播放切换到单曲循环
                newMode = Player.REPEAT_MODE_ONE;
                newShuffleState = false;
            } else {
                switch (currentMode) {
                    case Player.REPEAT_MODE_OFF:
                        newMode = Player.REPEAT_MODE_ONE;
                        break;
                    case Player.REPEAT_MODE_ONE:
                        newMode = Player.REPEAT_MODE_ALL;
                        break;
                    case Player.REPEAT_MODE_ALL:
                    default:
                        // 从列表循环切换到随机播放
                        newMode = Player.REPEAT_MODE_OFF;
                        newShuffleState = true;
                        // 执行随机播放
                        shuffleCurrentPlaylist();
                        break;
                }
            }
            
            playerController.getMediaController().setRepeatMode(newMode);
            this.repeatMode.postValue(newMode);
            this.isShuffleMode.postValue(newShuffleState);
        }
    }

    /**
     * 打乱当前播放列表并开始播放
     * 当前正在播放的歌曲会被放在打乱后的列表的开头
     * @return 返回打乱后的列表
     */
    public void shuffleCurrentPlaylist() {
        List<MediaItem> currentItems = currentPlaylist.getValue();
        if (currentItems == null || currentItems.isEmpty()) {
            return;
        }

        // 获取当前播放的媒体项
        Integer currentIdx = currentIndex.getValue();
        MediaItem currentItem = null;
        if (currentIdx != null && currentIdx < currentItems.size()) {
            currentItem = currentItems.get(currentIdx);
        }

        // 创建列表副本进行打乱
        List<MediaItem> shuffledList = new ArrayList<>(currentItems);

        // 如果有当前播放的项，将其从要打乱的列表中移除
        if (currentItem != null) {
            shuffledList.remove(currentItem);
        }

        // 打乱列表
        java.util.Collections.shuffle(shuffledList);

        // 如果有当前播放的项，将其放在列表开头
        if (currentItem != null) {
            shuffledList.add(0, currentItem);
        }

        // 保存打乱后的列表
        currentPlaylist.postValue(shuffledList);

        // 将打乱后的列表设置回播放器，并保持当前播放的歌曲在第0位
        if (currentItem != null) {
            playMusic(shuffledList, 0, true);
            currentIndex.postValue(0);
        } else {
            // 如果没有当前播放项，则正常播放第一首
            playMusic(shuffledList, 0, true);
            currentIndex.postValue(0);
        }
    }

    public void skipToQueueItem(int position) {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            List<MediaItem> items = currentPlaylist.getValue();
            if (items != null && position >= 0 && position < items.size()) {
                playerController.getMediaController().seekToDefaultPosition(position);
                // 更新当前索引
                currentIndex.postValue(position);
            }
        }
    }

    public void removeFromCurrentPlaylistByPosition(int position) {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            List<MediaItem> items = currentPlaylist.getValue();
            if (items != null && position >= 0 && position < items.size()) {
                // 获取当前索引
                int currentIdx = currentIndex.getValue() != null ? currentIndex.getValue() : 0;

                // 从播放器控制器中移除该项
                playerController.getMediaController().removeMediaItem(position);

                // 更新本地列表
                List<MediaItem> newList = new ArrayList<>(items);
                newList.remove(position);
                currentPlaylist.postValue(newList);

                // 调整当前播放索引
                if (position < currentIdx) {
                    // 如果删除的是当前播放项之前的项，索引需要减1
                    currentIndex.postValue(currentIdx - 1);
                } else if (position == currentIdx && position >= newList.size()) {
                    // 如果删除的是当前播放的最后一项，索引需要调整到列表的末尾
                    currentIndex.postValue(Math.max(0, newList.size() - 1));
                }
                // 其他情况索引不变
            }
        }
    }
    
    public void playMusic(List<MediaItem> playlist, int startIndex) {
        playMusic(playlist, startIndex, false);
    }

    public void playMusic(List<MediaItem> playlist, int startIndex, boolean isShuffleCall) {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            playerController.playMusic(playlist, startIndex);
            this.playlist.postValue(playlist);
            this.currentIndex.postValue(startIndex);
            // 将当前设备媒体列表同步给当前播放列表
            this.currentPlaylist.postValue(new ArrayList<>(playlist));
            
            // 如果不是随机播放调用，则重置随机播放状态
            if (!isShuffleCall) {
                this.isShuffleMode.postValue(false);
            }
        }
    }

    public void togglePlayPause() {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            playerController.togglePlayPause();
        }
    }

    public void togglePlayPause(boolean isPlaying) {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            playerController.togglePlayPause(isPlaying);
        }
    }

    public void seekTo(long positionMs) {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            playerController.getMediaController().seekTo(positionMs);
            currentPosition.postValue(positionMs);
        }
    }

    public void playNext() {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            if (playerController.getMediaController().hasNextMediaItem()) {
                playerController.getMediaController().seekToNext();
                // 更新索引
                updateCurrentIndex();
            }
        }
    }

    public void playPrevious() {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            if (playerController.getMediaController().hasPreviousMediaItem()) {
                playerController.getMediaController().seekToPrevious();
                // 更新索引
                updateCurrentIndex();
            } else {
                // 如果没有上一首，就重头开始播放当前歌曲
                playerController.getMediaController().seekTo(0);
            }
        }
    }

    public void toggleShowLyricsTranslate() {
        boolean currentState = showLyricsTranslate.getValue() != null && showLyricsTranslate.getValue();
        showLyricsTranslate.postValue(!currentState);
    }

    // 获取LiveData
    public LiveData<MediaItem> getCurrentMediaItem() {
        return currentMediaItem;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public LiveData<List<MediaItem>> getPlaylist() {
        return playlist;
    }

    public LiveData<Long> getCurrentPosition() {
        return currentPosition;
    }

    public LiveData<Long> getDuration() {
        return duration;
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    // Getter方法
    public LiveData<Boolean> getIsSorting() {
        return isSorting;
    }

    public LiveData<SongSortingRule> getCurrentSortRule() {
        return currentSortRule;
    }

    public LiveData<Integer> getCurrentIndex() {
        return currentIndex;
    }

    // 获取当前播放列表的LiveData
    public LiveData<List<MediaItem>> getCurrentPlaylist() {
        return currentPlaylist;
    }

    // 获取重复模式
    public LiveData<Integer> getRepeatMode() {
        return repeatMode;
    }

    // 获取随机播放状态
    public LiveData<Boolean> getIsShuffleMode() {
        return isShuffleMode;
    }

    // 获取当前歌词
    public LiveData<String> getCurrentLyrics() {
        return currentLyrics;
    }

    // 获取歌词翻译显示状态
    public LiveData<Boolean> getShowLyricsTranslate() {
        return showLyricsTranslate;
    }
    
    // 新增Getter方法
    public LiveData<Boolean> getIsLoadingFromDatabase() {
        return isLoadingFromDatabase;
    }
    
    public LiveData<String> getLoadingStatus() {
        return loadingStatus;
    }

    /**
     * 搜索音乐
     */
    public void searchMusic(String query, androidx.lifecycle.Observer<List<MediaItem>> observer) {
        if (query == null || query.trim().isEmpty()) {
            // 如果查询为空，返回完整列表
            if (observer != null) {
                observer.onChanged(playlist.getValue());
            }
            return;
        }
        
        musicRepository.searchMusicAsync(query.trim())
            .thenAccept(searchResults -> {
                // 在主线程中回调结果
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (observer != null) {
                        observer.onChanged(searchResults);
                    }
                });
            })
            .exceptionally(throwable -> {
                Logger.err("PlaybackViewModel", "搜索音乐时出错", throwable);
                return null;
            });
    }

    @Override
    protected void onCleared() {
        isControllerActive = false;
        playerController.release();
        MusicPlaybackService.setLyricsUpdateListener(null);
        progressHandler.removeCallbacks(progressUpdateRunnable);
        super.onCleared();
    }

}
