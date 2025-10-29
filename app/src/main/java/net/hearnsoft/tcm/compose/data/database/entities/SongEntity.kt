package net.hearnsoft.tcm.compose.data.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["album_id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["album_id"]),
        Index(value = ["artist_id"]),
        Index(value = ["title"]),
        Index(value = ["media_store_id"], unique = true)
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "song_id")
    val songId: Long = 0,

    @ColumnInfo(name = "media_store_id")
    val mediaStoreId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "artist_id")
    val artistId: Long,

    @ColumnInfo(name = "album_id")
    val albumId: Long,

    @ColumnInfo(name = "artist_name")
    val artistName: String,

    @ColumnInfo(name = "album_name")
    val albumName: String,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "artwork_uri")
    val artworkUri: Uri? = null,

    @ColumnInfo(name = "content_uri")
    val contentUri: Uri,

    @ColumnInfo(name = "track_number")
    val trackNumber: Int? = null,

    @ColumnInfo(name = "disc_number")
    val discNumber: Int? = null,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "date_modified")
    val dateModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,

    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)