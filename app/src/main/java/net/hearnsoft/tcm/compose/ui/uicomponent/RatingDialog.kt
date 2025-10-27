package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.outerPadding
import net.hearnsoft.tcm.compose.R

@UnstableSaltUiApi
@Composable
fun RatingDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: (rating: Float) -> Unit
) {
    var rating: Float by remember { mutableFloatStateOf(0f) }

    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties()
    ) {
        DialogTitle(text = stringResource(R.string.rate_dialog_title))
        RatingBar(
            modifier = Modifier.outerPadding(),
            value = rating,
            style = RatingBarStyle.Stroke(),
            stepSize = StepSize.HALF,
            onValueChange = { rating = it },
            numOfStars = 5,
            onRatingChanged = {
                rating = it
            },
        )
        Row(
            modifier = Modifier.outerPadding()
        ) {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.cancel),
                textColor = SaltTheme.colors.subText,
                backgroundColor = SaltTheme.colors.subBackground
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.padding))
            TextButton(
                onClick = {
                    onConfirm(rating)
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.submit)
            )
        }
    }
}