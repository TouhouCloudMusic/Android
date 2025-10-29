package net.hearnsoft.tcm.compose.utils

import androidx.media3.common.MediaItem
import net.hearnsoft.tcm.compose.data.database.entities.SongEntity
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingRule
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.AlbumName
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.ArtistName
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.CreatedAt
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.DateAdded
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.Duration
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.LastPlayed
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.PlayCount
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.Title
import net.hearnsoft.tcm.compose.domain.model.song.SongSortingStrategy.UpdatedAt
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

/**
 * 排序MediaItem工具类
 */
object LocalMusicSorter {
    private const val TAG = "LocalMusicSorter"

    // 拼音缓存 - 使用 LinkedHashMap 实现 LRU 缓存
    private val pinyinCache = object : LinkedHashMap<String, String>(100, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > 100 // 限制缓存为100个条目
        }
    }

    /**
     * 根据提供的排序规则对MediaItems列表进行排序
     *
     * @param items 要排序的MediaItems列表
     * @param rule 要应用的排序规则
     * @return 排序后的MediaItems列表
     */
    @JvmName("sortMediaItemList")
    fun sortMusicList(items: List<MediaItem>?, rule: SongSortingRule): List<MediaItem> {
        if (items.isNullOrEmpty()) {
            return emptyList()
        }

        val sortedList = items.toMutableList()

        val comparator = when (rule.strategy) {
            Title -> { item1: MediaItem, item2: MediaItem ->
                val title1 = item1.mediaMetadata.title?.toString() ?: ""
                val title2 = item2.mediaMetadata.title?.toString() ?: ""
                compareChinese(title1, title2)
            }

            ArtistName -> { item1: MediaItem, item2: MediaItem ->
                val artist1 = item1.mediaMetadata.artist?.toString() ?: ""
                val artist2 = item2.mediaMetadata.artist?.toString() ?: ""
                compareChinese(artist1, artist2)
            }
            CreatedAt -> { item1: MediaItem, item2: MediaItem ->
                val id1 = item1.mediaId.substringAfterLast("/")
                val id2 = item2.mediaId.substringAfterLast("/")
                try {
                    id1.toLong().compareTo(id2.toLong())
                } catch (e: NumberFormatException) {
                    id1.compareTo(id2)
                }
            }
            UpdatedAt -> { item1: MediaItem, item2: MediaItem ->
                Logger.debug(TAG, "Play count sorting not implemented yet")
                // 默认按标题排序
                val title1 = item1.mediaMetadata.title?.toString() ?: ""
                val title2 = item2.mediaMetadata.title?.toString() ?: ""
                compareChinese(title1, title2)
            }
            PlayCount -> { item1: MediaItem, item2: MediaItem ->
                // 默认按标题排序
                val title1 = item1.mediaMetadata.title?.toString() ?: ""
                val title2 = item2.mediaMetadata.title?.toString() ?: ""
                compareChinese(title1, title2)
            }

            AlbumName -> { item1: MediaItem, item2: MediaItem ->
                val album1 = item1.mediaMetadata.albumTitle?.toString() ?: ""
                val album2 = item2.mediaMetadata.albumTitle?.toString() ?: ""
                compareChinese(album1, album2)
            }
            Duration -> TODO()
            LastPlayed -> TODO()
            DateAdded -> TODO()
        }

        // 如果需要逆序排序
        val finalComparator = if (rule.reverse) {
            Comparator<MediaItem> { item1, item2 -> comparator(item1, item2) }.reversed()
        } else {
            Comparator<MediaItem> { item1, item2 -> comparator(item1, item2) }
        }

        sortedList.sortWith(finalComparator)
        return sortedList
    }

    /**
     * 根据提供的排序规则对SongEntity列表进行排序
     *
     * @param items 要排序的SongEntity列表
     * @param rule 要应用的排序规则
     * @return 排序后的SongEntity列表
     */
    @JvmName("sortSongEntityList")
    fun sortMusicList(items: List<SongEntity>?, rule: SongSortingRule): List<SongEntity> {
        if (items.isNullOrEmpty()) {
            return emptyList()
        }

        val sortedList = items.toMutableList()

        val comparator = when (rule.strategy) {
            Title -> { item1: SongEntity, item2: SongEntity ->
                compareChinese(item1.title, item2.title)
            }

            ArtistName -> { item1: SongEntity, item2: SongEntity ->
                compareChinese(item1.artistName, item2.artistName)
            }
            CreatedAt -> { item1: SongEntity, item2: SongEntity ->
                val id1 = item1.mediaStoreId
                val id2 = item2.mediaStoreId
                try {
                    id1.compareTo(id2)
                } catch (e: NumberFormatException) {
                    id1.compareTo(id2)
                }
            }
            UpdatedAt -> { item1: SongEntity, item2: SongEntity ->
                Logger.debug(TAG, "Play count sorting not implemented yet")
                // 默认按标题排序
                compareChinese(item1.title, item2.title)
            }
            PlayCount -> { item1: SongEntity, item2: SongEntity ->
                // 这里我们需要冒泡排序，播放次数多的在前面
                val playCount1 = item1.playCount ?: 0
                val playCount2 = item2.playCount ?: 0
                playCount2.compareTo(playCount1)
            }

            AlbumName -> { item1: SongEntity, item2: SongEntity ->
                val album1 = item1.albumName ?: ""
                val album2 = item2.albumName ?: ""
                compareChinese(album1, album2)
            }
            Duration -> { item1: SongEntity, item2: SongEntity ->
                val duration1 = item1.duration ?: 0L
                val duration2 = item2.duration ?: 0L
                duration1.compareTo(duration2)
            }
            LastPlayed -> { item1: SongEntity, item2: SongEntity ->
                val lastPlayed1 = item1.lastPlayed ?: 0L
                val lastPlayed2 = item2.lastPlayed ?: 0L
                lastPlayed1.compareTo(lastPlayed2)
            }
            DateAdded -> { item1: SongEntity, item2: SongEntity ->
                val dateAdded1 = item1.dateAdded ?: 0L
                val dateAdded2 = item2.dateAdded ?: 0L
                dateAdded1.compareTo(dateAdded2)
            }
        }

        // 如果需要逆序排序
        val finalComparator = if (rule.reverse) {
            Comparator<SongEntity> { item1, item2 -> comparator(item1, item2) }.reversed()
        } else {
            Comparator<SongEntity> { item1, item2 -> comparator(item1, item2) }
        }

        sortedList.sortWith(finalComparator)
        return sortedList
    }

    /**
     * 比较两个可能包含中文的字符串
     * 先按拼音排序，如果拼音相同则按原字符串排序
     */
    private fun compareChinese(str1: String, str2: String): Int {
        return try {
            // 创建HanyuPinyinOutputFormat对象
            val format = HanyuPinyinOutputFormat().apply {
                caseType = HanyuPinyinCaseType.LOWERCASE
                toneType = HanyuPinyinToneType.WITHOUT_TONE
                vCharType = HanyuPinyinVCharType.WITH_V
            }

            // 获取字符串的拼音
            val pinyin1 = getPinyin(str1, format)
            val pinyin2 = getPinyin(str2, format)

            // 先按拼音比较
            val result = pinyin1.compareTo(pinyin2, ignoreCase = true)

            // 如果拼音相同则按原始字符串比较
            if (result == 0) {
                str1.compareTo(str2, ignoreCase = true)
            } else {
                result
            }
        } catch (e: Exception) {
            Logger.err(TAG, "Error comparing Chinese strings: ${e.message}")
            // 如果出现异常，返回原始比较结果
            str1.compareTo(str2, ignoreCase = true)
        }
    }

    /**
     * 获取字符串的拼音表示（带缓存）
     */
    @Throws(BadHanyuPinyinOutputFormatCombination::class)
    private fun getPinyin(str: String, format: HanyuPinyinOutputFormat): String {
        if (str.isEmpty()) {
            return ""
        }

        // 检查缓存中是否已存在
        pinyinCache[str]?.let { return it }

        val pinyin = StringBuilder()

        for (c in str) {
            // 判断是否为汉字
            if (c.toString().matches(Regex("[\\u4E00-\\u9FA5]+"))) {
                // 中文字符，获取拼音
                val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format)
                if (!pinyinArray.isNullOrEmpty()) {
                    pinyin.append(pinyinArray[0])
                } else {
                    pinyin.append(c)
                }
            } else {
                // 非中文字符，直接添加
                pinyin.append(c)
            }
        }

        val result = pinyin.toString()
        // 存入缓存
        pinyinCache[str] = result

        return result
    }
}