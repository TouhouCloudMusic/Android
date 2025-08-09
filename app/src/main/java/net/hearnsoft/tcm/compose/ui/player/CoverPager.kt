package net.hearnsoft.tcm.compose.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.constants.PlayerCoverVerticalPadding
import net.hearnsoft.tcm.compose.constants.PlayerHorizontalPadding

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CoverPager(
    modifier: Modifier = Modifier,
    artworkUri: Uri?
) {
    // 封面容器
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PlayerHorizontalPadding, vertical = PlayerCoverVerticalPadding)
            .sizeIn(maxHeight = 600.dp, maxWidth = 600.dp)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(1f)
                .scale(1f),
            shape = RoundedCornerShape(16.dp),
        ) {
            // 封面图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUri ?: R.drawable.ic_nav_music)
                    .crossfade(true)
                    .crossfade(1000)
                    .build(),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(1f),
                contentDescription = "Cover Art",
                contentScale = ContentScale.Crop
            )
        }
    }
}