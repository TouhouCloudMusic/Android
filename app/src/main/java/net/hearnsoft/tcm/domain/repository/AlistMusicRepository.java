package net.hearnsoft.tcm.domain.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

import net.hearnsoft.alist_sdk.Alist;
import net.hearnsoft.alist_sdk.model.AlistConfig;
import net.hearnsoft.tcm.utils.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Alist远程仓库实现
 * 负责加载和管理Alist网盘中的媒体文件
 */
public class AlistMusicRepository implements MediaRepository {
    private static final String TAG = "AlistRepository";
    private final Alist alistClient;
    private final MutableLiveData<List<MediaItem>> mediaItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<MediaRepositoryState> repositoryStateLiveData = new MutableLiveData<>(MediaRepositoryState.initial());
    private final String rootPath;
    private String repositoryName;
    private String password = "";

    public AlistMusicRepository(String name, String host, String username, String password, String rootPath, boolean isAnonymous) {
        this.repositoryName = name;
        this.rootPath = rootPath;
        this.password = password;

        // 初始化Alist客户端
        AlistConfig config = new AlistConfig(host, username, password, isAnonymous);
        alistClient = new Alist(config);
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public RepositoryType getRepositoryType() {
        return RepositoryType.ALIST;
    }

    @Override
    public CompletableFuture<List<MediaItem>> loadMediaItems() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 更新为加载状态
                repositoryStateLiveData.postValue(MediaRepositoryState.loading());

                // 获取文件列表
                List<Map<String, String>> fileInfos = alistClient.getAllFilesInfo(rootPath, password);

                // 过滤并转换为MediaItems
                List<MediaItem> mediaItems = fileInfos.stream()
                    .filter(info -> isMediaFile(info.get("filePath")))
                    .map(this::createMediaItemFromAlist)
                    .collect(Collectors.toList());

                Logs.d(TAG, "加载到 " + mediaItems.size() + " 个媒体文件");

                // 更新LiveData和状态
                mediaItemsLiveData.postValue(mediaItems);
                repositoryStateLiveData.postValue(MediaRepositoryState.loaded(mediaItems.size()));

                return mediaItems;
            } catch (Exception e) {
                Logs.e(TAG, "加载Alist媒体失败: " + e.getMessage());
                repositoryStateLiveData.postValue(MediaRepositoryState.error(e.getMessage()));
                return new ArrayList<>();
            }
        });
    }

    @Override
    public LiveData<List<MediaItem>> observeMediaItems() {
        return mediaItemsLiveData;
    }

    /**
     * 获取仓库状态的LiveData
     */
    public LiveData<MediaRepositoryState> observeRepositoryState() {
        return repositoryStateLiveData;
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
            List<MediaItem> results = new ArrayList<>();

            if (mediaItems != null && !query.isEmpty()) {
                String lowerQuery = query.toLowerCase();
                for (MediaItem item : mediaItems) {
                    String title = item.mediaMetadata.title != null ?
                        item.mediaMetadata.title.toString().toLowerCase() : "";
                    if (title.contains(lowerQuery)) {
                        results.add(item);
                    }
                }
            }
            return results;
        });
    }

    @Override
    public CompletableFuture<Void> refreshMediaItems() {
        return loadMediaItems().thenAccept(items -> {
            // 已经在loadMediaItems中更新了LiveData，这里不需要额外操作
        });
    }

    /**
     * 从Alist文件信息创建MediaItem
     */
    private MediaItem createMediaItemFromAlist(Map<String, String> fileInfo) {
        String filePath = fileInfo.get("filePath");
        String rawUrl = fileInfo.get("rawUrl");

        if (rawUrl == null || rawUrl.isEmpty()) {
            Logs.e(TAG, "创建MediaItem失败：URL为空 - " + filePath);
            return null;
        }

        // 提取文件名作为标题
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        // 移除扩展名
        String title = fileName;
        if (fileName.contains(".")) {
            title = fileName.substring(0, fileName.lastIndexOf("."));
        }
        // 获取扩展名用于确定媒体类型
        String extension = "";
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }

        // 确定MIME类型
        String mimeType = getMimeType(extension);

        // 生成唯一ID
        String id = "alist_" + UUID.randomUUID().toString();

        Logs.d(TAG, "创建媒体项: " + title + ", URL: " + rawUrl);

        // 构建MediaItem
        return new MediaItem.Builder()
            .setUri(rawUrl)
            .setMediaId(id)
            .setMimeType(mimeType)  // 设置MIME类型
            .setMediaMetadata(new MediaMetadata.Builder()
                .setTitle(title)
                .setDisplayTitle(title)
                .setArtist(repositoryName)
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .build())
            .build();
    }

    // 根据扩展名确定MIME类型
    private String getMimeType(String extension) {
        switch (extension.toLowerCase()) {
            case "mp3":
                return "audio/mpeg";
            case "flac":
                return "audio/flac";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "m4a":
            case "aac":
                return "audio/aac";
            default:
                return "audio/*";
        }
    }

    /**
     * 判断文件是否是媒体文件
     */
    private boolean isMediaFile(String filePath) {
        if (filePath == null) return false;

        String lowerPath = filePath.toLowerCase();
        // 音频文件
        return lowerPath.endsWith(".mp3") ||
            lowerPath.endsWith(".wav") ||
            lowerPath.endsWith(".flac") ||
            lowerPath.endsWith(".aac") ||
            lowerPath.endsWith(".ogg");
    }
}
