package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
                .clip(RoundedCornerShape(4.dp))
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
                style = SaltTheme.textStyles.main
            )
            Text(
                text = "${albumEntity.albumArtist} ${albumEntity.songCount}",
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .align(Alignment.Start),
                maxLines = 1,
                style = SaltTheme.textStyles.sub
            )
        }
    }
}

@UnstableSaltUiApi
@Composable
@Preview
fun AlbumListItemPreview() {
    AlbumListItem(
        albumEntity = AlbumEntity(
            albumId = 1,
            mediaStoreAlbumId = 1,
            albumName = "专辑名称",
            albumArtist = "专辑艺术家",
            artworkUri = "https://upload.thbwiki.cc/thumb/f/f9/Akyu%27s_Untouched_Eurobeat_Vol._2%E5%B0%81%E9%9D%A2.png/1024px-Akyu%27s_Untouched_Eurobeat_Vol._2%E5%B0%81%E9%9D%A2.png".toUri(),
            songCount = 10,
            totalDuration = 3600000
        ),
        onClick = {}
    )
}