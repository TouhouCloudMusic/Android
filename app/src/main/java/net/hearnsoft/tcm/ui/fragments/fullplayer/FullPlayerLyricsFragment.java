package net.hearnsoft.tcm.ui.fragments.fullplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;

import com.dirror.lyricviewx.OnPlayClickListener;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentFullPlayerLyricsBinding;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;

@UnstableApi
public class FullPlayerLyricsFragment extends Fragment {
    private FragmentFullPlayerLyricsBinding binding;

    private PlaybackViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFullPlayerLyricsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        viewModel.getCurrentLyrics().observe(getViewLifecycleOwner(), lyrics -> {
            if (lyrics != null && !lyrics.isEmpty()) {
                binding.fullPlayerLyrics.loadLyric(lyrics, "");
            } else {
                binding.fullPlayerLyrics.loadLyric(null, null);
                binding.fullPlayerLyrics.setLabel("No Lyrics Available");
            }
        });

        viewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
            // 更新歌词滚动位置
            binding.fullPlayerLyrics.updateTime(position, true);
        });

        binding.fullPlayerLyrics.setDraggable(true, l -> {
            // 点击歌词时，跳转到对应的时间位置
            if (viewModel.getCurrentMediaItem() != null) {
                viewModel.seekTo(l);
                return true;
            }
            return false;
        });

        binding.fullPlayerLyrics.setCurrentColor(
            getResources().getColor(R.color.primary, getContext().getTheme())
        );
        binding.fullPlayerLyrics.setTimeTextColor(
            getResources().getColor(R.color.text_primary, getContext().getTheme())
        );
    }
}
