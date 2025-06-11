package net.hearnsoft.tcm.utils

import android.util.Log
import net.hearnsoft.tcm.BuildConfig

object Logs {
    @JvmStatic
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    @JvmStatic
    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }
    }
    @JvmStatic
    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
    @JvmStatic
    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }
}