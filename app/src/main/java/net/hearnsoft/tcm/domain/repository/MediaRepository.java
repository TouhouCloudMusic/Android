package net.hearnsoft.tcm.domain.repository;

import androidx.lifecycle.LiveData;
import androidx.media3.common.MediaItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 媒体仓库接口
 * 统一本地媒体库和远端Alist媒体库的访问方式
 */
public interface MediaRepository {
    /**
     * 获取仓库名称
     */
    String getRepositoryName();

    /**
     * 获取仓库类型
     */
    RepositoryType getRepositoryType();

    /**
     * 异步加载仓库中的所有媒体项
     * @return 包含媒体项列表的CompletableFuture
     */
    CompletableFuture<List<MediaItem>> loadMediaItems();

    /**
     * 观察仓库中的媒体项变化
     * @return 媒体项列表的LiveData
     */
    LiveData<List<MediaItem>> observeMediaItems();

    /**
     * 根据ID获取单个媒体项
     * @param mediaId 媒体项ID
     * @return 媒体项的CompletableFuture，如果未找到则返回null
     */
    CompletableFuture<MediaItem> getMediaItemById(String mediaId);

    /**
     * 搜索媒体项
     * @param query 搜索关键词
     * @return 搜索结果列表的CompletableFuture
     */
    CompletableFuture<List<MediaItem>> searchMediaItems(String query);

    /**
     * 刷新仓库中的媒体项
     * @return 刷新操作的CompletableFuture
     */
    CompletableFuture<Void> refreshMediaItems();

    /**
     * 仓库类型枚举
     */
    enum RepositoryType {
        LOCAL,   // 本地存储
        ALIST,   // Alist远程网盘
        ALL_ALIST, // 所有Alist仓库
        ALL      // 全部类型
    }
}