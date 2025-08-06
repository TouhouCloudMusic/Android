package net.hearnsoft.tcm.compose.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import android.net.Uri

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["media_store_album_id"], unique = true),
        Index(value = ["album_name"])
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "album_id")
    val albumId: Long = 0,

    @ColumnInfo(name = "media_store_album_id")
    val mediaStoreAlbumId: Long, // MediaStore 中的专辑 ID

    @ColumnInfo(name = "album_name")
    val albumName: String,

    @ColumnInfo(name = "artist_name")
    val artistName: String,

    @ColumnInfo(name = "artwork_uri")
    val artworkUri: Uri?,

    @ColumnInfo(name = "song_count")
    val songCount: Int = 0,

    @ColumnInfo(name = "total_duration")
    val totalDuration: Long = 0
)