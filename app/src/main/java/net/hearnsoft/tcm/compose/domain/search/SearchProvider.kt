package net.hearnsoft.tcm.compose.domain.search

import net.hearnsoft.tcm.compose.domain.model.search.SearchQuery
import net.hearnsoft.tcm.compose.domain.model.search.SearchResponse
import net.hearnsoft.tcm.compose.domain.model.search.SearchResultType

/**
 * 搜索提供商接口
 */
interface SearchProvider {
    /**
     * 搜索提供商名称
     */
    val providerName: String

    /**
     * 支持的搜索结果类型
     */
    val supportedType: SearchResultType

    /**
     * 是否启用
     */
    val isEnabled: Boolean

    /**
     * 执行搜索
     */
    suspend fun search(query: SearchQuery): SearchResponse

    /**
     * 搜索建议/自动完成
     */
    suspend fun getSuggestions(query: String): List<String> = emptyList()
}