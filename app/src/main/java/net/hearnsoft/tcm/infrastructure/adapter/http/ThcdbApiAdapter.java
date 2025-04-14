package net.hearnsoft.tcm.infrastructure.adapter.http;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import net.hearnsoft.thcdb_sdk.ApiClient;
import net.hearnsoft.thcdb_sdk.api.DefaultApi;
import net.hearnsoft.thcdb_sdk.api.UserApi;
import net.hearnsoft.thcdb_sdk.model.AuthCredential;
import net.hearnsoft.thcdb_sdk.model.BaseResponse;
import net.hearnsoft.thcdb_sdk.model.DataUserProfile;
import net.hearnsoft.thcdb_sdk.model.DataVecLanguage;
import net.hearnsoft.thcdb_sdk.model.DataVecString;
import net.hearnsoft.thcdb_sdk.model.UserProfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * ThcdbApiAdapter 提供了统一的接口访问THCDB API
 * 内部管理Cookie和会话，应用层无需处理身份验证细节
 */
public class ThcdbApiAdapter {
    // 默认的后台线程执行器
    private static final Executor executor = Executors.newCachedThreadPool();

    // API客户端实例，内部已集成Cookie管理
    @Getter
    private final ApiClient apiClient;

    // 各个API服务的实例
    @Getter
    private final UserApiAdapter user;
    @Getter
    private final DefaultApiAdapter defaultApi;

    /**
     * 创建THCDB API适配器
     * @param context 应用上下文，用于Cookie持久化
     * @param baseUrl API基础URL
     */
    public ThcdbApiAdapter(Context context, String baseUrl) {
        // 创建ApiClient并配置Cookie管理
        this.apiClient = new ApiClient(baseUrl, context);

        // 初始化各个API适配器
        this.user = new UserApiAdapter(apiClient, context);
        this.defaultApi = new DefaultApiAdapter(apiClient, context);
    }

    /**
     * 清除所有会话信息(登出)
     */
    public void clearSession() {
        apiClient.clearCookies();
    }

    /**
     * 检查用户是否已登录
     * @return 如果已登录返回true
     */
    public boolean isLoggedIn() {
        return apiClient.getSessionToken() != null;
    }

    /**
     * THCDB API异常类
     */
    public static class ThcdbApiException extends RuntimeException {
        private final Integer errorCode;

        public ThcdbApiException(String message) {
            super(message);
            this.errorCode = null;
        }

        public ThcdbApiException(Throwable cause) {
            super(cause.getMessage(), cause);
            this.errorCode = null;
        }

        public ThcdbApiException(String message, Integer errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public Integer getErrorCode() {
            return errorCode;
        }
    }
}