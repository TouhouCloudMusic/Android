package net.hearnsoft.tcm.compose.utils

import android.app.Activity
import android.content.Intent
import android.media.MediaRouter2
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import java.lang.ref.WeakReference

class SystemMediaDialogUtils private constructor(private val context: Activity) {
    companion object {
        @Volatile
        private var instance: WeakReference<SystemMediaDialogUtils>? = null

        fun getInstance(context: Activity): SystemMediaDialogUtils {
            return instance?.get() ?: synchronized(this) {
                instance?.get() ?: SystemMediaDialogUtils(context).also {
                    instance = WeakReference(it)
                }
            }
        }
    }

    fun showSystemMediaDialog() {
        val manufacturer = Build.MANUFACTURER
        val intent = Intent()

        when (manufacturer.lowercase()) {
            "xiaomi", "redmi" -> {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setClassName(
                    "miui.systemui.plugin",
                    "miui.systemui.miplay.MiPlayDetailActivity"
                )
                startIntent(intent)
            }
            "samsung" -> {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setClassName(
                    "com.samsung.android.mdx.quickboard",
                    "com.samsung.android.mdx.quickboard.view.MediaActivity"
                )
                startIntent(intent)
            }
            else -> {
                when {
                    Build.VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        startNativeMediaDialogForU()
                    }
                    Build.VERSION.SDK_INT >= VERSION_CODES.S -> {
                        intent.setPackage("com.android.systemui")
                        intent.action = "com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG"
                        intent.putExtra("package_name", context.packageName)
                        context.sendBroadcast(intent)
                    }
                    Build.VERSION.SDK_INT == VERSION_CODES.R -> {
                        startNativeMediaDialogForR()
                    }
                    else -> {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.action = "com.android.settings.panel.action.MEDIA_OUTPUT"
                        intent.putExtra("com.android.settings.panel.extra.PACKAGE_NAME", context.packageName)
                        startIntent(intent)
                    }
                }
            }
        }
    }

    private fun startNativeMediaDialogForR() {
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = "com.android.settings.panel.action.MEDIA_OUTPUT"
            putExtra("com.android.settings.panel.extra.PACKAGE_NAME", context.packageName)
        }
        startIntent(intent)
    }

    @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun startNativeMediaDialogForU() {
        val router2 = MediaRouter2.getInstance(context)
        router2.showSystemOutputSwitcher()
    }

    private fun startIntent(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
