package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute
import java.util.concurrent.TimeUnit


val sampleData = SongEntity(
    songId = 1L,
    mediaStoreId = 101L,
    title = "晴天",
    artistId = 201L,
    albumId = 301L,
    artistName = "周杰伦",
    albumName = "叶惠美",
    duration = TimeUnit.MINUTES.toMillis(4) + TimeUnit.SECONDS.toMillis(29), // 4:29
    filePath = "/storage/emulated/0/Music/周杰伦 - 晴天.mp3",
    artworkUri = Uri.parse("content://media/external/audio/albumart/301"),
    contentUri = Uri.parse("content://media/external/audio/media/101"),
    dateAdded = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30), // 30天前添加
    dateModified = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5), // 5天前修改
    playCount = 58,
    lastPlayed = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2), // 2小时前播放
    isFavorite = true
)


@Composable
@UnstableSaltUiApi
@ExperimentalMaterial3Api
fun SongActionSheetDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    songEntity: SongEntity,
    navController: NavController? = null
) {
    BottomSheetDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        RoundedColumn {
            SongActionHeader(songEntity = songEntity)
        }

        Spacer(modifier = Modifier.size(8.dp))

        SongActionSheetContent(
            songEntity = songEntity,
            navController = navController,
            onDismissRequest = onDismissRequest
        )
    }

}

@Composable
fun SongActionHeader(
    songEntity: SongEntity
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(songEntity.artworkUri)
                .crossfade(true)
                .placeholder(R.drawable.ic_nav_music)
                .build(),
            modifier = Modifier
                .padding(horizontal = 8.dp)
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
                text = songEntity.title ?: "未知歌曲",
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.text
            )

            val artist = songEntity.artistName ?: "未知艺术家"
            val album = songEntity.albumName ?: "未知专辑"

            val subTitle = "$artist - $album"
            Text(
                text = subTitle,
                style = SaltTheme.textStyles.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.subText
            )
        }
    }

}

@UnstableSaltUiApi
@Composable
fun SongActionSheetContent(
    songEntity: SongEntity,
    navController: NavController? = null,
    onDismissRequest: () -> Unit = {}
) {
    RoundedColumn {
        Item(
            onClick = {},
            text = "艺术家：${songEntity.artistName ?: "未知艺术家"}",
            iconPainter = painterResource(R.drawable.ic_artist_24px),
            iconColor = SaltTheme.colors.highlight,
        )
        Item(
            onClick = {
                songEntity.albumId.let { albumId ->
                    navController?.navigate(ScreenRoute.Album.createRoute(albumId))
                    onDismissRequest()
                }
            },
            text = "专辑：${songEntity.albumName ?: "未知专辑"}",
            iconPainter = painterResource(R.drawable.ic_album_24px),
            iconColor = SaltTheme.colors.highlight,
        )
        Item(
            onClick = {},
            text = "aa"
        )
        Item(
            onClick = {},
            text = "aa"
        )
        Item(
            onClick = {},
            text = "aa"
        )
    }
}

@Composable
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@Preview
fun SongActionSheetDialogPreview() {
    SongActionSheetDialog(
        songEntity = sampleData
    )
}

@Composable
@Preview
fun SongActionHeaderPreview() {
    Surface(
        color = SaltTheme.colors.background,
    ) {
        SongActionHeader(
            songEntity = sampleData
        )
    }
}

@Composable
@UnstableSaltUiApi
@Preview
fun SongActionSheetContentPreview() {
    Surface(
        color = SaltTheme.colors.background,
    ) {
        SongActionSheetContent(
            songEntity = sampleData
        )
    }
}
