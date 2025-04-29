package net.hearnsoft.tcm.domain.repository;

public class MediaRepositoryState {
    private boolean isLoading;
    private boolean isError;
    private String errorMessage;
    private int itemCount;

    public MediaRepositoryState(boolean isLoading, boolean isError, String errorMessage, int itemCount) {
        this.isLoading = isLoading;
        this.isError = isError;
        this.errorMessage = errorMessage;
        this.itemCount = itemCount;
    }

    // 创建初始状态
    public static MediaRepositoryState initial() {
        return new MediaRepositoryState(false, false, null, 0);
    }

    // 创建加载中状态
    public static MediaRepositoryState loading() {
        return new MediaRepositoryState(true, false, null, 0);
    }

    // 创建错误状态
    public static MediaRepositoryState error(String message) {
        return new MediaRepositoryState(false, true, message, 0);
    }

    // 创建加载完成状态
    public static MediaRepositoryState loaded(int count) {
        return new MediaRepositoryState(false, false, null, count);
    }

    // Getters
    public boolean isLoading() {
        return isLoading;
    }

    public boolean isError() {
        return isError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getItemCount() {
        return itemCount;
    }
}
