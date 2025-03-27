package net.hearnsoft.tcm.infrastructure.adapter.http

import android.content.Context
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitClient {
    companion object {
        @Volatile
        private var instance: Retrofit? = null

        fun init(context: Context): Retrofit {
            return instance ?: synchronized(this) {
                if (instance != null) {
                    return instance!!
                } else {
                    val cookieJar: ClearableCookieJar =
                        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

                    val okHttpClient = OkHttpClient.Builder()
                        .cookieJar(cookieJar)
                        .build()

                    instance = Retrofit.Builder().baseUrl(ApiEndpoints.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build()

                    return instance!!
                }
            }
        }
    }
}