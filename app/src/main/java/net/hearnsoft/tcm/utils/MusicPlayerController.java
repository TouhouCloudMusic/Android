package net.hearnsoft.tcm.utils;

import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import net.hearnsoft.tcm.services.MusicPlaybackService;

import java.util.List;

import lombok.Getter;

/**
 * 音乐播放控制器，用于连接和控制MusicService
 */
@UnstableApi
public class MusicPlayerController {
    private static MusicPlayerController instance;
    @Getter
    private MediaController mediaController;
    private ListenableFuture<MediaController> controllerFuture;
    private Context context;
    private Player.Listener playerListener;
    private List<MediaItem> playlist;

    private MusicPlayerController(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized MusicPlayerController getInstance(Context context) {
        if (instance == null) {
            instance = new MusicPlayerController(context);
        }
        return instance;
    }

    public void connect(@NonNull Player.Listener listener) {
        this.playerListener = listener;

        // Connect to service
        SessionToken sessionToken = new SessionToken(
                context,
                new ComponentName(context, MusicPlaybackService.class)
        );

        controllerFuture = new MediaController.Builder(context, sessionToken).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                mediaController.addListener(playerListener);

                // Immediately notify listener of current media item
                if (playerListener != null) {
                    MediaItem currentItem = mediaController.getCurrentMediaItem();
                    playerListener.onMediaItemTransition(currentItem, Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED);

                    // Update player state
                    int state = mediaController.getPlaybackState();
                    playerListener.onPlaybackStateChanged(state);
                    playerListener.onIsPlayingChanged(mediaController.isPlaying());
                }

                // Connection callback
                if (listener instanceof ConnectionCallback) {
                    ((ConnectionCallback) listener).onConnected(mediaController);
                }
            } catch (Exception e) {
                Logs.e("MusicPlayerController", "Error connecting to service: " + e.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }

    public void release() {
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
            controllerFuture = null;
        }
        mediaController = null;
        playerListener = null;
    }

    public void playMusic(List<MediaItem> playlist, int startIndex) {
        if (playlist != null) {
            this.playlist = playlist;
            mediaController.setMediaItems(playlist);
        }
        if (mediaController != null) {
            mediaController.seekTo(startIndex, 0);
            mediaController.prepare();
            mediaController.play();
        }
    }

    public void togglePlayPause() {
        if (mediaController != null) {
            if (mediaController.isPlaying()) {
                mediaController.pause();
            } else {
                mediaController.play();
            }
        }
    }

    public void togglePlayPause(boolean isPlaying) {
        if (mediaController != null) {
            if (isPlaying) {
                mediaController.play();
            } else {
                mediaController.pause();
            }
        }
    }

    public interface ConnectionCallback {
        void onConnected(MediaController controller);
    }
}