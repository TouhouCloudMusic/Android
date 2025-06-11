package net.hearnsoft.tcm.domain.model.music

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_music")
data class MusicEntity(
    @PrimaryKey
    val mediaId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumArtUri: String?,
    val duration: Long,
    val uri: String,
    val dateAdded: Long
)
