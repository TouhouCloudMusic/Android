package net.hearnsoft.tcm.compose.ui.uicomponent.sheet

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.UiMode
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun BottomSheetDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    title: String = "",
    skipPartiallyExpanded: Boolean = false,
    showDragHandle: Boolean = true,
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
            { /* 默认拖拽手柄 */ }
        } else {
            { /* 空内容，不显示拖拽手柄 */ }
        },
        onDismissRequest = {
            // 下滑dismiss时触发
            onDismissRequest()
        },
        sheetState = sheetState,
        modifier = modifier,
        contentColor = SaltTheme.colors.subBackground,
        containerColor = SaltTheme.colors.subBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(SaltTheme.colors.subBackground)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = SaltTheme.textStyles.main,
                    color = SaltTheme.colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                )
            }
            // 将 dismiss 方法传递给 content
            content(handleDismiss)
        }
    }
}

@ExperimentalMaterial3Api
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomSheetDialogPreview() {
    BottomSheetDialog(
        onDismissRequest = { /* 预览中不需要处理 */ },
        title = "foo"
    ) { dismiss ->
        // 示例内容
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "这是一个示例内容",
                style = SaltTheme.textStyles.main
            )
            Button(
                onClick = { dismiss() },
                text = "关闭"
            )
        }
    }
}