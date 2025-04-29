package net.hearnsoft.tcm.ui.model;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;

import net.hearnsoft.tcm.domain.model.song.SongSortingRule;
import net.hearnsoft.tcm.domain.model.song.SongSortingStrategy;
import net.hearnsoft.tcm.domain.repository.AlistMusicRepository;
import net.hearnsoft.tcm.domain.repository.LocalMusicRepository;
import net.hearnsoft.tcm.domain.repository.MediaRepository;
import net.hearnsoft.tcm.domain.repository.MediaRepositoryManager;
import net.hearnsoft.tcm.utils.LocalMusicScanner;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.MusicPlayerController;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@UnstableApi
public class PlaybackViewModel extends AndroidViewModel {
    private MusicPlayerController playerController;
    private final MutableLiveData<MediaItem> currentMediaItem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    // 当前选中的仓库类型
    private final MutableLiveData<MediaRepository.RepositoryType> currentRepositoryType =
        new MutableLiveData<>(MediaRepository.RepositoryType.LOCAL);

    // 设备本地音乐播放列表
    private final MutableLiveData<List<MediaItem>> localPlaylist = new MutableLiveData<>();
    // 远端音乐播放列表
    private final MutableLiveData<List<MediaItem>> alistPlaylist = new MutableLiveData<>();

    // playlist 变为当前活跃仓库类型的引用
    private final MutableLiveData<List<MediaItem>> playlist = new MutableLiveData<>();
    private final MutableLiveData<Long> currentPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> duration = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSorting = new MutableLiveData<>(false);
    private final MutableLiveData<SongSortingRule> currentSortRule = new MutableLiveData<>(new SongSortingRule(SongSortingStrategy.Title, false));
    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
    // 当前PlayerController的播放列表，通常是由UI控件增删监控这个列表
    private final MutableLiveData<List<MediaItem>> currentPlaylist = new MutableLiveData<>();
    private final MutableLiveData<Integer> repeatMode = new MutableLiveData<>(0);
    private final MutableLiveData<String> playbackError = new MutableLiveData<>(null);

    // 仓库管理器
    private MediaRepositoryManager repositoryManager;

    // 标记控制器是否已连接
    private boolean isControllerActive = false;

    public PlaybackViewModel(@NonNull Application application) {
        super(application);
        playerController = MusicPlayerController.getInstance(application);
        connectToService();

        // 初始化仓库管理器
        initRepositories(application);

        // 将playlist初始值设置为本地播放列表
        playlist.setValue(localPlaylist.getValue());
    }

