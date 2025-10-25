package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun BottomSheetDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    title: String = "",
    skipPartiallyExpanded: Boolean = false,
    showDragHandle: Boolean = false,
    content: @Composable (dismiss: () -> Unit) -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val scope = rememberCoroutineScope()

    // 处理关闭操作的统一方法
    val handleDismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        dragHandle = if (showDragHandle) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            { /* 空内容，不显示拖拽手柄 */ }
        },
        onDismissRequest = {
            // 下滑dismiss时触发
            onDismissRequest()
        },
        sheetState = sheetState,
        modifier = modifier,
        contentColor = SaltTheme.colors.background,
        containerColor = SaltTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = SaltTheme.textStyles.main,
                    color = SaltTheme.colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp)
                )
            }
            // 将 dismiss 方法传递给 content
            content(handleDismiss)
        }
    }
}