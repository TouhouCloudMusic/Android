package net.hearnsoft.tcm.infrastructure.adapter.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import net.hearnsoft.tcm.R;
import net.hearnsoft.thcdb_sdk.ApiClient;
import net.hearnsoft.thcdb_sdk.model.BaseResponse;

import retrofit2.Response;

/**
 * 所有API适配器的基类，提供通用错误处理和功能
 */
public abstract class BaseApiAdapter {
    protected final ApiClient apiClient;
    protected final Context context;

    protected BaseApiAdapter(ApiClient apiClient, Context context) {
        this.apiClient = apiClient;
        this.context = context;
    }

    /**
     * 处理API错误并显示Toast
     * @param error 错误信息
     */
    protected void handleApiError(ThcdbApiAdapter.ThcdbApiException error) {
        if (error != null) {
            showToastOnMainThread(error.getMessage());
        }
    }

    /**
     * 在主线程显示Toast
     * @param message 消息内容
     */
    protected void showToastOnMainThread(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * 根据响应创建适当的异常
     */
    protected ThcdbApiAdapter.ThcdbApiException createApiException(Response<?> response) {
        if (!response.isSuccessful()) {
            // 格式化HTTP错误信息：HTTP错误+错误码+错误消息
            String formattedErrorMsg = String.format("HTTP错误(%d): %s",
                response.code(),
                response.message());
            return new ThcdbApiAdapter.ThcdbApiException("req url: " + response.raw().request().url()+ ","+formattedErrorMsg);
        }

        // 尝试从响应体中获取错误信息
        try {
            if (response.body() instanceof BaseResponse) {
                BaseResponse<?> baseResponse = (BaseResponse<?>) response.body();
                if (baseResponse != null && !baseResponse.isSuccess()) {
                    // 如果是API业务错误，添加错误码
                    Integer errorCode = baseResponse.getErrorCode();
                    String errorMsg = baseResponse.getMessage();
                    if (errorCode != null) {
                        errorMsg = String.format("API错误(%d): %s", errorCode, errorMsg);
                    }
                    return new ThcdbApiAdapter.ThcdbApiException(
                        errorMsg,
                        errorCode
                    );
                }
            }
        } catch (Exception ignored) {
            // 解析失败时使用默认错误
        }

        return new ThcdbApiAdapter.ThcdbApiException("未知API错误");
    }
}