package net.hearnsoft.tcm.compose.ui.views

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import dagger.hilt.android.UnstableApi
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
import net.hearnsoft.tcm.compose.ui.utils.LocalPlayerAwareWindowInsets
import net.hearnsoft.tcm.compose.ui.utils.appBarScrollBehavior
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel

@OptIn(androidx.media3.common.util.UnstableApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@ExperimentalFoundationApi
@Composable
@UnstableApi
@UnstableSaltUiApi
@ExperimentalMaterial3Api
fun AppRootView(
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

        // 获取当前路由
        val currentRoute = navBackStackEntry?.destination?.route

        // 根据当前路由设置标题
        val title = when (currentRoute) {
            ScreenRoute.Explore.route -> "发现"
            ScreenRoute.Library.route -> "曲库"
            ScreenRoute.Statistics.route -> "统计"
            ScreenRoute.Music.route -> "音乐"
            ScreenRoute.Account.route -> "个人"
            else -> "Touhou Cloud Music"
        }

        Row(
            modifier = modifier.fillMaxWidth().systemBarsPadding(),
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
        CompositionLocalProvider(
            // 提供智能WindowInsets给所有子Screen
            LocalPlayerAwareWindowInsets provides playerAwareWindowInsets
        ) {
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

            BottomSheetPlayer(
                state = playerBottomSheetState,
                navController = navController,
                playerViewModel = playerViewModel,
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
        backgroundColor = SaltTheme.colors.background,
        modifier = modifier
    ) {
        BottomBarItem(
            text = "发现",
            onClick = {
                if (currentRoute != ScreenRoute.Explore.route) {
                    navController.navigate(ScreenRoute.Explore.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Explore.route,
            painter = painterResource(id = R.drawable.ic_explore),
        )
        BottomBarItem(
            text = "曲库",
            onClick = {
                if (currentRoute != ScreenRoute.Library.route) {
                    navController.navigate(ScreenRoute.Library.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Library.route,
            painter = painterResource(id = R.drawable.ic_library_music),
        )
        BottomBarItem(
            text = "统计",
            onClick = {
                if (currentRoute != ScreenRoute.Statistics.route) {
                    navController.navigate(ScreenRoute.Statistics.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Statistics.route,
            painter = painterResource(id = R.drawable.ic_nav_chart),
        )
        BottomBarItem(
            text = "音乐",
            onClick = {
                if (currentRoute != ScreenRoute.Music.route) {
                    navController.navigate(ScreenRoute.Music.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Music.route,
            painter = painterResource(id = R.drawable.ic_nav_music),
        )
        BottomBarItem(
            text = "个人",
            onClick = {
                if (currentRoute != ScreenRoute.Account.route) {
                    navController.navigate(ScreenRoute.Account.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Account.route,
            painter = painterResource(id = R.drawable.ic_account_circle),
        )
    }
}

@OptIn(UnstableApi::class)
@ExperimentalFoundationApi
@Composable
@UnstableSaltUiApi
@ExperimentalMaterial3Api
@Preview
fun AppRootViewPreview() {
    AppRootView(
        modifier = Modifier
            .fillMaxSize()
            .safeMainPadding(),
    )
}