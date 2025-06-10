package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentMainBinding;
import net.hearnsoft.tcm.ui.activity.MainActivity;
import net.hearnsoft.tcm.ui.interfaces.OnNowPlayingClickListener;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.utils.ViewModelUtils;

@UnstableApi
public class MainFragment extends Fragment {
    private static final int DEFAULT_NAV_ITEM = R.id.fragment_explore;

    private FragmentMainBinding binding;
    private OnNowPlayingClickListener listener;
    private PlaybackViewModel viewModel;
    private UserViewModel userViewModel;
    private NavController navController;
    private int currentNavSelected = DEFAULT_NAV_ITEM;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            listener = ((MainActivity) context).getNowPlayingClickListener();
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContainer, (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());

        initNavBar();

        // Initially hide now playing bar
        binding.nowPlayingBar.setVisibility(View.GONE);

        // 设置Toolbar点击事件
        binding.toolbarMenu.setOnClickListener(v -> {
            DrawerLayout drawerLayout = binding.rootDrawerContainer;
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

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
        viewModel = ViewModelUtils.getViewModel(requireActivity(), PlaybackViewModel.class);
        userViewModel = ViewModelUtils.getViewModel(requireActivity(), UserViewModel.class);
        if (userViewModel.isLoggedIn()) {
            // 已登录时获取用户信息
            userViewModel.loadCurrentUserProfile();
        }

        // 设置ViewModel观察者
        observeViewModel();
    }

    private void initNavBar() {
        navController = Navigation.findNavController(requireActivity(), R.id.app_main_view);
        NavigationUI.setupWithNavController(binding.navBar, navController);

        // Set listener to track selection changes
        binding.navBar.setOnItemSelectedListener(item -> {
            currentNavSelected = item.getItemId();
            // Let NavigationUI handle the navigation
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        navController.navigate(currentNavSelected);
        binding.navBar.setSelectedItemId(currentNavSelected);
    }

    private void observeViewModel() {
        viewModel.getCurrentMediaItem().observe(getViewLifecycleOwner(), this::updateNowPlayingBar);

        viewModel.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            binding.nowPlayingBar.updateIsPlaying(isPlaying);
        });
        
        viewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
            // 检查position是否有效
            if (position < 0) {
                position = 0L;
            }
            
            Long duration = viewModel.getDuration().getValue();
            // 只有当duration不为null且大于0时才更新进度
            if (duration != null && duration > 0) {
                binding.nowPlayingBar.updateDurationCurrentPositionMs(duration, position);
            }
        });

        // 观察duration变化，确保当duration从无效值变为有效值时能及时更新
        viewModel.getDuration().observe(getViewLifecycleOwner(), duration -> {
            if (duration != null && duration > 0) {
                Long position = viewModel.getCurrentPosition().getValue();
                if (position != null) {
                    // 确保position为有效值
                    if (position < 0) {
                        position = 0L;
                    }
                    binding.nowPlayingBar.updateDurationCurrentPositionMs(duration, position);
                }
            }
        });

        //在启动时就进行媒体库扫描
        // 检查是否已加载音乐
        if (viewModel.getPlaylist().getValue() == null) {
            // 让ViewModel处理扫描
            viewModel.scanAndLoadMusic(requireContext());
        }
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

    @Override
    public void onResume() {
        // 只在当前选中项变更时才重新导航
        if (binding.navBar.getSelectedItemId() != currentNavSelected) {
            navController.navigate(currentNavSelected);
        }
        // 确保控制器已连接
        if (viewModel != null) {
            viewModel.ensureControllerConnected();
            viewModel.updatePosition();

            // 确保播放状态正确同步
            Boolean isPlaying = viewModel.getIsPlaying().getValue();
            if (isPlaying != null && binding.nowPlayingBar.getVisibility() == View.VISIBLE) {
                binding.nowPlayingBar.forceUpdatePlayPauseIcon(isPlaying);
            }
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // Always stop tracking when the fragment pauses
        currentNavSelected = binding.navBar.getSelectedItemId();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
