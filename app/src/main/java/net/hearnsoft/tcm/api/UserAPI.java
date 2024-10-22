package net.hearnsoft.tcm.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import net.hearnsoft.tcm.utils.Constants;
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
        Log.d(TAG, "login json:" + requestData);

        apiCore.callAPI(Constants.API_LOGIN, ApiMethod.POST, requestData, String.class, new APICore.SessionTokenCallback<String>() {
            @Override
            public void onSuccess(String data, String sessionToken) {
                preferences.writeStringSettings("session_token", sessionToken);
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
        Log.d(TAG, "register json:" + requestData);

        apiCore.callAPI(Constants.API_REGISTER, ApiMethod.POST, requestData, String.class, new APICore.SessionTokenCallback<String>() {
            @Override
            public void onSuccess(String data, String sessionToken) {
                preferences.writeStringSettings("session_token", sessionToken);
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



}
