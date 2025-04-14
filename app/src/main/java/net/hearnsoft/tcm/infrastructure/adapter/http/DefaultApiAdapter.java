package net.hearnsoft.tcm.infrastructure.adapter.http;

import android.content.Context;

import net.hearnsoft.thcdb_sdk.ApiClient;
import net.hearnsoft.thcdb_sdk.api.DefaultApi;
import net.hearnsoft.thcdb_sdk.model.DataVecLanguage;
import net.hearnsoft.thcdb_sdk.model.DataVecString;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class DefaultApiAdapter extends BaseApiAdapter {
    // 默认的后台线程执行器
    private static final Executor executor = Executors.newCachedThreadPool();
    private final DefaultApi api;

    protected DefaultApiAdapter(ApiClient apiClient, Context context) {
        super(apiClient, context);
        this.api = apiClient.createService(DefaultApi.class);
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
                    ThcdbApiAdapter.ThcdbApiException exception =
                        new ThcdbApiAdapter.ThcdbApiException("获取语言列表失败: " +  response.code() + " " + response.message());
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
                    ThcdbApiAdapter.ThcdbApiException exception =
                        new ThcdbApiAdapter.ThcdbApiException("获取用户角色失败: " + response.code() + " " + response.message());
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
}
