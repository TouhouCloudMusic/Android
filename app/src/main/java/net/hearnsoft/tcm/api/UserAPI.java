package net.hearnsoft.tcm.api;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import net.hearnsoft.tcm.beans.UserProfile;
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.enums.ErrorCode;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;

public class UserAPI {
    private static final String TAG = "UserAPI";
    private static UserAPI instance;
    private APICore apiCore;
    private SettingsPrefUtils preferences;

    private UserAPI(Context context){
        String baseApiUrl = Constants.API_HOST;
        this.apiCore = new APICore(baseApiUrl);
        this.preferences = SettingsPrefUtils.getInstance(context);

        String storedToken = preferences.readStringSettings("session_token");
        if (storedToken != null) {
            apiCore.setSessionToken(storedToken);
        }
    }

    public static synchronized UserAPI getInstance(Context context) {
        if (instance == null) {
            instance = new UserAPI(context);
        }
        return instance;
    }

    public void login (String username, String password, APICore.SessionTokenCallback<String> callback) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("username", username);
        requestData.addProperty("password", password);
        Logs.d(TAG, "login json:" + requestData);

        apiCore.callAPI(Constants.API_LOGIN, ApiMethod.POST, requestData, String.class, new APICore.SessionTokenCallback<String>() {
            @Override
            public void onSuccess(String data, String sessionToken) {
                preferences.writeStringSettings(Constants.KEY_USER_TOKEN, sessionToken);
                apiCore.setSessionToken(sessionToken);
                callback.onSuccess(data, sessionToken);
            }

            @Override
            public void onSuccess(String data) {

            }

            @Override
            public void onError(APICore.ApiError error) {
                callback.onError(error);
            }
        }, false); // 登录请求不需要包含 Cookie
    }

    public void register(String username, String password, APICore.SessionTokenCallback<String> callback) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("username", username);
        requestData.addProperty("password", password);
        Logs.d(TAG, "register json:" + requestData);

        apiCore.callAPI(Constants.API_REGISTER, ApiMethod.POST, requestData, String.class, new APICore.SessionTokenCallback<String>() {
            @Override
            public void onSuccess(String data, String sessionToken) {
                preferences.writeStringSettings(Constants.KEY_USER_TOKEN, sessionToken);
                apiCore.setSessionToken(sessionToken);
                callback.onSuccess(data, sessionToken);
            }

            @Override
            public void onSuccess(String data) {
                // 注册方法不需要该回调
            }

            @Override
            public void onError(APICore.ApiError error) {
                callback.onError(error);
            }
        }, false);// 注册请求不需要包含 Cookie
    }

    public void logout(String token, APICore.APICallback<String> callback) {
        // 首先检查token是否为空
        if (token == null || TextUtils.isEmpty(token)) {
            callback.onError(new APICore.ApiError("Unauthorized", "No session token available",
                    ErrorCode.Unauthorized));
            return;
        }

        apiCore.setSessionToken(token);
        apiCore.callAPI(Constants.API_LOGOUT, ApiMethod.GET, null, String.class,
                new APICore.APICallback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        callback.onSuccess(data);
                        // 清除存储的 token
                        preferences.writeStringSettings(Constants.KEY_USER_TOKEN, null);
                        preferences.writeStringSettings(Constants.KEY_USER_ID, null);
                    }

                    @Override
                    public void onError(APICore.ApiError error) {
                        callback.onError(error);
                    }
                }, true);
    }

    /**
     * 获取已登录本地用户信息
     * @param token 本地存储的 token
     * @param callback 回调
     */
    public void getUserProfile(String token, APICore.APICallback<UserProfile> callback) {
        String username = preferences.readStringSettings(Constants.KEY_USER_ID);
        if (token == null || TextUtils.isEmpty(token)) {
            callback.onError(new APICore.ApiError("Unauthorized",
                    "No session token available",
                    ErrorCode.Unauthorized));
            return;
        }
        if (username == null || TextUtils.isEmpty(username)) {
            callback.onError(new APICore.ApiError("Bad Request",
                    "Username is required",
                    ErrorCode.UnexpectedError));
            return;
        }

        getUserProfile(token, username, callback);
    }

    /**
     * 获取指定用户信息
     * @param token 本地存储的 token
     * @param username 用户名
     * @param callback 回调
     */
    public void getUserProfile(String token, String username, APICore.APICallback<UserProfile> callback) {
        // 首先检查token是否为空
        if (token == null || TextUtils.isEmpty(token)) {
            callback.onError(new APICore.ApiError("Unauthorized",
                    "No session token available",
                    ErrorCode.Unauthorized));
            return;
        }
        // 检查用户名是否为空
        if (username == null || TextUtils.isEmpty(username)) {
            callback.onError(new APICore.ApiError("Bad Request",
                    "Username is required",
                    ErrorCode.UnexpectedError));
            return;
        }

        String endpoint = Constants.API_USER_PROFILE + "/" + username;

        apiCore.setSessionToken(token);
        apiCore.callAPI(endpoint, ApiMethod.GET, null,
                UserProfile.class, new APICore.APICallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(APICore.ApiError error) {
                if (error.getError_code() == ErrorCode.Unauthorized) {
                    // 如果未授权，可能是 token 过期，清除存储的 token
                    preferences.writeStringSettings(Constants.KEY_USER_TOKEN, null);
                }
                callback.onError(error);
            }
        });
    }

    public void uploadAvatar(String token, Uri imageUri, ContentResolver resolver, APICore.APICallback<String> callback) {
        if (TextUtils.isEmpty(token)){
            callback.onError(new APICore.ApiError("Unauthorized", "No session token available",
                    ErrorCode.Unauthorized));
            return;
        }
        apiCore.setSessionToken(token);
        apiCore.uploadImage(Constants.API_USER_AVATAR, imageUri, resolver, callback);
    }


}
