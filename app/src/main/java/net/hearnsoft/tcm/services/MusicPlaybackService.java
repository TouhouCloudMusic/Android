package net.hearnsoft.tcm.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.ui.activity.MainActivity;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.LyricsExtractor;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@UnstableApi
public class MusicPlaybackService extends MediaLibraryService implements AnalyticsListener {

    private MediaLibrarySession mediaLibrarySession;
    private ExoPlayer player;
    private final List<MediaItem> playlist = new ArrayList<>();

    // 歌词相关
    @Getter
    private String currentLyrics = "";
    private LyricsExtractor.LyricsFormat lyricsFormat = LyricsExtractor.LyricsFormat.UNKNOWN;
    // 添加歌词回调接口
    public interface LyricsUpdateListener {
        void onLyricsUpdated(String lyrics, LyricsExtractor.LyricsFormat format);
    }

    // 提供静态方法供ViewModel注册监听器
    @Setter
    private static LyricsUpdateListener lyricsUpdateListener;

    @Override
    public void onCreate() {
        super.onCreate();
        initializePlayer();
        initializeSession();

        // Add listener to track playback state
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                // Optional: log or handle transitions
                if (mediaItem != null) {
                    Logs.d("MusicPlaybackService", "Now playing: " + mediaItem.mediaMetadata.title);
                }
            }
        });

        player.addAnalyticsListener(this);
    }

    private void initializePlayer() {
        // 创建ExoPlayer实例
        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(C.USAGE_MEDIA)
                                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                                .build(),
                        true)
                .build();

        // 设置为循环播放
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    @UnstableApi
    private void initializeSession() {
        // 创建PendingIntent用于通知点击动作
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent sessionActivity = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 创建媒体库会话
        mediaLibrarySession = new MediaLibrarySession.Builder(this, player, new LibrarySessionCallback())
                .setSessionActivity(sessionActivity)
                .build();

        // 设置通知管理器
        DefaultMediaNotificationProvider provider = new DefaultMediaNotificationProvider.Builder(this)
                .build();
        provider.setSmallIcon(R.drawable.ic_launcher_foreground);
        setMediaNotificationProvider(provider);
    }

    @Override
    public void onTracksChanged(EventTime eventTime, Tracks tracks) {
        /*AnalyticsListener.super.onTracksChanged(eventTime, tracks);*/
        MediaItem currentItem = player.getCurrentMediaItem();
        if (currentItem == null) {
            return;
        }

        Logs.d("MusicPlaybackService", "Tracks changed, extracting lyrics for: " +
            currentItem.mediaMetadata.title);

        new Thread(() -> {
            String extractedLyrics = null;
            LyricsExtractor.LyricsFormat format = LyricsExtractor.LyricsFormat.UNKNOWN;

            // 遍历所有轨道组来查找歌词
            for (Tracks.Group group : tracks.getGroups()) {
                for (int i = 0; i < group.length; i++) {
                    if (!group.isTrackSelected(i)) continue;

                    Format trackFormat = group.getTrackFormat(i);
                    if (trackFormat.metadata != null) {
                        // 尝试从元数据中提取歌词
                        LyricsExtractor.LyricsOptions options = new LyricsExtractor.LyricsOptions(
                            true,
                            "Unable to extract lyrics"
                        );

                        LyricsExtractor.LyricsResult result =
                            LyricsExtractor.extractLyricsFromMetadata(trackFormat.metadata, options);

                        if (result != null) {
                            extractedLyrics = result.getLyricsText();
                            format = result.getFormat();
                            Logs.d("MusicPlaybackService",
                                "Lyrics extracted successfully, format: " + format);
                            break;
                        }
                    }
                }
                // 找到歌词后退出循环
                if (extractedLyrics != null) break;
            }

            // 更新歌词状态
            currentLyrics = extractedLyrics;
            lyricsFormat = format;

            if (lyricsUpdateListener != null) {
                new Handler(getMainLooper()).post(() -> {
                    lyricsUpdateListener.onLyricsUpdated(currentLyrics, lyricsFormat);
                });
            }

            if (extractedLyrics != null) {
                Logs.d("MusicPlaybackService", "Lyrics found: " +
                    extractedLyrics.substring(0, Math.min(100, extractedLyrics.length())) + "...");
            } else {
                Logs.d("MusicPlaybackService", "No lyrics found for current track");
            }
        }).start();
    }

    @Nullable
    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }

    @Override
    public void onDestroy() {
        // 释放播放器和会话资源
        if (mediaLibrarySession != null) {
            mediaLibrarySession.release();
            mediaLibrarySession = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        lyricsUpdateListener = null; // 清除歌词更新监听器
        super.onDestroy();
    }

    /**
     * 添加单个歌曲到播放列表
     */
    public void addSongToPlaylist(MediaItem item) {
        playlist.add(item);
        player.addMediaItem(item);
    }

    /**
     * 设置播放列表并开始播放
     */
    public void setPlaylistAndPlay(List<MediaItem> items, int startIndex) {
        playlist.clear();
        playlist.addAll(items);

        player.clearMediaItems();
        player.setMediaItems(items);
        player.seekTo(startIndex, 0);
        player.prepare();
        player.play();
    }

    private class LibrarySessionCallback implements MediaLibrarySession.Callback {
        @Override
        public MediaSession.ConnectionResult onConnect(MediaSession session, MediaSession.ControllerInfo controller) {
            return MediaLibrarySession.Callback.super.onConnect(session, controller);
        }

        @UnstableApi
        @Override
        public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onPlaybackResumption(MediaSession mediaSession, MediaSession.ControllerInfo controller) {
            return MediaLibrarySession.Callback.super.onPlaybackResumption(mediaSession, controller);
        }

        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(MediaLibrarySession session, MediaSession.ControllerInfo browser, @Nullable LibraryParams params) {
            return MediaLibrarySession.Callback.super.onGetLibraryRoot(session, browser, params);
        }

        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetItem(MediaLibrarySession session, MediaSession.ControllerInfo browser, String mediaId) {
            // 根据ID查找媒体项
            for (MediaItem item : playlist) {
                if (mediaId.equals(item.mediaId)) {
                    return Futures.immediateFuture(LibraryResult.ofItem(item, null));
                }
            }
            return MediaLibrarySession.Callback.super.onGetItem(session, browser, mediaId);
        }

        @Override
        public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(MediaLibrarySession session, MediaSession.ControllerInfo browser, String parentId, int page, int pageSize, @Nullable LibraryParams params) {
            // 实现媒体库浏览逻辑
            if (parentId.equals(MediaItemTree.ROOT)) {
                return Futures.immediateFuture(LibraryResult.ofItemList(
                        ImmutableList.copyOf(playlist),
                        /* params= */ null));
            }
            // 其他媒体树节点的处理...
            return Futures.immediateFuture(LibraryResult.ofItemList(
                    ImmutableList.of(),
                    /* params= */ null));
        }

        @Override
        public void onPostConnect(MediaSession session, MediaSession.ControllerInfo controller) {
            MediaLibrarySession.Callback.super.onPostConnect(session, controller);
        }
    }

    /**
     * 简单的媒体项树结构管理
     */
    private static class MediaItemTree {
        public static final String ROOT = "root";
    }
}
