package net.hearnsoft.tcm.compose.data.database.converters

import androidx.room.TypeConverter
import android.net.Uri

class UriConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}