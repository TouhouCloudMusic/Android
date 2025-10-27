package net.hearnsoft.tcm.compose.ui.uicomponent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Icon
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.icons.ArrowBack
import com.moriafly.salt.ui.icons.SaltIcons
import com.moriafly.salt.ui.noRippleClickable
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.viewmodel.SearchViewModel

enum class TopAppBarType {
    MAIN, SECONDARY, SEARCH
}

@UnstableSaltUiApi
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    titleBarType: TopAppBarType = TopAppBarType.MAIN,
    searchViewModel: SearchViewModel,
    onBackClick: () -> Unit = { },
    onDrawerClick: () -> Unit = { },
    onSearchClick: () -> Unit = { },
) {
    // 使用 AnimatedContent 实现淡入淡出动画
    AnimatedContent(
        targetState = titleBarType,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        modifier = modifier,
        label = "TopAppBarTransition"
    ) { targetType ->
        when (targetType) {
            TopAppBarType.MAIN ->
                MainTopAppBar(
                    title = title,
                    onDrawerClick = onDrawerClick,
                    onSearchClick = onSearchClick
                )

            TopAppBarType.SECONDARY ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TitleBar(
                        onBack = onBackClick,
                        text = title,
                        showBackBtn = true
                    )
                }

            TopAppBarType.SEARCH ->
                SearchTopAppBar(
                    searchViewModel = searchViewModel,
                    onBackClick = onBackClick
                )
        }
    }
}

@UnstableSaltUiApi
@Composable
fun MainTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onDrawerClick: () -> Unit = { },
    onSearchClick: () -> Unit = { }
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onDrawerClick() },
            modifier = modifier.padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_24px),
                contentDescription = stringResource(R.string.cd_drawer)
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
            onClick = { onSearchClick() },
            modifier = modifier.padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_24px),
                contentDescription = stringResource(R.string.cd_search)
            )
        }
    }
}

@UnstableSaltUiApi
@Composable
fun SearchTopAppBar(
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel,
    onBackClick: () -> Unit = { }
) {
    val searchQuery by searchViewModel.searchQuery.collectAsState()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val backButtonContentDescription = stringResource(R.string.back_btn)
        Icon(
            modifier = Modifier
                .size(56.dp)
                .semantics {
                    this.role = Role.Button
                    this.contentDescription = backButtonContentDescription
                }
                .noRippleClickable {
                    onBackClick()
                }
                .padding(18.dp),
            painter = rememberVectorPainter(SaltIcons.ArrowBack),
            contentDescription = backButtonContentDescription,
            tint = SaltTheme.colors.text
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemEdit(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                text = searchQuery,
                onChange = { query ->
                    searchViewModel.updateSearchQuery(query)
                },
                hint = "在此处键入关键字以搜索",
                hintColor = SaltTheme.colors.text,
            )

            // 清除按钮，只在有内容时显示
            if (searchQuery.isNotEmpty()) {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            this.role = Role.Button
                            this.contentDescription = "清除搜索"
                        }
                        .noRippleClickable {
                            searchViewModel.clearSearch()
                        }
                        .padding(12.dp),
                    painter = painterResource(id = R.drawable.ic_close_24px),
                    contentDescription = "清除搜索",
                    tint = SaltTheme.colors.text
                )
            }
        }
    }
}