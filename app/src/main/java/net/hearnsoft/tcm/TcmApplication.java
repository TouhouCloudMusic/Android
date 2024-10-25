package net.hearnsoft.tcm;

import android.app.Application;

import net.hearnsoft.tcm.utils.Logs;


public class TcmApplication extends Application {

    private static final String TAG = TcmApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.d(TAG, "onApplicationInit:");
    }
}
