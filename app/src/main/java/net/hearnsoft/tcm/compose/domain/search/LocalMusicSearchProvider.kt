package net.hearnsoft.tcm.compose.domain.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.hearnsoft.tcm.compose.data.repository.MusicRepository
import net.hearnsoft.tcm.compose.domain.model.search.LocalMusicSearchResult
import net.hearnsoft.tcm.compose.domain.model.search.SearchQuery
import net.hearnsoft.tcm.compose.domain.model.search.SearchResponse
import net.hearnsoft.tcm.compose.domain.model.search.SearchResultType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本地音乐搜索提供商
 */
@Singleton
class LocalMusicSearchProvider @Inject constructor(
    private val musicRepository: MusicRepository
) : SearchProvider {

    override val providerName: String = "本地音乐"
    override val supportedType: SearchResultType = SearchResultType.LOCAL_MUSIC
    override val isEnabled: Boolean = true

    override suspend fun search(query: SearchQuery): SearchResponse {
        return withContext(Dispatchers.IO) {
            try {
                // 使用 first() 获取当前的歌曲列表，而不是持续监听
                val songs = musicRepository.getAllSongs().first()

                val filteredSongs = songs.filter { song ->
                    song.title.contains(query.query, ignoreCase = true) ||
                            song.artistName.contains(query.query, ignoreCase = true) ||
                            song.albumName.contains(query.query, ignoreCase = true)
                }.take(query.limit)

                val searchResults = filteredSongs.map { song ->
                    val matchedFields = mutableListOf<String>()
                    if (song.title.contains(query.query, ignoreCase = true)) matchedFields.add("标题")
                    if (song.artistName.contains(query.query, ignoreCase = true)) matchedFields.add("艺术家")
                    if (song.albumName.contains(query.query, ignoreCase = true)) matchedFields.add("专辑")

                    LocalMusicSearchResult(
                        id = song.songId.toString(),
                        title = song.title,
                        subtitle = "${song.artistName} - ${song.albumName}",
                        thumbnailUrl = song.artworkUri?.toString(),
                        songEntity = song,
                        matchedFields = matchedFields
                    )
                }

                SearchResponse(
                    results = searchResults,
                    hasMore = false
                )
            } catch (e: Exception) {
                SearchResponse(results = emptyList())
            }
        }
    }

    override suspend fun getSuggestions(query: String): List<String> {
        return try {
            val songs = musicRepository.getAllSongs().first()

            // 提取匹配的艺术家和专辑名作为建议
            val suggestions = mutableSetOf<String>()
            songs.forEach { song ->
                if (song.artistName.contains(query, ignoreCase = true)) {
                    suggestions.add(song.artistName)
                }
                if (song.albumName.contains(query, ignoreCase = true)) {
                    suggestions.add(song.albumName)
                }
                if (song.title.contains(query, ignoreCase = true)) {
                    suggestions.add(song.title)
                }
            }

            suggestions.take(5).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}