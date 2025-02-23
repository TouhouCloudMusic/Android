package net.hearnsoft.tcm.utils;

import net.hearnsoft.tcm.BuildConfig;

public class Constants {
    public static final String PREF_GLOBAL_NAME = BuildConfig.APPLICATION_ID;
    public static final String API_HOST = BuildConfig.REMOTE_SERVER;

    // 用户登录鉴权注册
    public static final String API_LOGIN = "sign_in";
    public static final String API_REGISTER = "sign_up";
    public static final String API_LOGOUT = "sign_out";
    public static final String API_USER_PROFILE = "profile";
    public static final String API_USER_AVATAR = "avatar";
    public static final String API_USER_ROLES = "user_roles";

    // API字段
    public static final String API_DOCS = API_HOST + "/docs";
    public static final String API_STATIC_IMAGE_URL = API_HOST + "/public/image/";

    // 常用shared-pref key
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_TOKEN = "session_token";

    public static final String REACH_RISK_CONTROL_STRING = "Too Many Requests!";


}
