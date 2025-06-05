package net.hearnsoft.tcm.utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRouter2;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class SystemMediaDialogUtils {
    private Activity context;
    private static SystemMediaDialogUtils instance;

    private SystemMediaDialogUtils(Activity context) {
        // Private constructor to prevent instantiation
        this.context = context;
    }

    public static SystemMediaDialogUtils getInstance(Activity context) {
        if (instance == null) {
            instance = new SystemMediaDialogUtils(context);
        }
        return instance;
    }

    public void showSystemMediaDialog() {
        if (context == null) {
            throw new IllegalStateException("Context is not initialized. Call getInstance() first.");
        }
        String manufacturer = Build.MANUFACTURER;
        Intent intent = new Intent();
        switch (manufacturer.toLowerCase()) {
            case "xiaomi":
            case "redmi":
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(
                    "miui.systemui.plugin",
                    "miui.systemui.miplay.MiPlayDetailActivity"
                );
                startIntent(intent);
                break;
            case "samsung":
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(
                    "com.samsung.android.mdx.quickboard",
                    "com.samsung.android.mdx.quickboard.view.MediaActivity"
                );
                startIntent(intent);
                break;
            default:
                if (Build.VERSION.SDK_INT >= UPSIDE_DOWN_CAKE) {
                    startNativeMediaDialogForU();
                } else if (Build.VERSION.SDK_INT >= S) {
                    intent.setPackage("com.android.systemui");
                    intent.setAction("com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG");
                    intent.putExtra("package_name", context.getPackageName());
                    context.sendBroadcast(intent);
                } else if (Build.VERSION.SDK_INT == R) {
                    startNativeMediaDialogForR();
                } else {
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction("com.android.settings.panel.action.MEDIA_OUTPUT");
                    intent.putExtra("com.android.settings.panel.extra.PACKAGE_NAME", context.getPackageName());
                    startIntent(intent);
                }
                break;
        }
    }

    private void startNativeMediaDialogForR() {
        Intent intent = new Intent();
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("com.android.settings.panel.action.MEDIA_OUTPUT");
        intent.putExtra("com.android.settings.panel.extra.PACKAGE_NAME", context.getPackageName());
        startIntent(intent);
    }

    @RequiresApi(UPSIDE_DOWN_CAKE)
    private void startNativeMediaDialogForU() {
        MediaRouter2 router2 = MediaRouter2.getInstance(context);
        router2.showSystemOutputSwitcher();
    }

    private void startIntent(Intent intent) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
