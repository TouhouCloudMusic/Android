package net.hearnsoft.tcm.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地歌曲扫描工具类
 */
public class LocalMusicScanner {

    /**
     * 扫描设备中的音乐文件并返回媒体项列表
     * <p>该方法会遍历设备存储中的音乐文件，解析媒体元数据，并生成对应的MediaItem对象集合</p>
     *
     * @param context 应用程序上下文对象，用于访问设备存储或媒体库服务
     * @return List MediaItem 对象，包含所有扫描到的媒体项的列表，每个元素代表一个有效的音乐文件
     */
    public static List<MediaItem> scanDeviceMusic(Context context) {
        List<MediaItem> musicItems = new ArrayList<>();

        // Define media store projection
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        // Filter to include only music files
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        // Sort by title
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

                    // Create album art URI
                    Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);

                    // Create media item URI
                    Uri contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

                    // Create MediaItem
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
                    Logs.d("MusicScanner", "Song added: " + title);
                }
            }
        } catch (Exception e) {
            Logs.e("MusicScanner", "Error scanning music: " + e.getMessage());
        }

        return musicItems;
    }

}
