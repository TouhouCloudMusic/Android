package net.hearnsoft.tcm.compose.utils

import android.util.Log
import net.hearnsoft.tcm.compose.BuildConfig

object Logger {
    @JvmStatic
    fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    @JvmStatic
    fun debug(tag: String, message: String) {
        // Log debug messages
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    @JvmStatic
    fun warn(tag: String, message: String) {
        // Log warning messages
        Log.w(tag, message)
    }

    @JvmStatic
    fun err(tag: String, message: String, throwable: Throwable? = null) {
        // Log error messages
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
            throwable?.let { Log.e(tag, "Error details:", it) }
        }
    }
}