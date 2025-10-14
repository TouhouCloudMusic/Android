package net.hearnsoft.tcm.compose.ui.screens

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemOuterTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.domain.model.search.LocalMusicSearchResult
import net.hearnsoft.tcm.compose.domain.model.search.NetworkContentSearchResult
import net.hearnsoft.tcm.compose.domain.model.search.SearchResult
import net.hearnsoft.tcm.compose.domain.model.search.SearchResultType
import net.hearnsoft.tcm.compose.ui.viewmodel.PlayerViewModel
import net.hearnsoft.tcm.compose.ui.viewmodel.SearchViewModel

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableSaltUiApi
@UnstableApi
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {

    val searchResults by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val searchQuery by searchViewModel.searchQuery.collectAsState()

    Column(modifier.fillMaxSize()) {

        Spacer(modifier = Modifier.height(8.dp))

        // 什么都不做时显示的默认内容
        AnimatedVisibility(
            visible = !isSearching && searchResults.isEmpty() && searchQuery.isBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.no_item),
                        contentDescription = "搜索",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "键入关键字以搜索",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "可以搜索本机音乐以及东方同音鉴站内的任何内容",
                        style = SaltTheme.textStyles.sub
                    )
                }
            }
        }

        // 搜索结果为空时的显示（有搜索但无结果）
        AnimatedVisibility(
            visible = !isSearching && searchResults.isEmpty() && searchQuery.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.no_item), // 无结果图标
                        contentDescription = "无搜索结果",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "未找到相关内容",
                        style = SaltTheme.textStyles.main,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "尝试使用其他关键词",
                        style = SaltTheme.textStyles.sub
                    )
                }
            }
        }

        // 搜索结果
        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // 显示加载指示器
            RoundedColumn {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "正在搜索...",
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = searchResults.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn {
                // 按搜索结果类型分组
                val groupedResults = searchResults.groupBy { it.type }

                // 本地音乐结果
                groupedResults[SearchResultType.LOCAL_MUSIC]?.let { localResults ->
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SearchResultSection(
                            title = "本地音乐",
                            results = localResults,
                            playerViewModel = playerViewModel
                        )
                    }
                }

                // 网络内容结果
                groupedResults[SearchResultType.NETWORK_CONTENT]?.let { networkResults ->
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SearchResultSection(
                            title = "网络内容",
                            results = networkResults,
                            playerViewModel = playerViewModel
                        )
                    }
                }
            }
        }
    }
}


@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableSaltUiApi
@Composable
private fun SearchResultSection(
    title: String,
    results: List<SearchResult>,
    playerViewModel: PlayerViewModel
) {
    // 分组标题
    ItemOuterTitle(
        text = "$title (${results.size})"
    )
    RoundedColumn {
        // 搜索结果列表
        results.forEach { result ->
            SearchResultItem(
                result = result,
                onClick = {
                    handleSearchResultClick(result, playerViewModel)
                }
            )
        }
    }
}

@UnstableSaltUiApi
@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    val icon = when (result.type) {
        SearchResultType.LOCAL_MUSIC -> R.drawable.ic_nav_music
        else -> R.drawable.ic_explore
    }

    val subtitle = when (result) {
        is LocalMusicSearchResult -> {
            "${result.subtitle} • 匹配: ${result.matchedFields.joinToString(", ")}"
        }
        is NetworkContentSearchResult -> {
            "${result.subtitle} • ${result.source}"
        }
        else -> result.subtitle
    }

    Item(
        onClick = onClick,
        text = result.title,
        sub = subtitle,
        iconPainter = painterResource(icon)
    )
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@UnstableSaltUiApi
@UnstableApi
private fun handleSearchResultClick(
    result: SearchResult,
    playerViewModel: PlayerViewModel
) {
    when (result) {
        is LocalMusicSearchResult -> {
            // 播放本地音乐
            // playerViewModel.playSong(result.songEntity)
            playerViewModel.playSong(result.songEntity)
        }
        is NetworkContentSearchResult -> {
            // 打开网络内容
            // 可以打开浏览器或内置WebView
        }
    }
}