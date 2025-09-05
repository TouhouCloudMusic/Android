package net.hearnsoft.tcm.compose.domain.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hearnsoft.tcm.compose.domain.model.search.NetworkContentSearchResult
import net.hearnsoft.tcm.compose.domain.model.search.SearchQuery
import net.hearnsoft.tcm.compose.domain.model.search.SearchResponse
import net.hearnsoft.tcm.compose.domain.model.search.SearchResultType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络内容搜索提供商
 * 注意: 仅搜索网络内容（文章、视频等），不包含音乐内容
 */
@Singleton
class NetworkContentSearchProvider @Inject constructor(
    // 这里可以注入网络API客户端
) : SearchProvider {

    override val providerName: String = "网络内容"
    override val supportedType: SearchResultType = SearchResultType.NETWORK_CONTENT
    override val isEnabled: Boolean = false // 默认禁用

    override suspend fun search(query: SearchQuery): SearchResponse {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: 实现实际的网络搜索API调用
                // 注意: 确保不搜索音乐内容，只搜索文章、视频等其他内容

                // 示例实现
                val mockResults = listOf(
                    NetworkContentSearchResult(
                        id = "1",
                        title = "示例文章: ${query.query}",
                        subtitle = "这是一个关于${query.query}的示例文章",
                        thumbnailUrl = null,
                        contentUrl = "https://example.com/article/1",
                        contentType = "article",
                        source = "示例网站"
                    )
                )

                SearchResponse(
                    results = mockResults,
                    hasMore = true
                )
            } catch (e: Exception) {
                SearchResponse(results = emptyList())
            }
        }
    }
}