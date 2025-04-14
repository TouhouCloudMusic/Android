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
        this.user = new UserApiAdapter(apiClient);
        this.defaultApi = new DefaultApiAdapter(apiClient);
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
     * 用户API适配器
     */
    public static class UserApiAdapter {
        private final UserApi api;
        private final ApiClient client;

        UserApiAdapter(ApiClient client) {
            this.client = client;
            this.api = client.createService(UserApi.class);
        }

        /**
         * 获取当前用户信息
         * @return 包含用户信息的CompletableFuture
         */
        public CompletableFuture<UserProfile> profile() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Response<DataUserProfile> response = api.profile().execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        return response.body().getData();
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 获取指定用户名的用户信息
         * @param username 用户名
         * @return 包含用户信息的CompletableFuture
         */
        public CompletableFuture<UserProfile> profileWithName(String username) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Response<DataUserProfile> response = api.profileWithName(username).execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        return response.body().getData();
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 用户登录
         * @param username 用户名
         * @param password 密码
         * @return 包含用户信息的CompletableFuture
         */
        public CompletableFuture<UserProfile> signIn(String username, String password) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    AuthCredential credential = new AuthCredential(username, password);
                    Response<DataUserProfile> response = api.signIn(credential).execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        return response.body().getData();
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 用户注册
         * @param username 用户名
         * @param password 密码
         * @return 包含用户信息的CompletableFuture
         */
        public CompletableFuture<UserProfile> signUp(String username, String password) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    AuthCredential credential = new AuthCredential(username, password);
                    Response<DataUserProfile> response = api.signUp(credential).execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        return response.body().getData();
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 用户登录
         * @param auth {@code AuthCredential} 认证信息
         * @return 包含用户信息的CompletableFuture
         */
        public CompletableFuture<UserProfile> signIn(AuthCredential auth) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Response<DataUserProfile> response = api.signIn(auth).execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        return response.body().getData();
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 用户注册
         * @param auth {@code AuthCredential} 认证信息
         * @return 包含用户信息的CompletableFuture
         */
        public CompletableFuture<UserProfile> signUp(AuthCredential auth) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Response<DataUserProfile> response = api.signUp(auth).execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        return response.body().getData();
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 用户登出
         * @return 包含操作结果的CompletableFuture
         */
        public CompletableFuture<Boolean> signOut() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Response<BaseResponse<Void>> response = api.signOut().execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // 清除客户端的会话信息
                        client.clearCookies();
                        return true;
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 上传用户头像(使用Uri)
         * @param avatarUri 头像文件的URI
         * @param contentResolver 用于访问URI内容的ContentResolver
         * @return 包含操作结果的CompletableFuture
         */
        public CompletableFuture<Boolean> uploadAvatar(Uri avatarUri, ContentResolver contentResolver) {
            return CompletableFuture.supplyAsync(() -> {
                if (avatarUri == null) {
                    throw new ThcdbApiException("Image URI is null");
                }

                try {
                    // 获取文件名
                    String fileName = getFileNameFromUri(avatarUri, contentResolver);

                    // 创建请求体，直接从URI读取数据
                    RequestBody requestBody = new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            String mimeType = contentResolver.getType(avatarUri);
                            if (mimeType == null) {
                                mimeType = "image/*";
                            }
                            return MediaType.parse(mimeType);
                        }

                        @Override
                        public void writeTo(okio.BufferedSink bufferedSink) throws IOException {
                            try (InputStream inputStream = contentResolver.openInputStream(avatarUri)) {
                                if (inputStream != null) {
                                    byte[] buffer = new byte[4096];
                                    int read;
                                    while ((read = inputStream.read(buffer)) != -1) {
                                        bufferedSink.write(buffer, 0, read);
                                    }
                                }
                            }
                        }
                    };

                    // 创建MultipartBody.Part用于上传
                    MultipartBody.Part body = MultipartBody.Part.createFormData("data", fileName, requestBody);

                    // 执行上传
                    Response<BaseResponse<Void>> response = api.uploadAvatar(body).execute();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        return true;
                    } else {
                        throw createApiException(response);
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 根据响应创建适当的异常
         */
        private ThcdbApiException createApiException(Response<?> response) {
            if (!response.isSuccessful()) {
                return new ThcdbApiException("HTTP错误: " + response.code() + ",错误消息：" + response.message());
            }

            // 尝试从响应体中获取错误信息
            try {
                if (response.body() instanceof BaseResponse) {
                    BaseResponse<?> baseResponse = (BaseResponse<?>) response.body();
                    if (baseResponse != null && !baseResponse.isSuccess()) {
                        return new ThcdbApiException(
                            baseResponse.getMessage(),
                            baseResponse.getErrorCode()
                        );
                    }
                }
            } catch (Exception ignored) {
                // 解析失败时使用默认错误
            }

            return new ThcdbApiException("未知API错误");
        }
    }

    /**
     * 默认API适配器
     */
    public static class DefaultApiAdapter {
        private final DefaultApi api;

        DefaultApiAdapter(ApiClient client) {
            this.api = client.createService(DefaultApi.class);
        }

        /**
         * 获取可用语言列表
         * @return 包含语言列表的CompletableFuture
         */
        public CompletableFuture<DataVecLanguage> languageList() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Response<DataVecLanguage> response = api.languageList().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body();
                    } else {
                        throw new ThcdbApiException("获取语言列表失败: " + response.code());
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }

        /**
         * 获取用户角色列表
         * @return 包含角色列表的CompletableFuture
         */
        public CompletableFuture<DataVecString> userRoles() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Response<DataVecString> response = api.userRoles().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body();
                    } else {
                        throw new ThcdbApiException("获取用户角色失败: " + response.code());
                    }
                } catch (IOException e) {
                    throw new ThcdbApiException(e);
                }
            }, executor);
        }
    }

    /**
     * 从URI获取文件名
     * @param uri 文件URI
     * @param contentResolver ContentResolver实例
     * @return 文件名
     */
    @SuppressLint("Range")
    private static String getFileNameFromUri(Uri uri, ContentResolver contentResolver) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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