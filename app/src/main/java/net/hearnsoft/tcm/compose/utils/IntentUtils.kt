package net.hearnsoft.tcm.compose.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentUtils {
    private const val MUSIC_TAG_APP_PACKAGE = "com.xjcheng.musictageditor"
    private const val MUSIC_TAG_APP_ACTIVITY = "com.xjcheng.musictageditor.SongDetailActivity"

    fun openMusicTagApp(context: Context, musicUri: Uri): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = musicUri
                component = ComponentName(
                    MUSIC_TAG_APP_PACKAGE,
                    MUSIC_TAG_APP_ACTIVITY
                )
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }.onFailure {
            it.printStackTrace()
        }.isSuccess
    }

    fun openSystemEqualizer(context: Context): Boolean {
        return runCatching {
            val intent = Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }.onFailure {
            it.printStackTrace()
        }.isSuccess
    }
}