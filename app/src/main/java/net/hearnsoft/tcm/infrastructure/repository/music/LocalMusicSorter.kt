package net.hearnsoft.tcm.infrastructure.repository.music

import androidx.media3.common.MediaItem
import net.hearnsoft.tcm.domain.model.song.SongSortingRule
import net.hearnsoft.tcm.domain.model.song.SongSortingStrategy
import net.hearnsoft.tcm.infrastructure.logger.Logger.debug
import net.hearnsoft.tcm.infrastructure.logger.Logger.err
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination
import java.util.Collections

/**
 * 排序MediaItem工具类
 */
object LocalMusicSorter {
    private const val TAG = "LocalMusicSorter"

    // 拼音缓存
    private val pinyinCache: MutableMap<String, String> =
        object : LinkedHashMap<String, String>(100, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String?, String?>): Boolean {
                return size > 100 // Limit cache to 100 entries
            }
        }

    /**
     * Sort a list of MediaItems based on the provided sorting rule
     *
     * @param items List of MediaItems to sort
     * @param rule  Sorting rule to apply
     * @return Sorted list of MediaItems
     */
    fun sortMusicList(items: List<MediaItem>?, rule: SongSortingRule): List<MediaItem>? {
        if (items.isNullOrEmpty()) {
            return ArrayList()
        }
        val sortedList: List<MediaItem> = ArrayList(items)

        var comparator: Comparator<MediaItem>? = null

        comparator = when (rule.getStrategy()) {
            SongSortingStrategy.Title -> Comparator { item1: MediaItem, item2: MediaItem ->
                val title1 =
                    if (item1.mediaMetadata.title != null) item1.mediaMetadata.title.toString() else ""
                val title2 =
                    if (item2.mediaMetadata.title != null) item2.mediaMetadata.title.toString() else ""
                compareChinese(title1, title2)
            }

            SongSortingStrategy.ArtistName -> Comparator { item1: MediaItem, item2: MediaItem ->
                val artist1 =
                    if (item1.mediaMetadata.artist != null) item1.mediaMetadata.artist.toString() else ""
                val artist2 =
                    if (item2.mediaMetadata.artist != null) item2.mediaMetadata.artist.toString() else ""
                compareChinese(artist1, artist2)
            }

            SongSortingStrategy.CreatedAt -> Comparator { item1: MediaItem, item2: MediaItem ->
                val id1 = item1.mediaId.substring(item1.mediaId.lastIndexOf("/") + 1)
                val id2 = item2.mediaId.substring(item2.mediaId.lastIndexOf("/") + 1)
                try {
                    return@Comparator java.lang.Long.compare(id1.toLong(), id2.toLong())
                } catch (e: NumberFormatException) {
                    return@Comparator id1.compareTo(id2)
                }
            }

            SongSortingStrategy.PlayCount -> {
                debug(
                    TAG,
                    "Play count sorting not implemented yet"
                )
                return null
            }

            else -> {
                // Default to sorting by title
                Comparator { item1: MediaItem, item2: MediaItem ->
                    val title1 =
                        if (item1.mediaMetadata.title != null) item1.mediaMetadata.title.toString() else ""
                    val title2 =
                        if (item2.mediaMetadata.title != null) item2.mediaMetadata.title.toString() else ""
                    compareChinese(title1, title2)
                }
            }
        }

        // Apply reverse sorting if needed
        if (rule.isReverse) {
            comparator = Collections.reverseOrder(comparator)
        }

        Collections.sort(sortedList, comparator)
        return sortedList
    }

    /**
     * 比较两个可能包含中文的字符串
     * 先按拼音排序，如果拼音相同则按原字符串排序
     */
    private fun compareChinese(str1: String, str2: String): Int {
        try {
            // 创建HanyuPinyinOutputFormat对象
            val format = HanyuPinyinOutputFormat()
            // 设置拼音输出格式
            format.caseType = HanyuPinyinCaseType.LOWERCASE
            format.toneType = HanyuPinyinToneType.WITHOUT_TONE
            format.vCharType = HanyuPinyinVCharType.WITH_V

            // 获取字符串的拼音
            val pinyin1 = getPinyin(str1, format)
            val pinyin2 = getPinyin(str2, format)

            // 先按拼音比较
            val result = pinyin1!!.compareTo(pinyin2!!, ignoreCase = true)

            // 如果拼音相同则按原始字符串比较
            if (result == 0) {
                return str1.compareTo(str2, ignoreCase = true)
            }

            return result
        } catch (e: Exception) {
            err("MusicListFragment", "Error comparing Chinese strings: " + e.message)
            // 如果出现异常，返回原始比较结果
            return str1.compareTo(str2, ignoreCase = true)
        }
    }

    /**
     * 获取字符串的拼音表示（带缓存）
     */
    @Throws(BadHanyuPinyinOutputFormatCombination::class)
    private fun getPinyin(str: String?, format: HanyuPinyinOutputFormat): String? {
        if (str.isNullOrEmpty()) {
            return ""
        }

        // 检查缓存中是否已存在
        if (pinyinCache.containsKey(str)) {
            return pinyinCache[str]
        }

        val pinyin = StringBuilder()
        val chars = str.toCharArray()

        for (c in chars) {
            // 判断是否为汉字
            if (c.toString().matches("[\\u4E00-\\u9FA5]+".toRegex())) {
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