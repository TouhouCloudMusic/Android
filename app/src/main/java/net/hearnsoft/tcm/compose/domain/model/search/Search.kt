package net.hearnsoft.tcm.compose.domain.model.search

import net.hearnsoft.tcm.compose.data.database.entities.SongEntity

/**
 * 搜索结果类型枚举
 */
enum class SearchResultType {
    LOCAL_MUSIC,    // 本地音乐
    NETWORK_CONTENT // 网络内容
}

/**
 * 搜索结果接口
 */
interface SearchResult {
    val id: String
    val title: String
    val subtitle: String?
    val type: SearchResultType
    val thumbnailUrl: String?
}

/**
 * 本地音乐搜索结果
 */
data class LocalMusicSearchResult(
    override val id: String,
    override val title: String,
    override val subtitle: String?,
    override val type: SearchResultType = SearchResultType.LOCAL_MUSIC,
    override val thumbnailUrl: String?,
    val songEntity: SongEntity,
    val matchedFields: List<String> // 匹配的字段（title, artist, album）
) : SearchResult

/**
 * 网络内容搜索结果
 */
data class NetworkContentSearchResult(
    override val id: String,
    override val title: String,
    override val subtitle: String?,
    override val type: SearchResultType = SearchResultType.NETWORK_CONTENT,
    override val thumbnailUrl: String?,
    val contentUrl: String,
    val contentType: String, // 内容类型（文章、视频等）
    val source: String // 来源网站
) : SearchResult

/**
 * 搜索查询参数
 */
data class SearchQuery(
    val query: String,
    val limit: Int = 20
)

/**
 * 搜索响应
 */
data class SearchResponse(
    val results: List<SearchResult>,
    val hasMore: Boolean = false,
    val nextPage: String? = null
)