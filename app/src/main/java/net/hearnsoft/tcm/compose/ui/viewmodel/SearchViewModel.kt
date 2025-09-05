package net.hearnsoft.tcm.compose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.domain.model.search.SearchQuery
import net.hearnsoft.tcm.compose.domain.model.search.SearchResult
import net.hearnsoft.tcm.compose.domain.search.SearchManager
import net.hearnsoft.tcm.compose.utils.Logger
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchManager: SearchManager
) : ViewModel() {

    private val TAG = "SearchViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var searchJob: Job? = null
    private var suggestionsJob: Job? = null

    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        if (query.isNotBlank()) {
            // 延迟搜索，避免频繁搜索
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(300) // 延迟300ms
                performSearch(query)
            }

            // 获取搜索建议
            getSuggestions(query)
        } else {
            // 清空结果
            _searchResults.value = emptyList()
            _suggestions.value = emptyList()
            _hasMore.value = false
        }
    }

    /**
     * 立即执行搜索
     */
    fun search(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performSearch(query)
        }
    }

    /**
     * 执行搜索
     */
    private suspend fun performSearch(query: String) {
        if (query.isBlank()) return

        try {
            _isSearching.value = true
            _errorMessage.value = null

            Logger.debug(TAG, "开始搜索: $query")

            val searchQuery = SearchQuery(query = query, limit = 20)
            val response = searchManager.searchAll(searchQuery)

            _searchResults.value = response.results
            _hasMore.value = response.hasMore

            Logger.debug(TAG, "搜索完成，找到 ${response.results.size} 个结果")
        } catch (e: kotlinx.coroutines.CancellationException) {
            // 协程取消是正常情况，不需要显示错误
            Logger.debug(TAG, "搜索被取消: $query")
            throw e // 重新抛出 CancellationException
        } catch (e: Exception) {
            Logger.err(TAG, "搜索失败: ${e.message}")
            _errorMessage.value = "搜索失败: ${e.message}"
            _searchResults.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }

    /**
     * 获取搜索建议
     */
    private fun getSuggestions(query: String) {
        if (query.length < 2) return

        suggestionsJob?.cancel()
        suggestionsJob = viewModelScope.launch {
            delay(200) // 短延迟
            try {
                val suggestions = searchManager.getSuggestions(query)
                _suggestions.value = suggestions
            } catch (e: Exception) {
                Logger.err(TAG, "获取搜索建议失败: ${e.message}")
            }
        }
    }

    /**
     * 清空搜索
     */
    fun clearSearch() {
        searchJob?.cancel()
        suggestionsJob?.cancel()
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _suggestions.value = emptyList()
        _hasMore.value = false
        _errorMessage.value = null
    }

    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        suggestionsJob?.cancel()
    }
}