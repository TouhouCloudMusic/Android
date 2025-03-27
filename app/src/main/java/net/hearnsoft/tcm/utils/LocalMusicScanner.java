package net.hearnsoft.tcm.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

import net.hearnsoft.tcm.domain.model.song.SongSortingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地歌曲扫描工具类
 */
public class LocalMusicScanner {

    /**
     * 扫描设备中的音乐文件并转换为MediaItems
     *
     * @param context 应用上下文
     * @return 音乐MediaItems列表
     */
    public static List<MediaItem> scanDeviceMusic(Context context) {
        List<MediaItem> musicItems = new ArrayList<>();

        // 定义媒体存储投影
        String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        };

        // 过滤只包含音乐文件
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        // 按标题排序
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String album = cursor.getString(albumColumn);
                    long albumId = cursor.getLong(albumIdColumn);
                    long duration = cursor.getLong(durationColumn);
                    String path = cursor.getString(pathColumn);

                    // 创建专辑封面URI
                    Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);

                    // 创建媒体项URI
                    Uri contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

                    // 创建MediaItem
                    MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(contentUri)
                        .setMediaMetadata(new MediaMetadata.Builder()
                            .setTitle(title)
                            .setArtist(artist)
                            .setAlbumTitle(album)
                            .setArtworkUri(albumArtUri)
                            .build())
                        .build();

                    musicItems.add(mediaItem);
                    Logs.d("MusicScanner", "歌曲已添加: " + title);
                }
            }
        } catch (Exception e) {
            Logs.e("MusicScanner", "扫描音乐时出错: " + e.getMessage());
        }

        return musicItems;
    }

    /**
     * 对音乐列表进行排序（使用已有的LocalMusicSorter）
     *
     * @param items 要排序的音乐列表
     * @param rule  排序规则
     * @return 排序后的列表
     */
    public static List<MediaItem> sortMusicList(List<MediaItem> items, SongSortingRule rule) {
        return LocalMusicSorter.sortMusicList(items, rule);
    }

}
