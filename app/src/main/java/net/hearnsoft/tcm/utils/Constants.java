package net.hearnsoft.tcm.utils;

import net.hearnsoft.tcm.BuildConfig;

public class Constants {
    public static final String PREF_GLOBAL_NAME = BuildConfig.APPLICATION_ID;
    public static final String API_HOST = BuildConfig.REMOTE_SERVER;

    // 用户登录鉴权注册
    public static final String API_LOGIN = "signin";
    public static final String API_REGISTER = "signup";
    public static final String API_LOGOUT = "signout";
    public static final String API_USER_PROFILE = "profile";
    public static final String API_USER_AVATAR = "avatar";

    // 常用shared-pref key
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_TOKEN = "session_token";


}
