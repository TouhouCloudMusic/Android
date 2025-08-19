package net.hearnsoft.tcm.compose.utils

import androidx.media3.common.Metadata
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.BinaryFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class LyricsExtractor {

    companion object {
        private const val TAG = "LyricsExtractor"

        /**
         * 从Media3的Metadata中提取歌词文本
         *
         * @param metadata Media3轨道的元数据
         * @return 歌词文本，如果没有找到则返回null
         */
        @UnstableApi
        fun extractLyricsFromMetadata(metadata: Metadata): String? {
            for (i in 0 until metadata.length()) {
                val entry = metadata[i]
                val lyricsText = extractLyricsFromEntry(entry)
                if (lyricsText != null) {
                    return lyricsText
                }
            }
            return null
        }

        /**
         * 从单个元数据条目中提取歌词
         */
        @UnstableApi
        private fun extractLyricsFromEntry(entry: Metadata.Entry): String? {
            // FLAC/OGG格式 - Vorbis注释
            if (entry is VorbisComment) {
                if ("LYRICS" == entry.key) {
                    return entry.value
                }
            }
            // MP3格式 - ID3标签，支持USLT和SYLT帧
            else if (entry is BinaryFrame) {
                if ("USLT" == entry.id || "SYLT" == entry.id) {
                    return UsltFrameDecoder.decode(ParsableByteArray(entry.data))
                }
            }
            // M4A格式 - 文本信息帧
            else if (entry is TextInformationFrame) {
                if ("USLT" == entry.id || "SYLT" == entry.id) {
                    return entry.values.joinToString("\n")
                }
            }
            return null
        }

        /**
         * 带配置选项的歌词提取方法
         */
        @UnstableApi
        fun extractLyricsFromMetadata(
            metadata: Metadata,
            options: LyricsOptions
        ): LyricsResult? {
            return try {
                val lyricsText = extractLyricsFromMetadata(metadata)
                if (lyricsText == null) {
                    null
                } else {
                    // 如果需要，去除首尾空白
                    val processedText = if (options.shouldTrimWhitespace()) {
                        lyricsText.trim()
                    } else {
                        lyricsText
                    }

                    // 检测歌词格式
                    val format = detectLyricsFormat(processedText)
                    LyricsResult(processedText, format)
                }
            } catch (e: Exception) {
                // 如果设置了错误文本，返回错误结果
                options.errorText?.let {
                    LyricsResult(it, LyricsFormat.PLAIN_TEXT)
                } ?: throw e
            }
        }

        /**
         * 检测歌词格式
         */
        private fun detectLyricsFormat(lyricsText: String): LyricsFormat {
            val trimmedText = lyricsText.trim()

            // 检测TTML格式（XML格式，包含tt标签）
            if (trimmedText.startsWith("<?xml") || trimmedText.contains("<tt ") || trimmedText.contains("<tt>")) {
                return LyricsFormat.TTML
            }

            // 检测SRT格式（以数字开头，后跟时间戳）
            if (trimmedText.matches(Regex("^1\\s*\\n.*"))) {
                return LyricsFormat.SRT
            }

            // 检测LRC格式（包含时间标记 [mm:ss.xx]）
            if (trimmedText.contains("[") && trimmedText.matches(Regex(".*\\[\\d{2}:\\d{2}[.:]\\d+].*"))) {
                return LyricsFormat.LRC
            }

            // 默认为纯文本
            return LyricsFormat.PLAIN_TEXT
        }

        /**
         * ID3v2 USLT帧解码器
         * 基于MIT许可的代码改写：https://github.com/yoheimuta/ExoPlayerMusic
         * 参考：http://id3.org/id3v2.4.0-frames
         */
        @UnstableApi
        private object UsltFrameDecoder {
            private const val ID3_TEXT_ENCODING_ISO_8859_1 = 0
            private const val ID3_TEXT_ENCODING_UTF_16 = 1
            private const val ID3_TEXT_ENCODING_UTF_16BE = 2
            private const val ID3_TEXT_ENCODING_UTF_8 = 3

            fun decode(id3Data: ParsableByteArray): String? {
                if (id3Data.limit() < 4) {
                    // 帧格式错误
                    return null
                }

                val encoding = id3Data.readUnsignedByte()
                val charset = getCharsetName(encoding)

                // 读取语言代码（3字节）
                val lang = ByteArray(3)
                id3Data.readBytes(lang, 0, 3)

                // 读取剩余数据
                val restLength = id3Data.limit() - 4
                val rest = ByteArray(restLength)
                id3Data.readBytes(rest, 0, restLength)

                // 找到描述符结束位置
                val descriptionEndIndex = indexOfEos(rest, 0, encoding)
                val textStartIndex = descriptionEndIndex + delimiterLength(encoding)
                val textEndIndex = indexOfEos(rest, textStartIndex, encoding)

                return decodeStringIfValid(rest, textStartIndex, textEndIndex, charset)
            }

            private fun getCharsetName(encodingByte: Int): Charset {
                return when (encodingByte) {
                    ID3_TEXT_ENCODING_UTF_16 -> StandardCharsets.UTF_16
                    ID3_TEXT_ENCODING_UTF_16BE -> StandardCharsets.UTF_16BE
                    ID3_TEXT_ENCODING_UTF_8 -> StandardCharsets.UTF_8
                    ID3_TEXT_ENCODING_ISO_8859_1 -> StandardCharsets.ISO_8859_1
                    else -> {
                        // 如果编码未知，使用ISO_8859_1作为默认
                        StandardCharsets.ISO_8859_1
                    }
                }
            }

            private fun indexOfEos(data: ByteArray, fromIndex: Int, encoding: Int): Int {
                var terminationPos = indexOfZeroByte(data, fromIndex)

                // 对于单字节编码，直接返回
                if (encoding == ID3_TEXT_ENCODING_ISO_8859_1 || encoding == ID3_TEXT_ENCODING_UTF_8) {
                    return terminationPos
                }

                // 对于双字节编码，确保偶数索引并查找第二个零字节
                while (terminationPos < data.size - 1) {
                    if (terminationPos % 2 == 0 && data[terminationPos + 1] == 0.toByte()) {
                        return terminationPos
                    }
                    terminationPos = indexOfZeroByte(data, terminationPos + 1)
                }

                return data.size
            }

            private fun indexOfZeroByte(data: ByteArray, fromIndex: Int): Int {
                for (i in fromIndex until data.size) {
                    if (data[i] == 0.toByte()) {
                        return i
                    }
                }
                return data.size
            }

            private fun delimiterLength(encodingByte: Int): Int {
                return if (encodingByte == ID3_TEXT_ENCODING_ISO_8859_1 ||
                    encodingByte == ID3_TEXT_ENCODING_UTF_8
                ) 1 else 2
            }

            private fun decodeStringIfValid(
                data: ByteArray,
                from: Int,
                to: Int,
                charset: Charset
            ): String {
                if (to <= from || to > data.size) {
                    return ""
                }
                return String(data, from, to - from, charset)
            }
        }
    }

    /**
     * 歌词提取配置选项
     */
    data class LyricsOptions(
        private val trimWhitespace: Boolean,
        val errorText: String?
    ) {
        fun shouldTrimWhitespace(): Boolean = trimWhitespace
    }

    /**
     * 歌词提取结果
     */
    data class LyricsResult(
        val lyricsText: String,
        val format: LyricsFormat
    )

    /**
     * 支持的歌词格式
     */
    enum class LyricsFormat {
        UNKNOWN,    // 未知格式，需要进一步解析判断
        LRC,        // .lrc格式
        TTML,       // .ttml格式（Apple Music）
        SRT,        // .srt字幕格式
        PLAIN_TEXT  // 纯文本
    }
}
