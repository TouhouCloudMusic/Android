package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.ItemSlider
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
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
        title = stringResource(R.string.music_effects),
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        RoundedColumn {
            Item(
                text = stringResource(R.string.system_equalizer),
                onClick = {
                    IntentUtils.openSystemEqualizer(context) ?: Toast.makeText(
                        context,
                        context.getString(R.string.equalizer_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
        ItemOuterTitle(stringResource(R.string.speed_and_pitch))
        RoundedColumn {
            ItemSlider(
                text = stringResource(R.string.playback_speed),
                value = playbackSpeed?:1.0f,
                valueRange = 0.2f..2.0f,
                steps = 35,
                onValueChange = { 
                    // 舍入到最接近的 0.05 的倍数
                    val roundedValue = (it * 20).toInt() / 20f
                    playerViewModel.setPlayerSpeed(roundedValue)
                 },
                sub = String.format("%.2fx", playbackSpeed)
            )
            ItemSlider(
                text = stringResource(R.string.pitch_adjustment),
                value = pitch?:1.0f,
                valueRange = 0.1f..2.0f,
                steps = 18,
                onValueChange = { 
                    val roundedValue = (it * 10).toInt() / 10f
                    playerViewModel.setPlayerPitch(roundedValue)
                 },
                sub = String.format("%.2f", pitch)
            )
        }
    }
}