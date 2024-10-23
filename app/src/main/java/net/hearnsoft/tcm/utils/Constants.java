package net.hearnsoft.tcm.utils;

import net.hearnsoft.tcm.BuildConfig;

public class Constants {
    public static final String PREF_GLOBAL_NAME = BuildConfig.APPLICATION_ID;
    public static final String API_HOST = "http://192.168.27.190:11451";

    // 用户登录鉴权注册
    public static final String API_LOGIN = "sign-in";
    public static final String API_REGISTER = "sign-up";
    public static final String API_LOGOUT = "sign-out";

    // 常用shared-pref key
    public static final String KEY_USER_TOKEN = "session_token";


}
