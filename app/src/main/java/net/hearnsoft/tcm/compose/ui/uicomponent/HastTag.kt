package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.BorderStroke
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
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerUIColor

@Composable
fun HashTag(
    label: String,
) {
    val uiColor = LocalPlayerUIColor.current

    ElevatedAssistChip(
        modifier = Modifier,
        colors = ChipColors(
            containerColor = Color.DarkGray.copy(alpha = 0.2f), // 0.2f的深灰色半透明
            leadingIconContentColor = Color.Transparent,
            labelColor = SaltTheme.colors.text,
            trailingIconContentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = Color.Transparent,
            disabledLeadingIconContentColor = Color.Transparent,
            disabledTrailingIconContentColor = Color.Transparent,
        ),
        label = {
            Text(
                text = label,
                style = SaltTheme.textStyles.sub,
                color = uiColor
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_tag_24px),
                contentDescription = "Tag Icon",
                modifier = Modifier.size(16.dp),
                tint = uiColor
            )
        },
        onClick = {},
        border = BorderStroke(
            width = 1.dp,
            color = uiColor
        ),
        elevation = null
    )
}
