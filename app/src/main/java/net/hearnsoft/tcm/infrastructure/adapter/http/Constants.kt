package net.hearnsoft.tcm.infrastructure.adapter.http

import net.hearnsoft.tcm.BuildConfig

object ApiEndpoints {
    const val BASE_URL = BuildConfig.REMOTE_SERVER
    const val LOGIN = "sign_in"
}

object Constants {
    const val PREF_GLOBAL_NAME: String = BuildConfig.APPLICATION_ID
    const val API_HOST: String = BuildConfig.REMOTE_SERVER


    // 用户登录鉴权注册
    const val API_LOGIN: String = "sign_in"
    const val API_REGISTER: String = "sign_up"
    const val API_LOGOUT: String = "sign_out"
    const val API_USER_PROFILE: String = "profile"
    const val API_USER_AVATAR: String = "avatar"
    const val API_USER_ROLES: String = "user_roles"

    // API字段
    const val API_DOCS: String = "$API_HOST/docs"
    const val API_STATIC_IMAGE_URL: String = "$API_HOST/public/image/"

    // 常用shared-pref key
    const val KEY_USER_ID: String = "user_id"
    const val KEY_USER_TOKEN: String = "session_token"

    // App Action
    const val ACTION_REFRESH_USER_PROFILE: String = "ACTION_REFRESH_USER_PROFILE"
    const val ACTION_UNAUTHORIZED: String = "ACTION_UNAUTHORIZED"

    // 错误信息字符串
    const val REACH_RISK_CONTROL_STRING: String = "Too Many Requests!"
}
