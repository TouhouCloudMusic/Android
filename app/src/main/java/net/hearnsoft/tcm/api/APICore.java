package net.hearnsoft.tcm.api;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.hearnsoft.tcm.beans.DefaultResponse;
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.utils.ErrorCode;
import net.hearnsoft.tcm.utils.Logs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;
import okio.BufferedSink;
import okio.Okio;

public class APICore {
    private final String TAG = this.getClass().getSimpleName();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String apiUrl;
    private final OkHttpClient client;
    private final Handler mainHandler;
    private final Gson gson;
    private String sessionToken;


    public APICore() {
        this.apiUrl = Constants.API_HOST;
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setLenient()
                .serializeNulls()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
    }

    public APICore(String baseHostUrl) {
        this.apiUrl = baseHostUrl;
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setLenient()
                .serializeNulls()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void uploadImage(String endpoint, Uri imageUri, ContentResolver resolver, APICallback<String> callback) {
        String url = apiUrl + "/" + endpoint;
        if (imageUri == null) {
            callback.onError(new ApiError(
                    "Image URI is null",
                    "Image URI is null",
                    ErrorCode.FileURINull.getCode()));
            return;
        }
        if (TextUtils.isEmpty(sessionToken)) {
            callback.onError(new ApiError(
                    "Unauthorized",
                    "No session token available",
                    ErrorCode.InvalidToken.getCode())
            );
            return;
        }

        /*RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", "image.jpg",
                        RequestBody.create(image, MediaType.parse("image/*")))
                .build();*/

        RequestBody requestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                String mimeType = resolver.getType(imageUri);
                if (mimeType == null) {
                    mimeType = "image/*";
                }
                return MediaType.parse(mimeType);
            }

            @Override
            public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {
                try (InputStream inputStream = resolver.openInputStream(imageUri)) {
                    if (inputStream != null) {
                        bufferedSink.writeAll(Okio.source(inputStream));
                    }
                } catch (Exception e) {
                    callback.onError(new ApiError(
                            "File read error",
                            e.getMessage(),
                            ErrorCode.FileReadError.getCode())
                    );
                }
            }
        };

