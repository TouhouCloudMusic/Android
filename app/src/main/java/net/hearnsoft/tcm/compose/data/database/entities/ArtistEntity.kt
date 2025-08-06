package net.hearnsoft.tcm.compose.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["artist_name"], unique = true)
    ]
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "artist_id")
    val artistId: Long = 0,

    @ColumnInfo(name = "artist_name")
    val artistName: String,

    @ColumnInfo(name = "album_count")
    val albumCount: Int = 0,

    @ColumnInfo(name = "song_count")
    val songCount: Int = 0
)