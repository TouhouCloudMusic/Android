package net.hearnsoft.tcm.compose.ui.utils

import net.hearnsoft.tcm.compose.constants.THCDB_IMAGE_BASE_URL

fun getFullImageUrl(path: String?): String {
    return if (path.isNullOrEmpty()) {
        ""
    } else {
        THCDB_IMAGE_BASE_URL + path
    }
}