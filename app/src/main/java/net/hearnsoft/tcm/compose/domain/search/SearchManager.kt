package net.hearnsoft.tcm.compose.domain.search

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.hearnsoft.tcm.compose.domain.model.search.SearchQuery
import net.hearnsoft.tcm.compose.domain.model.search.SearchResponse
import net.hearnsoft.tcm.compose.domain.model.search.SearchResult
import net.hearnsoft.tcm.compose.utils.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * 搜索管理器 - 协调多个搜索提供商
 */
@Singleton
class SearchManager @Inject constructor(
    private val localMusicSearchProvider: LocalMusicSearchProvider,
    private val networkContentSearchProvider: NetworkContentSearchProvider
) {
    private val TAG = "SearchManager"

    /**
     * 所有搜索提供商
     */
    private val searchProviders: List<SearchProvider> = listOf(
        localMusicSearchProvider,
        networkContentSearchProvider
    )

    /**
     * 混合搜索 - 同时搜索本地音乐和网络内容
     */
    suspend fun searchAll(query: SearchQuery): SearchResponse {
        return coroutineScope {
            try {
                Logger.debug(TAG, "开始混合搜索: ${query.query}")

                // 并行执行所有启用的搜索提供商
                val searchJobs = searchProviders
                    .filter { it.isEnabled }
                    .map { provider ->
                        async {
                            try {
                                Logger.debug(TAG, "搜索提供商 ${provider.providerName} 开始搜索")
                                provider.search(query)
                            } catch (e: CancellationException) {
                                Logger.debug(TAG, "搜索提供商 ${provider.providerName} 被取消")
                                throw e // 重新抛出取消异常
                            } catch (e: Exception) {
                                Logger.err(TAG, "搜索提供商 ${provider.providerName} 搜索失败: ${e.message}")
                                SearchResponse(results = emptyList())
                            }
                        }
                    }

                // 等待所有搜索完成
                val searchResults = searchJobs.awaitAll()

                // 合并所有结果
                val allResults = mutableListOf<SearchResult>()
                var hasMore = false

                searchResults.forEach { response ->
                    allResults.addAll(response.results)
                    if (response.hasMore) hasMore = true
                }

                // 按类型分组并排序（本地音乐优先）
                val sortedResults = allResults.sortedWith(
                    compareBy<SearchResult> { it.type.ordinal }
                        .thenBy { it.title }
                )

                Logger.debug(TAG, "混合搜索完成，共找到 ${sortedResults.size} 个结果")

                SearchResponse(
                    results = sortedResults,
                    hasMore = hasMore
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                Logger.debug(TAG, "混合搜索被取消: ${query.query}")
                throw e // 重新抛出取消异常
            } catch (e: Exception) {
                Logger.err(TAG, "混合搜索失败: ${e.message}")
                SearchResponse(results = emptyList())
            }
        }
    }

    /**
     * 获取搜索建议
     */
    suspend fun getSuggestions(query: String): List<String> {
        return coroutineScope {
            try {
                val suggestionJobs = searchProviders
                    .filter { it.isEnabled }
                    .map { provider ->
                        async { provider.getSuggestions(query) }
                    }

                val allSuggestions = suggestionJobs.awaitAll()
                allSuggestions.flatten().distinct().take(10)
            } catch (e: Exception) {
                Logger.err(TAG, "获取搜索建议失败: ${e.message}")
                emptyList()
            }
        }
    }
}