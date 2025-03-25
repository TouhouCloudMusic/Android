package net.hearnsoft.tcm.ui.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;

import net.hearnsoft.tcm.utils.MusicPlayerController;

import java.util.List;

@UnstableApi
public class PlaybackViewModel extends AndroidViewModel {
    private MusicPlayerController playerController;
    private final MutableLiveData<MediaItem> currentMediaItem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<List<MediaItem>> playlist = new MutableLiveData<>();
    private final MutableLiveData<Long> currentPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> duration = new MutableLiveData<>(0L);

    public PlaybackViewModel(@NonNull Application application) {
        super(application);
        playerController = MusicPlayerController.getInstance(application);
        connectToService();
    }

    private void connectToService() {
        playerController.connect(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@NonNull MediaItem mediaItem, int reason) {
                currentMediaItem.postValue(mediaItem);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playerController.getMediaController() != null) {
                    duration.postValue(playerController.getMediaController().getDuration());
                }
            }

            @Override
            public void onIsPlayingChanged(boolean playing) {
                isPlaying.postValue(playing);
            }

            @Override
            public void onPlaylistMetadataChanged(MediaMetadata mediaMetadata) {
                Player.Listener.super.onPlaylistMetadataChanged(mediaMetadata);
                if (playerController.getMediaController() != null) {
                    updatePlaylist();
                }
            }

        });

        // 初始化状态
        if (playerController.getMediaController() != null) {
            MediaItem item = playerController.getMediaController().getCurrentMediaItem();
            if (item != null) {
                currentMediaItem.postValue(item);
            }
            isPlaying.postValue(playerController.getMediaController().isPlaying());
            duration.postValue(playerController.getMediaController().getDuration());
            updatePlaylist();
        }
    }

    private void updatePlaylist() {
        if (playerController.getMediaController() == null) return;

        int count = playerController.getMediaController().getMediaItemCount();
        if (count > 0) {
            // 获取当前播放列表
            List<MediaItem> items = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                items.add(playerController.getMediaController().getMediaItemAt(i));
            }
            playlist.postValue(items);
        }
    }

    public void updatePosition() {
        if (playerController.getMediaController() != null) {
            currentPosition.postValue(playerController.getMediaController().getCurrentPosition());
        }
    }

    public void playMusic(List<MediaItem> playlist, int startIndex) {
        if (playerController.getMediaController() != null) {
            playerController.playMusic(playlist, startIndex);
            this.playlist.postValue(playlist);
        }
    }

    public void togglePlayPause() {
        if (playerController.getMediaController() != null) {
            playerController.togglePlayPause();
        }
    }

    public void togglePlayPause(boolean isPlaying) {
        if (playerController.getMediaController() != null) {
            playerController.togglePlayPause(isPlaying);
        }
    }


    public void seekTo(long positionMs) {
        if (playerController.getMediaController() != null) {
            playerController.getMediaController().seekTo(positionMs);
            currentPosition.postValue(positionMs);
        }
    }

    public void playNext() {
        if (playerController.getMediaController() != null) {
            if (playerController.getMediaController().hasNextMediaItem()) {
                playerController.getMediaController().seekToNext();
            }
        }
    }

    public void playPrevious() {
        if (playerController.getMediaController() != null) {
            if (playerController.getMediaController().hasPreviousMediaItem()) {
                playerController.getMediaController().seekToPrevious();
            } else {
                // 如果没有上一首，就重头开始播放当前歌曲
                playerController.getMediaController().seekTo(0);
            }
        }
    }

    // 获取LiveData
    public LiveData<MediaItem> getCurrentMediaItem() {
        return currentMediaItem;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public LiveData<List<MediaItem>> getPlaylist() {
        return playlist;
    }

    public LiveData<Long> getCurrentPosition() {
        return currentPosition;
    }

    public LiveData<Long> getDuration() {
        return duration;
    }

    @Override
    protected void onCleared() {
        playerController.release();
        super.onCleared();
    }

}
