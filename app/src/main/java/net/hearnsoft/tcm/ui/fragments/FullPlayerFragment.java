package net.hearnsoft.tcm.ui.fragments;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FullPlayerBinding;
import net.hearnsoft.tcm.ui.activity.MainActivity;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.fragments.fullplayer.FullPlayerInfoFragment;
import net.hearnsoft.tcm.ui.fragments.fullplayer.FullPlayerMusicFragment;
import net.hearnsoft.tcm.ui.fragments.fullplayer.FullPlayerLyricsFragment;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;
import net.hearnsoft.tcm.ui.widgets.CurrentPlaylistBottomSheetDialog;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.SystemMediaDialogUtils;

import java.util.Locale;

@UnstableApi
public class FullPlayerFragment extends Fragment {
    private FullPlayerBinding binding;
    private static AppViewPagerAdapter adapter;
    private static final int[] fullPlayerTabs = {
            R.string.full_player_tabs_lyrics,
            R.string.full_player_tabs_music,
            R.string.full_player_tabs_info
    };

    private PlaybackViewModel viewModel;
    // 控制UI元素
    private FloatingActionButton playPauseButton;
    private MaterialButton prevButton;
    private MaterialButton nextButton;
    private MaterialButton playlistButton;
    private MaterialButton repeatButton;
    private Slider timelineSlider;
    private TextView currentTimeTextView;
    private TextView durationTextView;

    // 状态变量
    private boolean userIsSeeking = false;
    private boolean previousPlayingState = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FullPlayerBinding.inflate(inflater, container, false);
        adapter = AppViewPagerAdapter.create(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBar.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());

        // 获取ViewModel并观察数据
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        // 初始化标签页和ViewPager
        initFullPlayerTabs();

        initFullPlayerViewPager();
        binding.fullPlayerToolbarTabsContainer.getTabAt(1).select();

