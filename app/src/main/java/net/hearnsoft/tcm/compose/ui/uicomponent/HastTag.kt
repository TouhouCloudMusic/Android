package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import net.hearnsoft.tcm.compose.R

@Composable
fun HashTag(
    label: String,
) {
    ElevatedAssistChip(
        modifier = Modifier.padding(end = 4.dp),
        colors = ChipColors(
            containerColor = SaltTheme.colors.subBackground,
            leadingIconContentColor = SaltTheme.colors.text,
            labelColor = SaltTheme.colors.text,
            trailingIconContentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified,
            disabledLabelColor = Color.Unspecified,
            disabledLeadingIconContentColor = Color.Unspecified,
            disabledTrailingIconContentColor = Color.Unspecified,
        ),
        label = {
            Text(
                text = label,
                style = SaltTheme.textStyles.sub
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_tag_24px),
                contentDescription = "Tag Icon",
                modifier = Modifier.size(16.dp)
            )
        },
        onClick = {}
    )
}
