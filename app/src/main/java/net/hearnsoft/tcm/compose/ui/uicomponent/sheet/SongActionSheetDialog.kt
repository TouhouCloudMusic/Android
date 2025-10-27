package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
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
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.utils.IntentUtils


@ExperimentalFoundationApi
@UnstableApi
@Composable
@UnstableSaltUiApi
@ExperimentalMaterial3Api
fun SongActionSheetDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    playerViewModel: PlayerViewModel,
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
            playerViewModel = playerViewModel,
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
    val context = LocalContext.current
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
                text = songEntity.title ?: context.getString(R.string.unknown_song),
                style = SaltTheme.textStyles.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SaltTheme.colors.text
            )

            val artist = songEntity.artistName ?: context.getString(R.string.unknown_artist)
            val album = songEntity.albumName ?: context.getString(R.string.unknown_album)

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

@ExperimentalFoundationApi
@UnstableApi
@Composable
@UnstableSaltUiApi
@ExperimentalMaterial3Api
fun SongActionSheetContent(
    playerViewModel: PlayerViewModel,
    songEntity: SongEntity,
    navController: NavController? = null,
    onDismissRequest: () -> Unit = {}
) {
    val context = LocalContext.current

    RoundedColumn {
        Item(
            onClick = {
                playerViewModel.addToPlayNext(songEntity)
                onDismissRequest()
            },
            text = stringResource(R.string.add_to_next),
            iconPainter = painterResource(R.drawable.ic_playlist_play_24px),
            iconColor = SaltTheme.colors.highlight,
        )
        Item(
            onClick = {
                songEntity.artistId.let { artistId ->
                    /*navController?.navigate(ScreenRoute.Artist.createRoute(artistId))*/
                    onDismissRequest()
                }
            },
            text = stringResource(R.string.artist_label, songEntity.artistName ?: context.getString(R.string.unknown_artist)),
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
            text = stringResource(R.string.album_label, songEntity.albumName ?: context.getString(R.string.unknown_album)),
            iconPainter = painterResource(R.drawable.ic_album_24px),
            iconColor = SaltTheme.colors.highlight,
        )
        Item(
            onClick = {
                if (!IntentUtils.openMusicTagApp(context = context, musicUri = songEntity.contentUri)) {
                    Toast.makeText(context, context.getString(R.string.music_tag_not_found), Toast.LENGTH_SHORT).show()
                }
                onDismissRequest()
            },
            text = stringResource(R.string.edit_in_music_tag),
            iconPainter = painterResource(R.drawable.ic_edit_24px),
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
    }
}