    private void initRepositories(Context context) {
        // 获取仓库管理器实例
        repositoryManager = MediaRepositoryManager.getInstance();

        // 初始化并添加本地音乐仓库
        LocalMusicRepository localRepository = new LocalMusicRepository(context);
        repositoryManager.addRepository(localRepository);
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
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playerController.getMediaController() != null) {
                    duration.postValue(playerController.getMediaController().getDuration());
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

            @Override
            public void onPlayerError(PlaybackException error) {
                Logs.e("PlaybackViewModel", "播放出错: " + getReadableErrorMessage(error));
                playbackError.postValue(getReadableErrorMessage(error));
            }
        });

        // 初始化状态
        if (playerController.getMediaController() != null) {
            MediaItem item = playerController.getMediaController().getCurrentMediaItem();
            if (item != null) {
                currentMediaItem.postValue(item);
            }
            isPlaying.postValue(playerController.getMediaController().isPlaying());
            duration.postValue(playerController.getMediaController().getDuration());

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

    public void scanAndLoadMusic() {
        isScanning.postValue(true);

        // 在新线程中执行扫描操作
        // 使用仓库加载媒体
        MediaRepository.RepositoryType currentType = currentRepositoryType.getValue();
        if (currentType == null) {
            currentType = MediaRepository.RepositoryType.LOCAL;
        }

        final MediaRepository.RepositoryType type = currentType;

        // 在新线程中执行加载操作
        CompletableFuture.runAsync(() -> {
            try {
                List<MediaItem> mediaItems = repositoryManager.loadMediaItemsByType(type).get();

                // 根据仓库类型更新对应的播放列表
                if (type == MediaRepository.RepositoryType.LOCAL) {
                    localPlaylist.postValue(mediaItems);
                } else if (type == MediaRepository.RepositoryType.ALIST) {
                    alistPlaylist.postValue(mediaItems);
                }

                // 同时更新通用播放列表引用
                playlist.postValue(mediaItems);
            } catch (Exception e) {
                Logs.e("PlaybackViewModel", "加载媒体失败: " + e.getMessage());
            } finally {
                isScanning.postValue(false);
            }
        });
    }

    /**
     * 初始化默认的 Alist 仓库并保存到 SharedPreferences
     */
    public void initDefaultAlistRepository(String name, String host, String username,
        String password, String rootPath, boolean isAnonymous, boolean autoActivate) {
        // 创建 Alist 仓库
        AlistMusicRepository alistRepo = new AlistMusicRepository(
            name, host, username, password, rootPath, isAnonymous);

        // 添加到仓库管理器
        repositoryManager.addRepository(alistRepo);

        // 保存配置到 SharedPreferences
        saveAlistConfig(name, host, username, password, rootPath, isAnonymous);

        // 只有在autoActivate为true时才激活该仓库
        if (autoActivate) {
            // 设置为当前仓库
            repositoryManager.setCurrentRepository(alistRepo);
            // 更新当前仓库类型状态
            currentRepositoryType.postValue(MediaRepository.RepositoryType.ALIST);

            // 加载媒体
            CompletableFuture.runAsync(() -> {
                isScanning.postValue(true);
                try {
                    List<MediaItem> mediaItems = alistRepo.loadMediaItems().get();
                    alistPlaylist.postValue(mediaItems);
                    playlist.postValue(mediaItems);
                    Logs.d("PlaybackViewModel", "加载到 " + mediaItems.size() + " 个 Alist 媒体文件");
                } catch (Exception e) {
                    Logs.e("PlaybackViewModel", "加载 Alist 媒体失败: " + e.getMessage());
                    playbackError.postValue("加载失败: " + e.getMessage());
                } finally {
                    isScanning.postValue(false);
                }
            });
        }
    }

    /**
     * 保存 Alist 仓库配置到 SharedPreferences
     */
    private void saveAlistConfig(String name, String host, String username,
        String password, String rootPath, boolean isAnonymous) {
        SettingsPrefUtils prefUtils = SettingsPrefUtils.getInstance(getApplication());

        // 首先获取已保存的 Alist 仓库数量
        int count = prefUtils.readIntSettings("alist_repo_count");

        // 检查是否已存在同名仓库
        boolean exists = false;
        int existingIndex = -1;

        for (int i = 0; i < count; i++) {
            String savedName = prefUtils.readStringSettings("alist_repo_" + i + "_name", "");
            if (savedName.equals(name)) {
                exists = true;
                existingIndex = i;
                break;
            }
        }

        // 确定索引
        int index = exists ? existingIndex : count;

        // 保存仓库基本信息
        prefUtils.writeStringSettings("alist_repo_" + index + "_name", name);
        prefUtils.writeStringSettings("alist_repo_" + index + "_host", host);
        prefUtils.writeStringSettings("alist_repo_" + index + "_username", username);
        prefUtils.writeStringSettings("alist_repo_" + index + "_password", password);
        prefUtils.writeStringSettings("alist_repo_" + index + "_rootPath", rootPath);
        prefUtils.writeBooleanSettings("alist_repo_" + index + "_anonymous", isAnonymous);

        // 如果是新添加的仓库，更新计数
        if (!exists) {
            prefUtils.writeIntSettings("alist_repo_count", count + 1);
        }

        Logs.d("PlaybackViewModel", "已" + (exists ? "更新" : "保存") + " Alist 仓库配置: " + name);
    }

    public void loadMediaByType(MediaRepository.RepositoryType type) {
        isScanning.postValue(true);

        // 更新当前仓库类型
        currentRepositoryType.postValue(type);

        CompletableFuture.runAsync(() -> {
            try {
                List<MediaItem> mediaItems = repositoryManager.loadMediaItemsByType(type).get();

                // 根据仓库类型更新对应的播放列表
                if (type == MediaRepository.RepositoryType.LOCAL) {
                    localPlaylist.postValue(mediaItems);
                } else if (type == MediaRepository.RepositoryType.ALIST) {
                    alistPlaylist.postValue(mediaItems);
                }

                // 更新通用播放列表引用
                playlist.postValue(mediaItems);

                Logs.d("PlaybackViewModel", "已加载媒体: " + mediaItems.size() + " 个项目");
            } catch (Exception e) {
                Logs.e("PlaybackViewModel", "加载媒体失败: " + e.getMessage());
                playbackError.postValue("加载媒体失败: " + e.getMessage());
            } finally {
                isScanning.postValue(false);
            }
        });
    }

    // 重载方法，用于加载特定的Alist仓库
    public void loadMediaByType(MediaRepository repository) {
        if (repository != null && repository.getRepositoryType() == MediaRepository.RepositoryType.ALIST) {
            // 设置为当前仓库
            repositoryManager.setCurrentRepository(repository);
            // 加载ALIST类型媒体
            loadMediaByType(MediaRepository.RepositoryType.ALIST);
        }
    }

    // 排序方法
    public void sortMusic(SongSortingRule rule) {
        // 记录当前排序规则
        currentSortRule.postValue(rule);

        // 获取当前活跃类型的播放列表
        List<MediaItem> currentList = playlist.getValue();

        // 如果没有音乐项目，直接返回
        if (playlist.getValue() == null || playlist.getValue().isEmpty()) {
            return;
        }

        isSorting.postValue(true);

        // 在后台线程执行排序
        new Thread(() -> {
            List<MediaItem> sortedList = LocalMusicScanner.sortMusicList(currentList, rule);

            // 更新排序后的列表
            if (sortedList != null) {
                // 更新对应类型的播放列表
                MediaRepository.RepositoryType type = currentRepositoryType.getValue();
                if (type == MediaRepository.RepositoryType.LOCAL) {
                    localPlaylist.postValue(sortedList);
                } else if (type == MediaRepository.RepositoryType.ALIST) {
                    alistPlaylist.postValue(sortedList);
                }

                // 更新通用播放列表
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
            currentPosition.postValue(playerController.getMediaController().getCurrentPosition());

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

        // 将打乱后的列表设置回播放器，随机一首开始播放
        Random random = new Random();
        int startIndex = random.nextInt(shuffledList.size());
        playMusic(shuffledList, startIndex);
        currentIndex.postValue(startIndex);
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

    // 设置当前仓库类型
    public void setCurrentRepositoryType(MediaRepository.RepositoryType type) {
        if (this.currentRepositoryType.getValue() != type) {
            this.currentRepositoryType.postValue(type);

            // 根据仓库类型切换活动的播放列表
            if (type == MediaRepository.RepositoryType.ALIST) {
                MediaRepositoryManager.getInstance().setCurrentRepository(
                    MediaRepositoryManager.getInstance().getCurrentAlistRepository()
                );
                // 将当前 playlist 指向 alistPlaylist 的数据
                playlist.setValue(alistPlaylist.getValue());

                // 只有当 Alist 播放列表为空时才加载
                if (alistPlaylist.getValue() == null || alistPlaylist.getValue().isEmpty()) {
                    scanAndLoadMusic();
                }
            } else if (type == MediaRepository.RepositoryType.LOCAL) {
                MediaRepositoryManager.getInstance().setCurrentRepository(
                    MediaRepositoryManager.getInstance().getLocalRepository()
                );
                // 将当前 playlist 指向 localPlaylist 的数据
                playlist.setValue(localPlaylist.getValue());

                // 只有当本地播放列表为空时才加载
                if (localPlaylist.getValue() == null || localPlaylist.getValue().isEmpty()) {
                    scanAndLoadMusic();
                }
            }
        }
    }
    public void playMusic(List<MediaItem> playlist, int startIndex) {
        ensureControllerConnected();
        if (playerController.getMediaController() != null) {
            playerController.playMusic(playlist, startIndex);
            this.playlist.postValue(playlist);
            this.currentIndex.postValue(startIndex);
            // 将当前设备媒体列表同步给当前播放列表
            this.currentPlaylist.postValue(new ArrayList<>(playlist));
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

    // 读取友好的错误信息
    private String getReadableErrorMessage(PlaybackException error) {
        if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
            return "找不到音频文件";
        } else if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
            return "网络连接失败";
        } else if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT) {
            return "网络连接超时";
        } else if (error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED) {
            return "不支持的音频格式";
        } else {
            return "播放失败 (代码: " + error.errorCode + ")";
        }
    }

    // 提供获取特定类型播放列表的方法（可选）
    public LiveData<List<MediaItem>> getLocalPlaylist() {
        return localPlaylist;
    }

    public LiveData<List<MediaItem>> getAlistPlaylist() {
        return alistPlaylist;
    }

    public LiveData<MediaRepository.RepositoryType> getCurrentRepositoryType() {
        return currentRepositoryType;
    }

    // 获取指定类型的所有仓库
    public List<MediaRepository> getRepositoriesByType(MediaRepository.RepositoryType type) {
        return repositoryManager.getRepositoriesByType(type);
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

    // 提供访问方法
    public LiveData<String> getPlaybackError() {
        return playbackError;
    }

    // 清除错误
    public void clearPlaybackError() {
        playbackError.postValue(null);
    }


    @Override
    protected void onCleared() {
        isControllerActive = false;
        playerController.release();
        super.onCleared();
    }

}
