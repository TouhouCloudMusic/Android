package net.hearnsoft.tcm.services;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.ui.activity.NewMainActivity;
import net.hearnsoft.tcm.utils.Logs;

import java.util.ArrayList;
import java.util.List;

public class MusicPlaybackService extends MediaLibraryService {
    private MediaLibrarySession mediaLibrarySession;
    private ExoPlayer player;
    private final List<MediaItem> playlist = new ArrayList<>();

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

    @OptIn(markerClass = UnstableApi.class)
    private void initializeSession() {
        // 创建PendingIntent用于通知点击动作
        PendingIntent sessionActivity = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, NewMainActivity.class),
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
