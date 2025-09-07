package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.domain.model.album.AlbumSortingRule
import net.hearnsoft.tcm.compose.domain.model.album.AlbumSortingStrategy
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingRule
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy

@ExperimentalMaterial3Api
@Composable
fun AlbumSortSheetDialog(
    modifier: Modifier = Modifier,
    currentRule: AlbumSortingRule,
    onSortRuleSelected: (AlbumSortingRule) -> Unit,
    onDismissRequest: () -> Unit = {},
) {

    val sortRulesOptions = listOf(
        "标题" to AlbumSortingStrategy.AlbumName,
        "年份" to AlbumSortingStrategy.AlbumYear,
        "数量" to AlbumSortingStrategy.SongCount,
    )

    BottomSheetDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "音乐排序规则",
    ) { dismiss ->
        RoundedColumn(modifier.selectableGroup()) {
            sortRulesOptions.forEach { (optionText, strategy) ->
                val isSelected = currentRule.strategy == strategy
                val arrow = if (isSelected) {
                    if (currentRule.reverse)
                        painterResource(R.drawable.ic_sort_alphabetical_descending)
                    else
                        painterResource(R.drawable.ic_sort_alphabetical_ascending)
                } else null

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                val newRule = if (isSelected) {
                                    // 如果点击的是当前选中项，切换reverse状态
                                    AlbumSortingRule(strategy, !currentRule.reverse)
                                } else {
                                    // 如果点击的是其他项，选择该项且默认为正序
                                    AlbumSortingRule(strategy, false)
                                }
                                onSortRuleSelected(newRule)
                                dismiss()
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Text(
                        text = optionText,
                        style = SaltTheme.textStyles.main,
                        color = if (isSelected) SaltTheme.colors.highlight else SaltTheme.colors.text,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                    )
                    if (arrow != null) {
                        Icon(
                            painter = arrow,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
