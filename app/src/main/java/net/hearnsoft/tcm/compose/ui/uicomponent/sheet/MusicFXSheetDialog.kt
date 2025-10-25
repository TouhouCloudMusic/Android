package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemSlider
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.utils.IntentUtils

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@UnstableSaltUiApi
@UnstableApi
@Composable
fun MusicFXSheetDialog(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    onDismissRequest: () -> Unit = {},
) {
    val context = LocalContext.current

    val playbackSpeed = playerViewModel.playbackSpeed.collectAsState().value
    val pitch = playerViewModel.pitch.collectAsState().value

    BottomSheetDialog(
        title = "音乐效果",
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        RoundedColumn {
            Item(
                text = "系统均衡器",
                onClick = {
                    IntentUtils.openSystemEqualizer(context) ?: Toast.makeText(
                        context,
                        "未检测到系统均衡器应用",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
        ItemOuterTitle("速度和音高调整")
        RoundedColumn {
            ItemSlider(
                text = "播放速度",
                value = playbackSpeed?:1.0f,
                valueRange = 0.1f..4.0f,
                steps = 40,
                onValueChange = { playerViewModel.setPlayerSpeed(it) },
                sub = String.format("%.2fx", playbackSpeed)
            )
            ItemSlider(
                text = "音高调整",
                value = pitch?:1.0f,
                valueRange = 0.1f..2f,
                steps = 20,
                onValueChange = { playerViewModel.setPlayerPitch(it) },
                sub = String.format("%.2f", pitch)
            )
        }
    }
}