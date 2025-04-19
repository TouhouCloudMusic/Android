package net.hearnsoft.tcm.infrastructure.adapter.http;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import net.hearnsoft.thcdb_sdk.ApiClient;
import net.hearnsoft.thcdb_sdk.api.UserApi;
import net.hearnsoft.thcdb_sdk.model.AuthCredential;
import net.hearnsoft.thcdb_sdk.model.BaseResponse;
import net.hearnsoft.thcdb_sdk.model.DataUserProfile;
import net.hearnsoft.thcdb_sdk.model.UserProfile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class UserApiAdapter extends BaseApiAdapter {
    // 默认的后台线程执行器
    private static final Executor executor = Executors.newCachedThreadPool();
    private final UserApi api;
    private final ApiClient client;

    protected UserApiAdapter(ApiClient apiClient, Context context) {
        super(apiClient, context);
        this.client = apiClient;
        this.api = apiClient.createService(UserApi.class);
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
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
                throw new ThcdbApiAdapter.ThcdbApiException("Image URI is null");
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
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
            }
        }, executor);
    }

    /**
     * 上传用户背景横幅图
     * @param bannerUri 头像文件的URI
     * @param contentResolver 用于访问URI内容的ContentResolver
     * @return 包含操作结果的CompletableFuture
     */
    public CompletableFuture<Boolean> uploadProfileBanner(Uri bannerUri, ContentResolver contentResolver) {
        return CompletableFuture.supplyAsync(() -> {
            if (bannerUri == null) {
                throw new ThcdbApiAdapter.ThcdbApiException("Image URI is null");
            }

            try {
                // 获取文件名
                String fileName = getFileNameFromUri(bannerUri, contentResolver);

                // 创建请求体，直接从URI读取数据
                RequestBody requestBody = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        String mimeType = contentResolver.getType(bannerUri);
                        if (mimeType == null) {
                            mimeType = "image/*";
                        }
                        return MediaType.parse(mimeType);
                    }

                    @Override
                    public void writeTo(okio.BufferedSink bufferedSink) throws IOException {
                        try (InputStream inputStream = contentResolver.openInputStream(bannerUri)) {
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
                Response<BaseResponse<Void>> response = api.uploadProfileBanner(body).execute();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    return true;
                } else {
                    ThcdbApiAdapter.ThcdbApiException exception = createApiException(response);
                    handleApiError(exception);
                    throw exception;
                }
            } catch (IOException e) {
                ThcdbApiAdapter.ThcdbApiException exception = new ThcdbApiAdapter.ThcdbApiException(e);
                handleApiError(exception);
                throw exception;
            }
        }, executor);
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
}
