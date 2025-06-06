package net.hearnsoft.tcm.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Metadata;
import androidx.media3.common.util.ParsableByteArray;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.extractor.metadata.id3.BinaryFrame;
import androidx.media3.extractor.metadata.id3.TextInformationFrame;
import androidx.media3.extractor.metadata.vorbis.VorbisComment;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LyricsExtractor {

    private static final String TAG = "LyricsExtractor";

    /**
     * 从Media3的Metadata中提取歌词文本
     *
     * @param metadata Media3轨道的元数据
     * @return 歌词文本，如果没有找到则返回null
     */
    @OptIn(markerClass = UnstableApi.class)
    @Nullable
    public static String extractLyricsFromMetadata(@NonNull Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);
            String lyricsText = extractLyricsFromEntry(entry);
            if (lyricsText != null) {
                return lyricsText;
            }
        }
        return null;
    }

    /**
     * 从单个元数据条目中提取歌词
     */
    @OptIn(markerClass = UnstableApi.class)
    @Nullable
    private static String extractLyricsFromEntry(@NonNull Metadata.Entry entry) {
        // FLAC/OGG格式 - Vorbis注释
        if (entry instanceof VorbisComment) {
            VorbisComment vorbisComment = (VorbisComment) entry;
            if ("LYRICS".equals(vorbisComment.key)) {
                return vorbisComment.value;
            }
        }

        // MP3格式 - ID3标签，支持USLT和SYLT帧
        else if (entry instanceof BinaryFrame) {
            BinaryFrame binaryFrame = (BinaryFrame) entry;
            if ("USLT".equals(binaryFrame.id) || "SYLT".equals(binaryFrame.id)) {
                return UsltFrameDecoder.decode(new ParsableByteArray(binaryFrame.data));
            }
        }

        // M4A格式 - 文本信息帧
        else if (entry instanceof TextInformationFrame) {
            TextInformationFrame textFrame = (TextInformationFrame) entry;
            if ("USLT".equals(textFrame.id) || "SYLT".equals(textFrame.id)) {
                return String.join("\n", textFrame.values);
            }
        }

        return null;
    }

    /**
     * ID3v2 USLT帧解码器
     * 基于MIT许可的代码改写：https://github.com/yoheimuta/ExoPlayerMusic
     * 参考：http://id3.org/id3v2.4.0-frames
     */
    @OptIn(markerClass = UnstableApi.class)
    private static class UsltFrameDecoder {

        private static final int ID3_TEXT_ENCODING_ISO_8859_1 = 0;
        private static final int ID3_TEXT_ENCODING_UTF_16 = 1;
        private static final int ID3_TEXT_ENCODING_UTF_16BE = 2;
        private static final int ID3_TEXT_ENCODING_UTF_8 = 3;

        @Nullable
        public static String decode(@NonNull ParsableByteArray id3Data) {
            if (id3Data.limit() < 4) {
                // 帧格式错误
                return null;
            }

            int encoding = id3Data.readUnsignedByte();
            Charset charset = getCharsetName(encoding);

            // 读取语言代码（3字节）
            byte[] lang = new byte[3];
            id3Data.readBytes(lang, 0, 3);

            // 读取剩余数据
            byte[] rest = new byte[id3Data.limit() - 4];
            id3Data.readBytes(rest, 0, id3Data.limit() - 4);

            // 找到描述符结束位置
            int descriptionEndIndex = indexOfEos(rest, 0, encoding);
            int textStartIndex = descriptionEndIndex + delimiterLength(encoding);
            int textEndIndex = indexOfEos(rest, textStartIndex, encoding);

            return decodeStringIfValid(rest, textStartIndex, textEndIndex, charset);
        }

        @NonNull
        private static Charset getCharsetName(int encodingByte) {
            switch (encodingByte) {
                case ID3_TEXT_ENCODING_UTF_16:
                    return StandardCharsets.UTF_16;
                case ID3_TEXT_ENCODING_UTF_16BE:
                    return StandardCharsets.UTF_16BE;
                case ID3_TEXT_ENCODING_UTF_8:
                    return StandardCharsets.UTF_8;
                case ID3_TEXT_ENCODING_ISO_8859_1:
                default:
                    return StandardCharsets.ISO_8859_1;
            }
        }

        private static int indexOfEos(byte[] data, int fromIndex, int encoding) {
            int terminationPos = indexOfZeroByte(data, fromIndex);

            // 对于单字节编码，直接返回
            if (encoding == ID3_TEXT_ENCODING_ISO_8859_1 || encoding == ID3_TEXT_ENCODING_UTF_8) {
                return terminationPos;
            }

            // 对于双字节编码，确保偶数索引并查找第二个零字节
            while (terminationPos < data.length - 1) {
                if (terminationPos % 2 == 0 && data[terminationPos + 1] == 0) {
                    return terminationPos;
                }
                terminationPos = indexOfZeroByte(data, terminationPos + 1);
            }

            return data.length;
        }

        private static int indexOfZeroByte(byte[] data, int fromIndex) {
            for (int i = fromIndex; i < data.length; i++) {
                if (data[i] == 0) {
                    return i;
                }
            }
            return data.length;
        }

        private static int delimiterLength(int encodingByte) {
            return (encodingByte == ID3_TEXT_ENCODING_ISO_8859_1 ||
                encodingByte == ID3_TEXT_ENCODING_UTF_8) ? 1 : 2;
        }

        @NonNull
        private static String decodeStringIfValid(byte[] data, int from, int to, Charset charset) {
            if (to <= from || to > data.length) {
                return "";
            }
            return new String(data, from, to - from, charset);
        }
    }

    /**
     * 歌词提取配置选项
     */
    public static class LyricsOptions {
        private final boolean trimWhitespace;
        private final String errorText;

        public LyricsOptions(boolean trimWhitespace, @Nullable String errorText) {
            this.trimWhitespace = trimWhitespace;
            this.errorText = errorText;
        }

        public boolean shouldTrimWhitespace() {
            return trimWhitespace;
        }

        @Nullable
        public String getErrorText() {
            return errorText;
        }
    }

    /**
     * 歌词提取结果
     */
    public static class LyricsResult {
        private final String lyricsText;
        private final LyricsFormat format;

        public LyricsResult(@NonNull String lyricsText, @NonNull LyricsFormat format) {
            this.lyricsText = lyricsText;
            this.format = format;
        }

        @NonNull
        public String getLyricsText() {
            return lyricsText;
        }

        @NonNull
        public LyricsFormat getFormat() {
            return format;
        }
    }

    /**
     * 支持的歌词格式
     */
    public enum LyricsFormat {
        UNKNOWN,    // 未知格式，需要进一步解析判断
        LRC,        // .lrc格式
        TTML,       // .ttml格式（Apple Music）
        SRT,        // .srt字幕格式
        PLAIN_TEXT  // 纯文本
    }

    /**
     * 带配置选项的歌词提取方法
     */
    @OptIn(markerClass = UnstableApi.class)
    @Nullable
    public static LyricsResult extractLyricsFromMetadata(@NonNull Metadata metadata,
        @NonNull LyricsOptions options) {
        try {
            String lyricsText = extractLyricsFromMetadata(metadata);
            if (lyricsText == null) {
                return null;
            }

            // 如果需要，去除首尾空白
            if (options.shouldTrimWhitespace()) {
                lyricsText = lyricsText.trim();
            }

            // 检测歌词格式
            LyricsFormat format = detectLyricsFormat(lyricsText);

            return new LyricsResult(lyricsText, format);

        } catch (Exception e) {
            // 如果设置了错误文本，返回错误结果
            if (options.getErrorText() != null) {
                return new LyricsResult(options.getErrorText(), LyricsFormat.PLAIN_TEXT);
            }
            throw e;
        }
    }

    /**
     * 检测歌词格式
     */
    @NonNull
    private static LyricsFormat detectLyricsFormat(@NonNull String lyricsText) {
        String trimmedText = lyricsText.trim();

        // 检测TTML格式（XML格式，包含tt标签）
        if (trimmedText.startsWith("<?xml") || trimmedText.contains("<tt ") || trimmedText.contains("<tt>")) {
            return LyricsFormat.TTML;
        }

        // 检测SRT格式（以数字开头，后跟时间戳）
        if (trimmedText.matches("^1\\s*\\n.*")) {
            return LyricsFormat.SRT;
        }

        // 检测LRC格式（包含时间标记 [mm:ss.xx]）
        if (trimmedText.contains("[") && trimmedText.matches(".*\\[\\d{2}:\\d{2}[.:]\\d+\\].*")) {
            return LyricsFormat.LRC;
        }

        // 默认为纯文本
        return LyricsFormat.PLAIN_TEXT;
    }
}