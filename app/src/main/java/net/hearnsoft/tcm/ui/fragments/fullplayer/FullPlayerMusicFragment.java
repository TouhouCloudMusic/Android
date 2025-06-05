package net.hearnsoft.tcm.ui.fragments.fullplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;

import com.bumptech.glide.Glide;
import com.flyjingfish.openimagelib.OpenImage;
import com.flyjingfish.openimagelib.beans.OpenImageUrl;
import com.flyjingfish.openimagelib.enums.MediaType;
import com.google.android.material.chip.Chip;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentFullPlayerMusicBinding;
import net.hearnsoft.tcm.databinding.FullPlayerMusicInfoBinding;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;

@UnstableApi
public class FullPlayerMusicFragment extends Fragment {
    private FragmentFullPlayerMusicBinding binding;
    private FullPlayerMusicInfoBinding bindingMusicInfo;

    private PlaybackViewModel viewModel;
    private ImageView albumArtImageView;
    private TextView titleTextView;
    private TextView artistTextView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFullPlayerMusicBinding.inflate(inflater, container, false);
        bindingMusicInfo = binding.fullPlayerMusicInfo;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图引用
        albumArtImageView = binding.albumArtImageView;
        titleTextView = bindingMusicInfo.fullPlayerMusicTitle;
        artistTextView = bindingMusicInfo.fullPlayerMusicArtist;

        bindingMusicInfo.fullPlayerMusicTagsAdd.setOnClickListener(v -> {
            Chip chip = new Chip(requireContext());
            chip.setText("baka");
            bindingMusicInfo.fullPlayerMusicTagsGroup.addView(chip);
        });

        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        // 观察媒体项变化
        viewModel.getCurrentMediaItem().observe(getViewLifecycleOwner(), this::updateMediaInfo);
    }

    private void updateMediaInfo(MediaItem mediaItem) {
        if (mediaItem == null) return;

        // 更新标题
        if (mediaItem.mediaMetadata.title != null) {
            titleTextView.setText(mediaItem.mediaMetadata.title);
        } else {
            titleTextView.setText(R.string.unknown);
        }

        // 更新艺术家
        if (mediaItem.mediaMetadata.artist != null) {
            artistTextView.setText(mediaItem.mediaMetadata.artist);
        } else {
            artistTextView.setText(R.string.unknown);
        }

        // 更新专辑封面
        if (mediaItem.mediaMetadata.artworkUri != null) {
            Glide.with(this)
                    .load(mediaItem.mediaMetadata.artworkUri)
                    .placeholder(R.drawable.ic_nav_music)
                    .into(albumArtImageView);
        } else {
            // 设置默认图片
            albumArtImageView.setImageResource(R.drawable.ic_nav_music);
        }

        // 设置封面点击事件
        albumArtImageView.setOnClickListener(v -> {
            if (mediaItem.mediaMetadata.artworkUri != null) {
                OpenImage.with(this)
                    .setClickImageView(albumArtImageView)
                    .setImageUrl(String.valueOf(mediaItem.mediaMetadata.artworkUri), MediaType.IMAGE)
                    .show();
            }
        });
    }
}
