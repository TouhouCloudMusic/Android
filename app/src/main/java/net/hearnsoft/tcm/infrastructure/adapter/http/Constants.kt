package net.hearnsoft.tcm.infrastructure.adapter.http

import net.hearnsoft.tcm.BuildConfig

object Constants {
    const val PREF_GLOBAL_NAME: String = BuildConfig.APPLICATION_ID

    @JvmField
    val API_HOST: String = BuildConfig.REMOTE_SERVER.trimEndingSlash()

    // API字段
    @JvmField
    val API_DOCS: String = "$API_HOST/docs"
    @JvmField
    val API_STATIC_IMAGE_URL: String = "$API_HOST/public/image/"

    // App Action
    const val ACTION_UNAUTHORIZED: String = "ACTION_UNAUTHORIZED"

    /**
     * 移除URL末尾的斜杠（如果存在）
     * @return 处理后的URL字符串
     */
    fun String.trimEndingSlash(): String {
        return if (this.endsWith("/")) {
            this.substring(0, this.length - 1)
        } else {
            this
        }
    }
}
