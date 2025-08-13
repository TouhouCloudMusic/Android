package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.Text
import net.hearnsoft.tcm.compose.R

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {},
            modifier = modifier.padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_24px),
                contentDescription = "侧边栏抽屉"
            )
        }
        Text(
            text = title,
            modifier = modifier.fillMaxWidth().weight(1f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        IconButton(
            onClick = { /* TODO: 打开搜索界面 */ },
            modifier = modifier.padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_24px),
                contentDescription = "搜索"
            )
        }
    }
}