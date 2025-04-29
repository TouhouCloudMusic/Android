package net.hearnsoft.tcm.domain.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;

import net.hearnsoft.tcm.utils.LocalMusicScanner;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 本地音乐仓库实现
 * 负责加载和管理本地存储中的音乐文件
 */
public class LocalMusicRepository implements MediaRepository {
    private final Context context;
    private final MutableLiveData<List<MediaItem>> mediaItemsLiveData = new MutableLiveData<>();

    public LocalMusicRepository(Context context) {
        this.context = context;
    }

    @Override
    public String getRepositoryName() {
        return "LocalMusicRepository";
    }

    @Override
    public RepositoryType getRepositoryType() {
        return RepositoryType.LOCAL;
    }

    @Override
    public CompletableFuture<List<MediaItem>> loadMediaItems() {
        return CompletableFuture.supplyAsync(() -> {
            List<MediaItem> mediaItems = LocalMusicScanner.scanDeviceMusic(context);
            mediaItemsLiveData.postValue(mediaItems);
            return mediaItems;
        });
    }

    @Override
    public LiveData<List<MediaItem>> observeMediaItems() {
        return mediaItemsLiveData;
    }

    @Override
    public CompletableFuture<MediaItem> getMediaItemById(String mediaId) {
        return CompletableFuture.supplyAsync(() -> {
            List<MediaItem> mediaItems = mediaItemsLiveData.getValue();
            if (mediaItems != null) {
                for (MediaItem item : mediaItems) {
                    if (item.mediaId.equals(mediaId)) {
                        return item;
                    }
                }
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<MediaItem>> searchMediaItems(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<MediaItem> mediaItems = mediaItemsLiveData.getValue();
            if (mediaItems != null) {
                return LocalMusicScanner.searchMusicList(mediaItems, query);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> refreshMediaItems() {
        return CompletableFuture.runAsync(() -> {
            List<MediaItem> mediaItems = LocalMusicScanner.scanDeviceMusic(context);
            mediaItemsLiveData.postValue(mediaItems);
        });
    }
}