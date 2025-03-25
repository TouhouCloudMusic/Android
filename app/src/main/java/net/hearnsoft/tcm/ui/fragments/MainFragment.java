package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentMainBinding;
import net.hearnsoft.tcm.ui.activity.NewMainActivity;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.interfaces.OnNowPlayingClickListener;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;
import net.hearnsoft.tcm.utils.Logs;

@UnstableApi
public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private AppViewPagerAdapter adapter;
    private OnNowPlayingClickListener listener;
    private PlaybackViewModel viewModel;
    private int currentPagerPosition = 0;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            viewModel.updatePosition();
            progressHandler.postDelayed(this, 1000); // 每秒更新一次
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存ViewPager2的状态
        if (binding != null) {
            outState.putInt("viewpager_position", binding.mainView.getCurrentItem());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            currentPagerPosition = savedInstanceState.getInt("viewpager_position", 0);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NewMainActivity) {
            listener = ((NewMainActivity) context).getNowPlayingClickListener();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        adapter = AppViewPagerAdapter.create(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());

        initPager();
        initNavBar();
        binding.nowPlayingBar.setOnPlayingBarClickListener(v -> {
            listener.onNowPlayingClick();
            onDestroyView();
        });

        // Initially hide now playing bar
        binding.nowPlayingBar.setVisibility(View.GONE);

        // 设置播放控制
        binding.nowPlayingBar.setOnPlayingBarClickListener(v -> {
            if (listener != null) {
                listener.onNowPlayingClick();
            }
        });

        binding.nowPlayingBar.setOnPlayPauseClickListener(v -> {
            viewModel.togglePlayPause();
        });

        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);
        observeViewModel();

        // 启动进度更新
        startProgressTracking();
    }

    private void initPager() {
        binding.mainView.setAdapter(adapter);
        // 添加Fragments
        if (adapter.getItemCount() == 0) {
            adapter.addFragment(new ExploreFragment());
            adapter.addFragment(new RadioFragment());
            adapter.addFragment(new LibraryFragment());
            adapter.addFragment(new MusicFragment());
            adapter.addFragment(new AccountFragment());
        }
        // ViewPager2属性设置
        // 设置当前页面以及是否启用丝滑滚动
        binding.mainView.setCurrentItem(currentPagerPosition,true);
        // 设置是否启用用户切换手势
        binding.mainView.setUserInputEnabled(false);
        // 设置onPageChangeCallback
        binding.mainView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.navBar.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private void initNavBar() {
        binding.navBar.setOnItemSelectedListener(menuItem -> {
            int position = 0;
            if (menuItem.getItemId() == R.id.nav_explore) {
                position = 0;
            } else if (menuItem.getItemId() == R.id.nav_music_library) {
                position = 1;
            } else if (menuItem.getItemId() == R.id.nav_statistics) {
                position = 2;
            } else if (menuItem.getItemId() == R.id.nav_music) {
                position = 3;
            } else if (menuItem.getItemId() == R.id.nav_account) {
                position = 4;
            }
            binding.mainView.setCurrentItem(position, true);
            return true;
        });
    }

    private void observeViewModel() {
        viewModel.getCurrentMediaItem().observe(getViewLifecycleOwner(), this::updateNowPlayingBar);

        viewModel.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            binding.nowPlayingBar.updateIsPlaying(isPlaying);
            if (isPlaying) {
                startProgressTracking();
            } else {
                stopProgressTracking();
            }
        });

        viewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
            Long duration = viewModel.getDuration().getValue();
            if (duration != null) {
                binding.nowPlayingBar.updateDurationCurrentPositionMs(duration, position);
            }
        });
    }

    private void updateNowPlayingBar(MediaItem mediaItem) {
        if (mediaItem == null) {
            binding.nowPlayingBar.setVisibility(View.GONE);
            return;
        }

        binding.nowPlayingBar.updateMediaItem(mediaItem);
        binding.nowPlayingBar.updateMediaMetadata(mediaItem.mediaMetadata);

        // 更新专辑封面
        if (mediaItem.mediaMetadata.artworkUri != null) {
            binding.nowPlayingBar.updateCoverImage(mediaItem.mediaMetadata.artworkUri.toString());
        }

        // 显示播放栏
        binding.nowPlayingBar.setVisibility(View.VISIBLE);
    }

    private void startProgressTracking() {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);
    }

    private void stopProgressTracking() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    @Override
    public void onDestroyView() {
        stopProgressTracking();
        super.onDestroyView();
    }
}
