package net.hearnsoft.tcm;

import android.app.Application;
import android.util.Log;


public class TcmApplication extends Application {

    private static final String TAG = TcmApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onApplicationInit:");
    }
}
