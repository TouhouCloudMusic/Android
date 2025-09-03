package net.hearnsoft.tcm.compose.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableSaltUiApi
@UnstableApi
@Composable
fun MusicScanScreen(
    modifier: Modifier = Modifier
) {

    val playerViewModel: PlayerViewModel = hiltViewModel()

    val isLoading by playerViewModel.isLoading.collectAsState()

    RoundedColumn {
        Item(
            text = "扫描音乐",
            onClick = {
                playerViewModel.scanAndUpdateMusicLibrary()
            },
            arrowType = ItemArrowType.None
        )
    }

    if (isLoading) {
        RoundedColumn {

        }
    }

}