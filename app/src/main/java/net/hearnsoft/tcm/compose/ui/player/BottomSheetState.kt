package net.hearnsoft.tcm.compose.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.constants.NavigationBarAnimationSpec

/**
 * 通用BottomSheet状态管理类
 * 支持三种状态：dismissed（隐藏）、collapsed（折叠）、expanded（展开）
 */
@Stable
class BottomSheetState(
    draggableState: DraggableState,
    private val coroutineScope: CoroutineScope,
    private val animatable: Animatable<Dp, AnimationVector1D>,
    private val onAnchorChanged: (Int) -> Unit,
    val collapsedBound: Dp,
    val expandedBound: Dp,
) : DraggableState by draggableState {
    companion object {
        private const val VELOCITY_THRESHOLD = 400f  // 阈值
        private const val POSITION_THRESHOLD_RATIO = 0.5f  // 位置阈值比例
    }

    val value by animatable.asState()

    val isCollapsed by derivedStateOf {
        value == collapsedBound
    }

    val isExpanded by derivedStateOf {
        value == expandedBound
    }

    val progress by derivedStateOf {
        (value - collapsedBound) / (expandedBound - collapsedBound)
    }

    fun collapse(animationSpec: AnimationSpec<Dp> = SpringSpec()) {
        onAnchorChanged(COLLAPSED_ANCHOR)
        coroutineScope.launch {
            animatable.animateTo(collapsedBound, animationSpec)
        }
    }

    fun expand(animationSpec: AnimationSpec<Dp> = SpringSpec()) {
        onAnchorChanged(EXPANDED_ANCHOR)
        coroutineScope.launch {
            animatable.animateTo(expandedBound, animationSpec)
        }
    }

    fun collapseSoft() {
        collapse(spring(stiffness = Spring.StiffnessMediumLow))
    }

    fun expandSoft() {
        expand(spring(stiffness = Spring.StiffnessMediumLow))
    }

    fun snapTo(value: Dp) {
        coroutineScope.launch {
            animatable.snapTo(value)
        }
    }

    fun performFling(velocity: Float) {
        val threshold = VELOCITY_THRESHOLD

        when {
            // 快速向上滑动
            velocity > threshold -> expand()
            // 快速向下滑动
            velocity < -threshold -> collapse()
            // 速度不够,根据位置判断
            else -> {
                val totalDistance = expandedBound - collapsedBound
                val currentProgress = (value - collapsedBound) / totalDistance

                if (currentProgress > POSITION_THRESHOLD_RATIO) {
                    expand()
                } else {
                    collapse()
                }
            }
        }
    }

    val consumeSwipeNestedScrollConnection
        get() = object : NestedScrollConnection {
            private var isTopReached = false

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // 只在用户手势输入时处理
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                val delta = available.y
                return when {
                    // 如果已经展开且正在往上拖，则允许里面的内容滚动
                    isExpanded && delta < 0 -> {
                        isTopReached = false
                        Offset.Zero
                    }
                    // 如果没完全展开且往上拖，优先展开BottomSheet
                    !isExpanded && delta < 0 -> {
                        dispatchRawDelta(delta)
                        available // 消费所有的滚动
                    }
                    else -> Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                val delta = available.y

                // 检测是否到达顶部
                if (!isTopReached && delta > 0) {
                    isTopReached = consumed.y == 0f
                }

                // 向下拖拽且到达顶部时,折叠 BottomSheet
                return if (isTopReached && delta > 0) {
                    dispatchRawDelta(delta)
                    available  // 消费剩余滚动
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val velocity = -available.y

                return when {
                    // 向上快速滑动且未完全展开
                    velocity > 0 && !isExpanded -> {
                        performFling(velocity)
                        available  // 消费所有速度
                    }
                    // 向下快速滑动且到达顶部
                    velocity < 0 && isTopReached -> {
                        performFling(velocity)
                        available
                    }
                    else -> Velocity.Zero
                }
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                isTopReached = false

                // 如果有剩余速度,尝试让 BottomSheet 处理
                return if (available.y != 0f) {
                    performFling(-available.y)
                    available
                } else {
                    Velocity.Zero
                }
            }
        }
}

/** 展开状态锚点 */
const val EXPANDED_ANCHOR = 1
/** 折叠状态锚点 */
const val COLLAPSED_ANCHOR = 0

/**
 * 创建BottomSheet状态
 * @param expandedBound 展开状态边界
 * @param collapsedBound 折叠状态边界
 * @param initialAnchor 初始状态锚点
 */
@Composable
fun rememberBottomSheetState(
    collapsedBound: Dp,
    expandedBound: Dp,
    initialAnchor: Int = COLLAPSED_ANCHOR,
): BottomSheetState {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var previousAnchor by rememberSaveable {
        mutableIntStateOf(initialAnchor)
    }

    val animatable = remember {
        Animatable(0.dp, Dp.VectorConverter)
    }

    return remember(collapsedBound, expandedBound, coroutineScope) {
        val initialValue = when (previousAnchor) {
            EXPANDED_ANCHOR -> expandedBound
            COLLAPSED_ANCHOR -> collapsedBound
            else -> collapsedBound
        }

        animatable.updateBounds(collapsedBound, expandedBound)
        coroutineScope.launch {
            animatable.animateTo(initialValue, NavigationBarAnimationSpec)
        }

        BottomSheetState(
            draggableState = DraggableState { delta ->
                coroutineScope.launch {
                    animatable.snapTo(animatable.value - with(density) { delta.toDp() })
                }
            },
            onAnchorChanged = { previousAnchor = it },
            coroutineScope = coroutineScope,
            animatable = animatable,
            collapsedBound = collapsedBound,
            expandedBound = expandedBound,
        )
    }
}