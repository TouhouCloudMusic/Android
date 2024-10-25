package net.hearnsoft.tcm.utils;

import android.util.Log;
import net.hearnsoft.tcm.BuildConfig;


public class Logs {

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag,msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag,msg);
        }
    }

}
