package net.hearnsoft.tcm.compose.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.IntOffset
import com.moriafly.salt.ui.SaltTheme

/**
 * 底部弹出面板组件
 * 从 [InnerTube](https://github.com/Malopieds/InnerTune)
 *
 * @param state 控制底部面板状态的状态对象，包含展开/折叠/关闭等状态信息
 * @param modifier 修饰符，用于自定义样式
 * @param onDismiss 可选的关闭回调，当面板被完全关闭时触发
 * @param collapsedContent 折叠状态下显示的内容
 * @param content 展开状态下显示的内容
 */
@Composable
fun BottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    collapsedContent: @Composable BoxScope.() -> Unit,
    backgroundContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val nestedScrollConnection = remember(state) {
        state.consumeSwipeNestedScrollConnection
    }

    // 主容器，通过偏移实现底部面板的滑动效果
    Box(
        modifier = modifier
            .fillMaxSize()
            // 根据状态值计算垂直偏移量，实现面板的上下移动动画
            .offset {
                val y = (state.expandedBound - state.value)
                    .roundToPx()
                    .coerceAtLeast(0) // 确保偏移量不为负数
                IntOffset(x = 0, y = y)
            }.pointerInput(state) {
                // 创建手势速度跟踪器，用于计算拖拽手势的速度
                val velocityTracker = VelocityTracker()

                // 检测垂直拖动手势
                detectVerticalDragGestures(
                    // 拖拽过程中的处理
                    onVerticalDrag = { change, dragAmount ->
                        // 记录手势变化用于速度计算
                        velocityTracker.addPointerInputChange(change)
                        // 将拖拽距离传递给状态对象进行处理
                        state.dispatchRawDelta(dragAmount)
                    },
                    // 拖拽被取消时的处理
                    onDragCancel = {
                        // 重置速度跟踪器
                        velocityTracker.resetTracking()
                        // 根据当前位置平滑动画到合适的状态
                        val midPoint = (state.expandedBound + state.collapsedBound) / 2
                        if (state.value > midPoint) {
                            state.expand()
                        } else {
                            state.collapse()
                        }
                    },
                    // 拖拽结束时的处理
                    onDragEnd = {
                        // 计算拖拽结束时的垂直速度（取负值是因为向上拖拽应为正速度）
                        val velocity = -velocityTracker.calculateVelocity().y
                        // 重置速度跟踪器
                        velocityTracker.resetTracking()
                        // 根据速度执行惯性滑动效果
                        state.performFling(velocity)
                    },
                )
            }.background(SaltTheme.colors.subBackground) // 设置背景色,
    ) {
        // 展开状态下的内容显示
        if (!state.isCollapsed) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .graphicsLayer {
                        // 根据展开进度计算透明度，实现渐显效果
                        // 当进度超过0.25时开始显示，完全展开时透明度为1
                        alpha = ((state.progress - 0.1f) * 4).coerceIn(0f, 1f)
                    }
            ) {
                backgroundContent()

                content()
            }
        }

        // 折叠状态下的内容显示（当面板未完全展开且允许显示时）
        if (!state.isExpanded) {
            Box(
                modifier =
                    Modifier
                        .graphicsLayer {
                            // 根据展开进度计算透明度，实现渐隐效果
                            // 面板展开时折叠内容逐渐变透明
                            alpha = 1f - (state.progress * 4).coerceAtMost(1f)
                        }.clickable(
                            // 设置无波纹效果的点击交互
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            // 点击时执行软展开操作
                            onClick = state::expandSoft,
                        ).fillMaxWidth()
                        // 设置折叠状态的高度
                        .height(state.collapsedBound),
                content = collapsedContent,
            )
        }
    }

}