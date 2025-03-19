package net.hearnsoft.tcm.ui.widgets;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import net.hearnsoft.tcm.R;

public class NowPlayingBar extends FrameLayout {

    private ImageView coverImageView;
    private TextView titleTextView;
    private TextView artistTextView;
    private CircularProgressIndicator circularProgressIndicator;
    private MaterialCardView nowPlayingBarContainer;
    private MaterialButton playPauseButton;

    private boolean isBottomNavigationBar = false;

    public NowPlayingBar(@NonNull Context context) {
        this(context, null);
    }

    public NowPlayingBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.now_playing_bar, this);
        nowPlayingBarContainer = findViewById(R.id.nowPlayingBarContainer);
        circularProgressIndicator = findViewById(R.id.circularProgressIndicator);
        titleTextView = findViewById(R.id.titleTextView);
        artistTextView = findViewById(R.id.artistNameTextView);
        coverImageView = findViewById(R.id.coverImageView);
        playPauseButton = findViewById(R.id.playPauseMaterialButton);

        circularProgressIndicator.setMin(0);
    }

    public void setOnPlayingBarClickListener(OnClickListener onClickListener) {
        nowPlayingBarContainer.setOnClickListener(onClickListener);
    }

    public void setOnPlayPauseClickListener(OnClickListener onClickListener) {
        playPauseButton.setOnClickListener(onClickListener);
    }

    public void updateCoverImage(String coverImageUrl) {
        if (coverImageUrl == null) {
            return;
        }
        Glide.with(this)
                .load(coverImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(coverImageView);
    }

    public void updateIsPlaying(boolean isPlaying) {
        playPauseButton.setIconResource(
                isPlaying ? R.drawable.avd_play_to_pause : R.drawable.avd_pause_to_play);
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) playPauseButton.getIcon();
        animatedVectorDrawable.start();
    }

    public void updateDurationCurrentPositionMs(long durationMs, long currentPositionMs) {
        int currentPositionSecs = (int) (currentPositionMs / 1000);
        int durationSecs = (int) durationMs / 1000;
        if (durationSecs == 0) {
            durationSecs = 1;
        }

        circularProgressIndicator.setMax(durationSecs);
        circularProgressIndicator.setProgressCompat(currentPositionSecs, true);
    }
}
