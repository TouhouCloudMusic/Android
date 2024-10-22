package net.hearnsoft.tcm.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.hearnsoft.tcm.utils.Constants;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    public <T> void callAPI(String endpoint, ApiMethod method, Object requestData, Class<T> responseType, APICallback<T> callback) {
        callAPI(endpoint, method, requestData, responseType, callback, true);
    }

    public <T> void callAPI(String endpoint, ApiMethod method, Object requestData, Class<T> responseType, APICallback<T> callback, boolean includeCookie) {
        String url = apiUrl + "/" + endpoint;
        Request.Builder requestBuilder = new Request.Builder().url(url);
        Log.d(TAG, "request api url:"+ url);

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

        Log.d(TAG, requestBuilder.toString());

        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleError(new ApiError("Network error", e.getMessage()), callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                String newSessionToken = getSessionToken(response);
                Log.d(TAG, "Raw response: " + responseBody);

                try {
                    JsonElement jsonElement = JsonParser.parseString(responseBody);
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonResponse = jsonElement.getAsJsonObject();
                        String state = jsonResponse.has("state") ? jsonResponse.get("state").getAsString() : "error";

                        if ("success".equals(state)) {
                            T result;
                            if (responseType == String.class) {
                                result = (T) (jsonResponse.has("data") ? jsonResponse.get("data").getAsString() : responseBody);
                            } else {
                                JsonObject data = jsonResponse.getAsJsonObject("data");
                                result = gson.fromJson(data, responseType);
                            }
                            handleSuccess(result, newSessionToken, callback);
                        } else {
                            String errorMessage = jsonResponse.has("message") ? jsonResponse.get("message").getAsString() : "Unknown error";
                            ApiError error = new ApiError(state, errorMessage);
                            handleError(error, callback);
                        }
                    } else {
                        // 处理非 JSON 对象的响应
                        handleError(new ApiError("Non-JSON response", responseBody), callback);
                    }
                } catch (JsonSyntaxException e) {
                    // 处理 JSON 解析错误
                    handleError(new ApiError("JSON parsing error", "Received malformed JSON: " + responseBody), callback);
                } catch (Exception e) {
                    handleError(new ApiError("Unexpected error", e.getMessage() + ". Response: " + responseBody), callback);
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

    public static class ApiError {
        private String state;
        private String message;

        public ApiError(String state, String message) {
            this.state = state;
            this.message = message;
        }

        public String getState() {
            return state;
        }

        public String getMessage() {
            return message;
        }
    }

}
