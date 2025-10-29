package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.theme.Theme

@Composable
fun PlaylistItem(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    currentPlaying: MediaItem?,
    currentPlayingIndex: Int,
    itemIndex: Int,
    textColor: Color,
    onClick: () -> Unit = {},
    onRemoveClick: (MediaItem) -> Unit = { _ -> }
) {
    val context = LocalContext.current
    val title =  mediaItem.mediaMetadata.title.toString() ?: context.getString(R.string.unknown_song)
    val artist = mediaItem.mediaMetadata.artist.toString() ?: context.getString(R.string.unknown_artist)
    val album = mediaItem.mediaMetadata.albumTitle.toString() ?: context.getString(R.string.unknown_album)

    // 比较是否是当前播放的歌曲
    val isCurrentPlaying = currentPlaying?.mediaId == mediaItem.mediaId && currentPlayingIndex == itemIndex

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isCurrentPlaying) Theme.colors.alphaStroke else Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                modifier = Modifier.padding(vertical = 1.dp),
                text = title,
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = textColor
            )
            Text(
                modifier = Modifier.padding(vertical = 1.dp),
                text = "$artist - $album",
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = textColor.copy(alpha = 0.5f)
            )
        }
        IconButton(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .align(Alignment.CenterVertically),
            onClick = {
                onRemoveClick(mediaItem)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_remove_24px),
                contentDescription = "Remove",
                tint = textColor
            )
        }
    }
}