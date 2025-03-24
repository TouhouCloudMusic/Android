package net.hearnsoft.tcm.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.misc.ViewKt;
import net.hearnsoft.tcm.utils.Logs;

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
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.now_playing_bar, this);
        nowPlayingBarContainer = findViewById(R.id.nowPlayingBarContainer);
        circularProgressIndicator = findViewById(R.id.circularProgressIndicator);
        titleTextView = findViewById(R.id.titleTextView);
        artistTextView = findViewById(R.id.artistNameTextView);
        coverImageView = findViewById(R.id.coverImageView);
        playPauseButton = findViewById(R.id.playPauseMaterialButton);

        if (attrs != null) {
            try (TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NowPlayingBar, 0, 0)) {
                isBottomNavigationBar = array.getBoolean(
                        R.styleable.NowPlayingBar_isBottomNavigationBar,
                        false
                );
                array.recycle();
            } catch (Exception e) {
                Logs.e("NowPlayingBar", "get TypedArray error, " + e.getMessage());
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets windowInsets = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            layoutParams.leftMargin = windowInsets.left;
            layoutParams.rightMargin = windowInsets.right;
            setLayoutParams(layoutParams);

            nowPlayingBarContainer.setContentPadding(
                    0,
                    0,
                    0,
                    isBottomNavigationBar ? windowInsets.bottom : 0
            );

            return insets;
        });

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
                .placeholder(R.mipmap.ic_launcher)
                .into(coverImageView);
    }

    public void updateIsPlaying(boolean isPlaying) {
        playPauseButton.setIconResource(
                isPlaying ? R.drawable.avd_play_to_pause : R.drawable.avd_pause_to_play);
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) playPauseButton.getIcon();
        animatedVectorDrawable.start();
    }

    public void updateMediaItem(MediaItem mediaItem) {
        if (mediaItem != null) {
            ViewKt.slideUp(this);
        } else {
            ViewKt.slideDown(this);
        }
    }

    public void updateMediaMetadata(MediaMetadata metadata){
        String title = metadata.title == null ?
                getContext().getString(R.string.unknown) : (String) metadata.title;
        String artist = metadata.artist == null ?
                getContext().getString(R.string.unknown) : (String) metadata.artist;

        if (titleTextView.getText() != title) {
            titleTextView.setText(title);
        }

        if (artistTextView.getText() != artist) {
            artistTextView.setText(artist);
        }
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
