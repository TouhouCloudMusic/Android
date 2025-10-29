package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity

@Composable
@UnstableSaltUiApi
fun MusicListItem(
    modifier: Modifier = Modifier,
    songEntity: SongEntity,
    currentPlaying: MediaItem?,
    onClick: () -> Unit = {},
    onActionClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isCurrentPlaying = currentPlaying?.mediaId == songEntity.mediaStoreId.toString()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        val artworkUri = songEntity.artworkUri ?: R.drawable.ic_nav_music.toDrawable()

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(artworkUri)
                .crossfade(true)
                .crossfade(1000)
                .placeholder(R.drawable.ic_nav_music)
                .build(),
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp))
                .align(Alignment.CenterVertically),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = songEntity.title ?: stringResource(R.string.unknown_song),
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentPlaying) SaltTheme.colors.highlight else SaltTheme.colors.text
            )

            val artist = songEntity.artistName ?: stringResource(R.string.unknown_artist)
            val album = songEntity.albumName ?: stringResource(R.string.unknown_album)

            val subTitle = "$artist - $album"
            Text(
                text = subTitle,
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentPlaying) SaltTheme.colors.highlight else SaltTheme.colors.subText
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(8.dp),
            onClick = { onActionClick() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert_24px),
                contentDescription = "More Options",
            )
        }
    }
}

@Composable
@UnstableSaltUiApi
@Preview(name = "isCurrent")
fun MusicListItemPreview() {
    MusicListItem(
        modifier = Modifier.background(SaltTheme.colors.background),
        songEntity = SongEntity(
            songId = 1,
            mediaStoreId = 123456789L,
            title = "Sample Song",
            artistId = 1,
            albumId = 1,
            artistName = "Sample Artist",
            albumName = "Sample Album",
            duration = 240000L,
            filePath = "/path/to/song.mp3",
            artworkUri = null, // 可以替换为实际的Uri
            contentUri = "content://media/external/audio/media/123456789".toUri()
        ),
        currentPlaying = MediaItem.Builder()
            .setMediaId("123456789")
            .build()
    )
}

@Composable
@UnstableSaltUiApi
@Preview(name = "isNotCurrent")
fun MusicListItemNotCurrentPreview() {
    MusicListItem(
        modifier = Modifier.background(SaltTheme.colors.background),
        songEntity = SongEntity(
            songId = 1,
            mediaStoreId = 123456789L,
            title = "Sample Song",
            artistId = 1,
            albumId = 1,
            artistName = "Sample Artist",
            albumName = "Sample Album",
            duration = 240000L,
            filePath = "/path/to/song.mp3",
            artworkUri = null, // 可以替换为实际的Uri
            contentUri = "content://media/external/audio/media/123456789".toUri()
        ),
        currentPlaying = MediaItem.Builder()
            .build()
    )
}