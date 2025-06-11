package net.hearnsoft.tcm.infrastructure.logger

import android.util.Log
import net.hearnsoft.tcm.BuildConfig

object Logger {
    @JvmStatic
    fun info(tag: String, message: String) {
        Log.i(tag, message)
    }
    @JvmStatic
    fun debug(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    @JvmStatic
    fun warn(tag: String, message: String) {
        Log.w(tag, message)
    }
    @JvmStatic
    fun err(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }
    }
    @JvmStatic
    fun err(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }
}