        // 初始化返回按钮
        binding.fullPlayerToolbar.setNavigationOnClickListener(v -> {
            try {
                // Use the activity's helper method for safe navigation
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateUpSafely();
                } else {
                    // Fallback if not in NewMainActivity
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                }
            } catch (Exception e) {
                Logs.e("FullPlayerFragment", "Navigation error: " + e.getMessage());
                // Fallback to standard back press
                requireActivity().onBackPressed();
            }
        });

        // 初始化播放控制按钮
        setupControlButtons();

        // 设置Slider UI
        setupTimelineSlider();

        // 监控播放状态和进度
        observeViewModel();
    }

    private void initFullPlayerTabs() {
        if (binding.fullPlayerToolbarTabsContainer.getTabCount() != 0) {
            return;
        }
        for (int fullPlayerTab : fullPlayerTabs) {
            TabLayout.Tab tab = binding.fullPlayerToolbarTabsContainer.newTab();
            tab.setText(fullPlayerTab);
            binding.fullPlayerToolbarTabsContainer.addTab(tab);
        }
        binding.fullPlayerToolbarTabsContainer.setTabMode(TabLayout.MODE_FIXED);
        binding.fullPlayerToolbarTabsContainer.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.fullPlayerPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.fullPlayerToolbarTabsContainer.getTabAt(1).select();
    }

    private void initFullPlayerViewPager() {
        binding.fullPlayerPager.setAdapter(adapter);
        if (adapter.getItemCount() == 0) {
            adapter.addFragment(new FullPlayerLyricsFragment());
            adapter.addFragment(new FullPlayerMusicFragment());
            adapter.addFragment(new FullPlayerInfoFragment());
        }
        binding.fullPlayerPager.setUserInputEnabled(true);
        binding.fullPlayerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.fullPlayerToolbarTabsContainer.getTabAt(position).select();
            }
        });
        binding.fullPlayerPager.setCurrentItem(1, false);
    }

    private void setupControlButtons() {
        playPauseButton = binding.fullPlayerControlsAction.fullPlayerControlsPlayPause;
        prevButton = binding.fullPlayerControlsAction.fullPlayerControlsPrevious;
        nextButton = binding.fullPlayerControlsAction.fullPlayerControlsNext;
        playlistButton = binding.fullPlayerControlsAction.fullPlayerControlsPlaylist;
        repeatButton = binding.fullPlayerControlsAction.fullPlayerControlsPlaymode;

        // 设置播放/暂停按钮点击事件
        playPauseButton.setOnClickListener(v -> {
            viewModel.togglePlayPause();
        });

        // 设置上一曲按钮点击事件
        prevButton.setOnClickListener(v -> {
            viewModel.playPrevious();
        });

        // 设置下一曲按钮点击事件
        nextButton.setOnClickListener(v -> {
            viewModel.playNext();
        });

        // 设置播放列表按钮点击事件
        playlistButton.setOnClickListener(v -> {
            // 创建并显示BottomSheetDialog
            CurrentPlaylistBottomSheetDialog dialog = new CurrentPlaylistBottomSheetDialog();
            dialog.show(getParentFragmentManager(), "playlist_dialog");
        });

        repeatButton.setOnClickListener(v -> {
            // 切换播放器的重复模式
            viewModel.toggleRepeatMode();
        });

        // 设置Toolbar按钮
        // 绑定Toolbar中的cast按钮
        binding.fullPlayerToolbarMedia.setOnClickListener(v -> {
            // 这里可以添加投屏逻辑
            Logs.d("FullPlayerFragment", "Cast button clicked");
            SystemMediaDialogUtils.getInstance(requireActivity())
                    .showSystemMediaDialog();
        });
    }

    private void setupTimelineSlider() {
        timelineSlider = binding.fullPlayerControlsSlider.fullPlayerSlider;
        currentTimeTextView = binding.fullPlayerControlsSlider.currentTimestampTextView;
        durationTextView = binding.fullPlayerControlsSlider.durationTimestampTextView;

        // Initialize with default values
        currentTimeTextView.setText(formatTime(0));
        durationTextView.setText(formatTime(0));

        // Set up the slider listeners
        timelineSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                userIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                viewModel.seekTo((long) (slider.getValue() * 1000));
                userIsSeeking = false;
            }
        });

        // Update time display during slider movement
        timelineSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                // Update current time text view with the dragged position
                currentTimeTextView.setText(formatTime((long) (value * 1000)));
            }
        });

        // Set the slider's label formatter to show time instead of numeric value
        timelineSlider.setLabelFormatter(value -> formatTime((long) (value * 1000)));
    }

    private void observeViewModel() {
        // 观察当前媒体项变化
        viewModel.getCurrentMediaItem().observe(getViewLifecycleOwner(), this::updateMediaInfo);

        // 观察播放状态变化
        viewModel.getIsPlaying().observe(getViewLifecycleOwner(), this::updatePlayPauseButton);

        // 观察播放进度
        viewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
            if (!userIsSeeking) {
                updatePlaybackPosition(position);
            }
        });

        // 观察总时长变化
        viewModel.getDuration().observe(getViewLifecycleOwner(), duration -> {
            // 更新总时长显示
            durationTextView.setText(formatTime(duration));
            // 如果用户不在拖动进度条，则同时更新当前进度
            if (!userIsSeeking) {
                Long position = viewModel.getCurrentPosition().getValue();
                if (position != null) {
                    updatePlaybackPosition(position);
                }
            }
        });

        // 观察播放器重复模式
        viewModel.getRepeatMode().observe(getViewLifecycleOwner(), repeatMode -> {
            updatePlayModeUI(repeatMode, viewModel.getIsShuffleMode().getValue());
        });

        // 观察随机播放状态
        viewModel.getIsShuffleMode().observe(getViewLifecycleOwner(), isShuffleMode -> {
            updatePlayModeUI(viewModel.getRepeatMode().getValue(), isShuffleMode);
        });
    }

    private void updatePlayModeUI(Integer repeatMode, Boolean isShuffleMode) {
        if (repeatMode == null) return;
        
        boolean shuffle = isShuffleMode != null && isShuffleMode;
        
        Logs.d("FullPlayerFragment", "Play mode changed - Repeat: " + repeatMode + ", Shuffle: " + shuffle);
        
        if (shuffle) {
            // 随机播放模式
            binding.fullPlayerControlsAction.fullPlayerControlsPlaymode
                .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_shuffle_one, requireContext().getTheme()));
        } else {
            switch (repeatMode) {
                case Player.REPEAT_MODE_ONE:
                    binding.fullPlayerControlsAction.fullPlayerControlsPlaymode
                        .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_once, requireContext().getTheme()));
                    break;
                case Player.REPEAT_MODE_ALL:
                    binding.fullPlayerControlsAction.fullPlayerControlsPlaymode
                        .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_cycle, requireContext().getTheme()));
                    break;
                case Player.REPEAT_MODE_OFF:
                default:
                    binding.fullPlayerControlsAction.fullPlayerControlsPlaymode
                        .setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_normal_play, requireContext().getTheme()));
                    break;
            }
        }
    }

    private void updateMediaInfo(MediaItem mediaItem) {
        if (mediaItem == null) return;

        // FullPlayerFragment主要负责控制，
        // 媒体信息的显示由FullPlayerMusicFragment负责
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        // 如果状态没有变化，不执行动画
        if (isPlaying == previousPlayingState) {
            return;
        }

        // 更新状态记录
        previousPlayingState = isPlaying;

        // 根据播放状态设置正确的图标
        playPauseButton.setImageResource(
                isPlaying ? R.drawable.avd_play_to_pause : R.drawable.avd_pause_to_play);

        // 启动动画
        AnimatedVectorDrawable animatedVectorDrawable =
                (AnimatedVectorDrawable) playPauseButton.getDrawable();
        if (animatedVectorDrawable != null) {
            animatedVectorDrawable.start();
        }
    }

    private void updatePlaybackPosition(long position) {
        // 检查position是否小于0
        if (position < 0) {
            Logs.e("FullPlayerFragment", "Invalid negative position: " + position);
            position = 0;
        }

        // 更新时间文本
        currentTimeTextView.setText(formatTime(position));

        // 获取总时长
        Long duration = viewModel.getDuration().getValue();
        if (duration != null && duration > 0) {
            // 更新进度条位置 (注意转换为秒，因为Slider使用的单位是秒)
            if (position <= duration) {
                try {
                    float positionValue = position / 1000f;
                    float durationValue = duration / 1000f;

                    // 如有需要，设置Slider控件范围
                    if (timelineSlider.getValueTo() != durationValue) {
                        timelineSlider.setValueTo(durationValue);
                    }

                    // 将当前位置设置在有效范围内
                    timelineSlider.setValue(Math.min(positionValue, durationValue));
                } catch (Exception e) {
                    Logs.e("FullPlayerFragment", "Error updating slider: " + e.getMessage());
                }
            }
        }
    }

    private String formatTime(long timeMs) {
        // 确保timeMs非负数
        long safeTimeMs = Math.max(0, timeMs);

        // 通过将其上限设定为最大合理值（10 小时）来处理溢出情况
        if (safeTimeMs > 36000000) {
            safeTimeMs = 36000000; // 10 小时的毫秒数表现
        }

        long totalSeconds = safeTimeMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // 根据是否包含小时数返回不同的格式
        if (hours > 0) {
            return String.format(Locale.CHINA, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.CHINA, "%d:%02d", minutes, seconds);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 确保控制器已连接
        if (viewModel != null) {
            viewModel.ensureControllerConnected();
            viewModel.updatePosition();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
