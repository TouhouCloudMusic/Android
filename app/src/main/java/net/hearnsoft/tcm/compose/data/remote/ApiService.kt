package net.hearnsoft.tcm.compose.data.remote

import android.content.Context
import net.hearnsoft.tcm.compose.constants.THCDB_API_BASE_URL
import net.hearnsoft.thcdb_api.ApiClient
import net.hearnsoft.thcdb_api.api.UserApi

class ApiService private constructor(
    private val context: Context,
    private val isDebug: Boolean = false
){
    companion object {
        @Volatile
        private var INSTANCE: ApiService? = null

        fun getInstance(context: Context, isDebug: Boolean = false): ApiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiService(context.applicationContext, isDebug).also { INSTANCE = it }
            }
        }
    }

    private val apiClient: ApiClient by lazy {
        ApiClient.getInstance(
            context = context,
            baseUrl = THCDB_API_BASE_URL,
            isDebug = isDebug
        )
    }

    val userApi: UserApi by lazy {
        apiClient.createService(UserApi::class.java)
    }

    /**
     * 清除所有cookies
     */
    fun clearCookies() {
        apiClient.clearCookies()
    }

    /**
     * 清除特定域名的cookies
     */
    fun clearCookiesForHost(host: String) {
        apiClient.clearCookiesForHost(host)
    }
}