package net.hearnsoft.tcm.utils;

import androidx.media3.common.MediaItem;

import net.hearnsoft.tcm.domain.model.song.SongSortingRule;
import net.hearnsoft.tcm.infrastructure.logger.Logger;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 排序MediaItem工具类
 */
public class LocalMusicSorter {
    private static final String TAG = "LocalMusicSorter";
    // 拼音缓存
    private static final Map<String, String> pinyinCache = new LinkedHashMap<String, String>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 100; // Limit cache to 100 entries
        }
    };

    /**
     * Sort a list of MediaItems based on the provided sorting rule
     *
     * @param items List of MediaItems to sort
     * @param rule  Sorting rule to apply
     * @return Sorted list of MediaItems
     */
    public static List<MediaItem> sortMusicList(List<MediaItem> items, SongSortingRule rule) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        List<MediaItem> sortedList = new ArrayList<>(items);

        Comparator<MediaItem> comparator = null;

        switch (rule.getStrategy()) {
            case Title:
                comparator = (item1, item2) -> {
                    String title1 = item1.mediaMetadata.title != null ?
                        item1.mediaMetadata.title.toString() : "";
                    String title2 = item2.mediaMetadata.title != null ?
                        item2.mediaMetadata.title.toString() : "";
                    return compareChinese(title1, title2);
                };
                break;

            case ArtistName:
                comparator = (item1, item2) -> {
                    String artist1 = item1.mediaMetadata.artist != null ?
                        item1.mediaMetadata.artist.toString() : "";
                    String artist2 = item2.mediaMetadata.artist != null ?
                        item2.mediaMetadata.artist.toString() : "";
                    return compareChinese(artist1, artist2);
                };
                break;

            case CreatedAt:
                comparator = (item1, item2) -> {
                    String id1 = item1.mediaId.substring(item1.mediaId.lastIndexOf("/") + 1);
                    String id2 = item2.mediaId.substring(item2.mediaId.lastIndexOf("/") + 1);
                    try {
                        return Long.compare(Long.parseLong(id1), Long.parseLong(id2));
                    } catch (NumberFormatException e) {
                        return id1.compareTo(id2);
                    }
                };
                break;
            case PlayCount:
                Logger.debug(TAG, "Play count sorting not implemented yet");
                return null;
            default:
                // Default to sorting by title
                comparator = (item1, item2) -> {
                    String title1 = item1.mediaMetadata.title != null ? item1.mediaMetadata.title.toString() : "";
                    String title2 = item2.mediaMetadata.title != null ? item2.mediaMetadata.title.toString() : "";
                    return compareChinese(title1, title2);
                };
                break;
        }

        // Apply reverse sorting if needed
        if (rule.isReverse()) {
            comparator = Collections.reverseOrder(comparator);
        }

        Collections.sort(sortedList, comparator);
        return sortedList;
    }

    /**
     * 比较两个可能包含中文的字符串
     * 先按拼音排序，如果拼音相同则按原字符串排序
     */
    private static int compareChinese(String str1, String str2) {
        try {
            // 创建HanyuPinyinOutputFormat对象
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            // 设置拼音输出格式
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            format.setVCharType(HanyuPinyinVCharType.WITH_V);

            // 获取字符串的拼音
            String pinyin1 = getPinyin(str1, format);
            String pinyin2 = getPinyin(str2, format);

            // 先按拼音比较
            int result = pinyin1.compareToIgnoreCase(pinyin2);

            // 如果拼音相同则按原始字符串比较
            if (result == 0) {
                return str1.compareToIgnoreCase(str2);
            }

            return result;
        } catch (Exception e) {
            Logger.err("MusicListFragment", "Error comparing Chinese strings: " + e.getMessage());
            // 如果出现异常，返回原始比较结果
            return str1.compareToIgnoreCase(str2);
        }
    }

    /**
     * 获取字符串的拼音表示（带缓存）
     */
    private static String getPinyin(String str, HanyuPinyinOutputFormat format) throws BadHanyuPinyinOutputFormatCombination {
        if (str == null || str.isEmpty()) {
            return "";
        }

        // 检查缓存中是否已存在
        if (pinyinCache.containsKey(str)) {
            return pinyinCache.get(str);
        }

        StringBuilder pinyin = new StringBuilder();
        char[] chars = str.toCharArray();

        for (char c : chars) {
            // 判断是否为汉字
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                // 中文字符，获取拼音
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    pinyin.append(pinyinArray[0]);
                } else {
                    pinyin.append(c);
                }
            } else {
                // 非中文字符，直接添加
                pinyin.append(c);
            }
        }

        String result = pinyin.toString();
        // 存入缓存
        pinyinCache.put(str, result);

        return result;
    }
}