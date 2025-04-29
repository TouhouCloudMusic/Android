package net.hearnsoft.tcm.domain.repository;

import androidx.media3.common.MediaItem;

import net.hearnsoft.tcm.utils.Logs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MediaRepositoryManager {
    private static volatile MediaRepositoryManager instance;
    private final Map<String, MediaRepository> repositories = new HashMap<>();

    // 保持对本地仓库的单一引用
    private LocalMusicRepository localRepository;

    // 当前选中的 Alist 仓库
    private AlistMusicRepository currentAlistRepository;

    // 添加一个私有构造函数
    private MediaRepositoryManager() {}

    // 单例模式获取实例
    public static MediaRepositoryManager getInstance() {
        if (instance == null) {
            synchronized (MediaRepositoryManager.class) {
                if (instance == null) {
                    instance = new MediaRepositoryManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加仓库
     * @param repository 要添加的仓库
     */
    public void addRepository(MediaRepository repository) {
        String key = generateRepositoryKey(repository);
        repositories.put(key, repository);

        // 如果是本地仓库，保存引用
        if (repository.getRepositoryType() == MediaRepository.RepositoryType.LOCAL) {
            localRepository = (LocalMusicRepository) repository;
        }

        // 如果是第一个 Alist 仓库，设为当前 Alist 仓库
        if (repository.getRepositoryType() == MediaRepository.RepositoryType.ALIST
            && currentAlistRepository == null) {
            currentAlistRepository = (AlistMusicRepository) repository;
            Logs.d("MediaRepositoryManager", "设置当前 Alist 仓库: " + repository.getRepositoryName());
        }
    }

    /**
     * 设置当前仓库
     * @param repository 要设置的仓库
     */
    public void setCurrentRepository(MediaRepository repository) {
        if (repository == null) return;

        // 只处理 Alist 仓库类型，本地仓库不需要切换
        if (repository.getRepositoryType() == MediaRepository.RepositoryType.ALIST) {
            currentAlistRepository = (AlistMusicRepository) repository;
            Logs.d("MediaRepositoryManager", "切换到 Alist 仓库: " + repository.getRepositoryName());
        }
    }

    /**
     * 获取当前本地仓库
     */
    public LocalMusicRepository getLocalRepository() {
        return localRepository;
    }

    /**
     * 获取当前 Alist 仓库
     */
    public AlistMusicRepository getCurrentAlistRepository() {
        return currentAlistRepository;
    }

    /**
     * 根据类型获取当前仓库
     */
    public MediaRepository getCurrentRepositoryByType(MediaRepository.RepositoryType type) {
        switch (type) {
            case LOCAL:
                return localRepository;
            case ALIST:
                return currentAlistRepository;
            default:
                return null;
        }
    }

    /**
     * 获取所有已注册的仓库
     */
    public List<MediaRepository> getAllRepositories() {
        return new ArrayList<>(repositories.values());
    }

    /**
     * 获取指定类型的仓库列表
     */
    public List<MediaRepository> getRepositoriesByType(MediaRepository.RepositoryType type) {
        return repositories.values().stream()
            .filter(repo -> repo.getRepositoryType() == type)
            .collect(Collectors.toList());
    }

    /**
     * 加载指定类型的所有媒体项
     * @param type 要加载的仓库类型
     */
    public CompletableFuture<List<MediaItem>> loadMediaItemsByType(MediaRepository.RepositoryType type) {
        switch (type) {
            case LOCAL:
                if (localRepository != null) {
                    return localRepository.loadMediaItems();
                }
                break;
            case ALIST:
                if (currentAlistRepository != null) {
                    return currentAlistRepository.loadMediaItems();
                }
                break;
            case ALL_ALIST:
                return loadAllAlistMediaItems();
            case ALL:
                return loadAllMediaItems();
        }
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    private CompletableFuture<List<MediaItem>> loadAllAlistMediaItems() {
        List<MediaRepository> alistRepos = getRepositoriesByType(MediaRepository.RepositoryType.ALIST);
        List<CompletableFuture<List<MediaItem>>> futures = new ArrayList<>();

        for (MediaRepository repo : alistRepos) {
            futures.add(repo.loadMediaItems());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<MediaItem> allItems = new ArrayList<>();
                for (CompletableFuture<List<MediaItem>> future : futures) {
                    try {
                        allItems.addAll(future.get());
                    } catch (Exception e) {
                        Logs.e("MediaRepositoryManager", "加载Alist媒体项失败: " + e.getMessage());
                    }
                }
                return allItems;
            });
    }

    /**
     * 加载所有仓库的媒体项
     */
    public CompletableFuture<List<MediaItem>> loadAllMediaItems() {
        List<CompletableFuture<List<MediaItem>>> futures = new ArrayList<>();

        for (MediaRepository repository : repositories.values()) {
            futures.add(repository.loadMediaItems());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<MediaItem> allItems = new ArrayList<>();
                for (CompletableFuture<List<MediaItem>> future : futures) {
                    try {
                        allItems.addAll(future.get());
                    } catch (Exception e) {
                        Logs.e("MediaRepositoryManager", "加载媒体项时出错: " + e.getMessage());
                    }
                }
                return allItems;
            });
    }

    /**
     * 生成仓库的唯一键名
     */
    private String generateRepositoryKey(MediaRepository repository) {
        return repository.getRepositoryType().name() + "_" + repository.getRepositoryName();
    }
}