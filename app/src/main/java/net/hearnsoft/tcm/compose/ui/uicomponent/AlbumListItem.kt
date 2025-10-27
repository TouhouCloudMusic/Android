package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.AlbumEntity

@UnstableSaltUiApi
@Composable
fun AlbumListItem(
    modifier: Modifier = Modifier,
    albumEntity: AlbumEntity,
    onClick: (Long) -> Unit
) {
    // 专辑列表项 UI 组件
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .clickable { onClick(albumEntity.albumId) }
    ) {
        val artworkUri = albumEntity.artworkUri
        val subTitle = if (albumEntity.albumYear == null || albumEntity.albumYear == 0) {
            stringResource(R.string.album_info_no_year, albumEntity.songCount, albumEntity.albumArtist)
        } else {
            stringResource(R.string.album_info_with_year, albumEntity.albumYear, albumEntity.songCount, albumEntity.albumArtist)
        }

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(artworkUri)
                .crossfade(true)
                .crossfade(1000)
                .placeholder(R.drawable.ic_nav_music)
                .build(),
            modifier = Modifier
                .padding(8.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterHorizontally),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop
        )
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = albumEntity.albumName,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.Start),
                maxLines = 1,
                style = SaltTheme.textStyles.main,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subTitle,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .align(Alignment.Start),
                maxLines = 1,
                style = SaltTheme.textStyles.sub,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}