        String fileName = getFileNameFromUri(imageUri, resolver);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("data", fileName, requestBody);

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(imagePart)
                .build();

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (sessionToken != null) {
            requestBuilder.addHeader("Cookie", "session_token=" + sessionToken);
        }
        requestBuilder.post(body).build();

        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(new ApiError(
                        "Network error",
                        e.getMessage(),
                        ErrorCode.NetworkError.getCode())
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Logs.d(TAG, "Raw response: " + response);
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    callback.onSuccess(responseBody);
                } else {
                    // 处理错误响应
                    String responseBody = response.body().string();
                    try {
                        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                        DefaultResponse errorResponse = gson.fromJson(jsonResponse, DefaultResponse.class);
                        callback.onError(new ApiError(
                            errorResponse.getStatus(),
                            errorResponse.getMessage(),
                            errorResponse.getError_code()
                        ));
                    } catch (JsonSyntaxException e) {
                        callback.onError(new ApiError(
                                "JSON parsing error",
                                "Received malformed JSON: " + responseBody,
                                ErrorCode.JSONParseError.getCode())
                        );
                    }
                }
            }
        });
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri, ContentResolver contentResolver) {
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

    public <T> void callAPI(String endpoint, ApiMethod method, Object requestData, Class<T> responseType, APICallback<T> callback) {
        callAPI(endpoint, method, requestData, responseType, callback, true);
    }

    public <T> void callAPI(String endpoint, ApiMethod method, Object requestData, Class<T> responseType, APICallback<T> callback, boolean includeCookie) {
        String url = apiUrl + "/" + endpoint;
        Request.Builder requestBuilder = new Request.Builder().url(url);
        Logs.d(TAG, "request api url:"+ url);

        if (includeCookie && sessionToken != null) {
            requestBuilder.addHeader("Cookie", "session_token=" + sessionToken);
        }

        switch (method) {
            case POST:
                if (requestData != null) {
                    String jsonBody = gson.toJson(requestData);
                    RequestBody body = RequestBody.create(jsonBody, JSON);
                    requestBuilder.post(body);
                }
                break;
            case PUT:
                if (requestData != null) {
                    String jsonBody = gson.toJson(requestData);
                    RequestBody body = RequestBody.create(jsonBody, JSON);
                    requestBuilder.put(body);
                }
                break;
            case DELETE:
                requestBuilder.delete();
                break;
            case GET:
            default:
                requestBuilder.get();
                break;
        }

        Logs.d(TAG, requestBuilder.toString());

        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleError(new ApiError(
                        "Network error",
                        e.getMessage(),
                        ErrorCode.NetworkError.getCode()), callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                String newSessionToken = getSessionToken(response);
                Logs.d(TAG, "Raw response: " + responseBody + ", code:" + response.code());

                try {
                    JsonElement jsonElement = JsonParser.parseString(responseBody);
                    DefaultResponse defaultResponse = gson.fromJson(jsonElement, DefaultResponse.class);

                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonResponse = jsonElement.getAsJsonObject();
                        String state = jsonResponse.has("status") ? jsonResponse.get("status").getAsString() : "error";

                        // 处理成功响应
                        if ("Ok".equals(defaultResponse.getStatus())) {
                            if (jsonResponse.has("data")) {
                                // 处理status + data结构
                                T result;
                                if (responseType == String.class) {
                                    result = (T) jsonResponse.get("data").getAsString();
                                } else {
                                    JsonElement data = jsonResponse.get("data");
                                    result = gson.fromJson(data, responseType);
                                }
                                handleSuccess(result, newSessionToken, callback);
                            } else {
                                // 处理status + message结构
                                handleSuccess((T) defaultResponse.getMessage(), newSessionToken, callback);
                            }
                        } else {
                            ApiError error = new ApiError(
                                defaultResponse.getStatus(),
                                defaultResponse.getMessage(),
                                defaultResponse.getError_code()
                            );
                            handleError(error, callback);
                        }
                    } else {
                        // 处理非 JSON 对象的响应
                        handleError(new ApiError(
                                "Non-JSON response",
                                responseBody,
                                ErrorCode.NonJSONResponse.getCode()), callback);
                    }
                } catch (JsonSyntaxException e) {
                    // 处理 JSON 解析错误
                    handleError(new ApiError(
                            "JSON parsing error",
                            "Received malformed JSON: " + responseBody,
                            ErrorCode.JSONParseError.getCode()), callback);
                } catch (Exception e) {
                    handleError(new ApiError("Unexpected error", e.getMessage() + ". Response: " + responseBody,
                            ErrorCode.UnexpectedError.getCode()), callback);
                }
            }
        });
    }

    private String getSessionToken(Response response) {
        String sessionToken = null;
        List<String> cookies = response.headers("Set-Cookie");
        for (String cookie : cookies) {
            if (cookie.startsWith("session_token=")) {
                sessionToken = cookie.split(";")[0].substring("session_token=".length());
                break;
            }
        }
        return sessionToken;
    }

    private <T> void handleSuccess(final T data, final String sessionToken, final APICallback<T> callback) {
        mainHandler.post(() -> {
            if (callback instanceof SessionTokenCallback) {
                ((SessionTokenCallback<T>) callback).onSuccess(data, sessionToken);
            } else {
                callback.onSuccess(data);
            }
        });
    }

    private <T> void handleError(final ApiError error, final APICallback<T> callback) {
        mainHandler.post(() -> callback.onError(error));
    }

    public interface APICallback<T> {
        void onSuccess(T data);
        void onError(ApiError error);
    }

    public interface SessionTokenCallback<T> extends APICallback<T> {
        void onSuccess(T data, String sessionToken);
    }

    @Getter
    @Setter
    public static class ApiError {
        private String state;
        private String message;
        private int error_code;

        public ApiError(String state, String message, int errorCode) {
            this.state = state;
            this.message = message;
            this.error_code = errorCode;
        }
    }

}
