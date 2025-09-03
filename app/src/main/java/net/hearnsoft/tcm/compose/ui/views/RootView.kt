package net.hearnsoft.tcm.compose.ui.views

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import dagger.hilt.android.UnstableApi
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.constants.AppBarHeight
import net.hearnsoft.tcm.compose.constants.MiniPlayerHeight
import net.hearnsoft.tcm.compose.constants.NavigationBarAnimationSpec
import net.hearnsoft.tcm.compose.constants.NavigationBarHeight
import net.hearnsoft.tcm.compose.ui.player.BottomSheetPlayer
import net.hearnsoft.tcm.compose.ui.player.COLLAPSED_ANCHOR
import net.hearnsoft.tcm.compose.ui.player.rememberBottomSheetState
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute
import net.hearnsoft.tcm.compose.ui.screens.navigationBuilder
import net.hearnsoft.tcm.compose.ui.uicomponent.AppDrawer
import net.hearnsoft.tcm.compose.ui.uicomponent.TopAppBar
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerAwareWindowInsets
import net.hearnsoft.tcm.compose.ui.utils.appBarScrollBehavior
import net.hearnsoft.tcm.compose.ui.utils.canGoBack
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@OptIn(androidx.media3.common.util.UnstableApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@ExperimentalFoundationApi
@Composable
@UnstableApi
@UnstableSaltUiApi
@ExperimentalMaterial3Api
fun AppRootView(
    context : Activity,
    modifier: Modifier = Modifier
) {

    // 使用 Hilt 注入的 ViewModel
    val playerViewModel: PlayerViewModel = hiltViewModel()
    
    BoxWithConstraints(
        modifier = modifier
            .background(SaltTheme.colors.background)
            .fillMaxSize()
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()

        val navigationItems = remember { ScreenRoute.MainScreens }

        val density = LocalDensity.current
        val windowsInsets = WindowInsets.systemBars
        val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }

        var active by rememberSaveable { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        val shouldShowNavigationBar =
            remember(navBackStackEntry, active) {
                navBackStackEntry?.destination?.route == null ||
                        navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } &&
                        !active
            }

        val navigationBarHeight by animateDpAsState(
            targetValue = if (shouldShowNavigationBar) NavigationBarHeight else 0.dp,
            animationSpec = NavigationBarAnimationSpec,
            label = "",
        )

        val topAppBarScrollBehavior =
            appBarScrollBehavior(
                canScroll = {
                    // HACK: 临时设置，后续判断是否可以滚动
                    true
                }
            )

        val playerBottomSheetState =
            rememberBottomSheetState(
                collapsedBound = bottomInset + (if (shouldShowNavigationBar) NavigationBarHeight else 0.dp) + MiniPlayerHeight,
                expandedBound = maxHeight,
                initialAnchor = COLLAPSED_ANCHOR
            )

        // 智能的WindowInsets计算
        val playerAwareWindowInsets = remember(bottomInset, shouldShowNavigationBar) {
            var bottom = bottomInset + MiniPlayerHeight
            if (shouldShowNavigationBar) bottom += NavigationBarHeight
            windowsInsets
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                .add(WindowInsets(top = AppBarHeight, bottom = bottom))
        }

        // 记录当前的主屏幕路由，默认为 Explore
        val currentMainScreenRoute = remember { mutableStateOf(ScreenRoute.Explore.route) }
        val currentRoute = navBackStackEntry?.destination?.route

        // 当路由变化时，如果新路由是主屏幕之一，则更新状态
        if (ScreenRoute.MainScreens.any { it.route == currentRoute }) {
            currentRoute?.let { currentMainScreenRoute.value = it }
        }

        // 判断是否为二级页面
        val isSecondaryScreen = remember(currentRoute) {
            currentRoute != null && !ScreenRoute.MainScreens.any { it.route == currentRoute }
        }

        // 根据当前路由设置标题
        val title = when {
            currentRoute?.startsWith("album/") == true -> "专辑"
            currentRoute == ScreenRoute.Explore.route -> "发现"
            currentRoute == ScreenRoute.Library.route -> "曲库"
            currentRoute == ScreenRoute.Statistics.route -> "统计"
            currentRoute == ScreenRoute.Music.route -> "音乐"
            currentRoute == ScreenRoute.Account.route -> "个人"
            currentRoute == ScreenRoute.Settings.route -> "设置"
            else -> stringResource(R.string.app_name)
        }

        CompositionLocalProvider(
            // 提供智能WindowInsets给所有子Screen
            LocalPlayerAwareWindowInsets provides playerAwareWindowInsets
        ) {
            val drawerState = rememberDrawerState(DrawerValue.Closed)

            // 侧边栏打开时，让返回键优先响应关闭侧边栏
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }

            DismissibleNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    AppDrawer(
                        drawerState = drawerState,
                        scope = scope,
                        navController = navController,
                        currentMainScreenRoute = currentMainScreenRoute
                    )
                }
            ) {
                TopAppBar(
                    modifier = modifier.systemBarsPadding(),
                    title = title,
                    showSecondaryTitleBar = isSecondaryScreen,
                    onBackClick = {
                        if (navController.canGoBack) {
                            navController.popBackStack()
                        }
                    },
                    onDrawerClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    }
                )
                NavHost(
                    navController = navController,
                    startDestination = ScreenRoute.Explore.route,
                    modifier = Modifier
                        .nestedScroll(
                            topAppBarScrollBehavior.nestedScrollConnection
                        )
                        // 为NavHost添加智能边距
                        .windowInsetsPadding(playerAwareWindowInsets)
                ) {
                    navigationBuilder(
                        navController,
                        topAppBarScrollBehavior,
                        playerViewModel
                    )
                }
            }

            BottomSheetPlayer(
                state = playerBottomSheetState,
                navController = navController,
                playerViewModel = playerViewModel,
                context = context
            )

            MainBottomBar(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset {
                        if (navigationBarHeight == 0.dp) {
                            IntOffset(
                                x = 0,
                                y = (bottomInset + NavigationBarHeight).roundToPx(),
                            )
                        } else {
                            val slideOffset =
                                (bottomInset + NavigationBarHeight) *
                                        playerBottomSheetState.progress.coerceIn(
                                            0f,
                                            1f,
                                        )
                            val hideOffset =
                                (bottomInset + NavigationBarHeight) * (1 - navigationBarHeight / NavigationBarHeight)
                            IntOffset(
                                x = 0,
                                y = (slideOffset + hideOffset).roundToPx(),
                            )
                        }
                    }
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
@UnstableSaltUiApi
fun MainBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomBar(
        backgroundColor = Color.Transparent,
        modifier = modifier
    ) {
        ScreenRoute.MainScreens.forEach { screen ->
            BottomBarItem(
                text = when (screen) {
                    ScreenRoute.Explore -> "发现"
                    ScreenRoute.Library -> "曲库"
                    ScreenRoute.Statistics -> "统计"
                    ScreenRoute.Music -> "音乐"
                    ScreenRoute.Account -> "个人"
                    else -> ""
                },
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // 弹出到导航图的起始位置（ID为0），并清空包括它在内的所有内容
                            popUpTo(0) {
                                inclusive = true
                            }
                            // 确保在栈顶只有一个实例
                            launchSingleTop = true
                        }
                    }
                },
                state = currentRoute == screen.route,
                painter = painterResource(
                    id = when (screen) {
                        ScreenRoute.Explore -> R.drawable.ic_explore
                        ScreenRoute.Library -> R.drawable.ic_library_music
                        ScreenRoute.Statistics -> R.drawable.ic_nav_chart
                        ScreenRoute.Music -> R.drawable.ic_nav_music
                        ScreenRoute.Account -> R.drawable.ic_account_circle
                        else -> R.drawable.ic_explore
                    }
                ),
            )
        }
    }
}
