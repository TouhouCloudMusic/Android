package net.hearnsoft.tcm.ui.fragments.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentMusicListBinding;
import net.hearnsoft.tcm.domain.model.song.SongSortingRule;
import net.hearnsoft.tcm.domain.model.song.SongSortingStrategy;
import net.hearnsoft.tcm.ui.adapter.MusicItemAdapter;
import net.hearnsoft.tcm.ui.interfaces.OnMusicItemClickListener;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;
import net.hearnsoft.tcm.ui.utils.LinearTopSmoothScroller;
import net.hearnsoft.tcm.utils.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@UnstableApi
public class MusicListFragment extends Fragment implements OnMusicItemClickListener {
    private FragmentMusicListBinding binding;
    private MusicItemAdapter adapter;
    private List<MediaItem> musicList = new ArrayList<>();
    private PlaybackViewModel viewModel;
    private LinearTopSmoothScroller scroller;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Logs.d("MusicListFragment", "存储权限已授予，开始加载音乐");
                loadMusic();
            } else {
                Logs.w("MusicListFragment", "存储权限被拒绝");
                showNoPermissionUI();
            }
        });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MusicItemAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        // 创建LinearTopSmoothScroller
        scroller = new LinearTopSmoothScroller(requireContext(), true);

        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        //设置排序chips
        setSortingChips();
        //设置ViewModel观察者
        setViewModelObserver();

        binding.musicLocationButton.setOnClickListener(v -> {
            if (!musicList.isEmpty()) {
                binding.recyclerView.post(() -> {
                    int currentIndex = viewModel.getCurrentIndex().getValue() != null
                            ? viewModel.getCurrentIndex().getValue() : 0;
                    scroller.setTargetPosition(currentIndex);
                    binding.recyclerView.getLayoutManager().startSmoothScroll(scroller);
                });
            }
        });

        // 检查权限
        checkPermissionAndLoadMusic();
    }

    private void setViewModelObserver() {
        // 观察播放列表变化
        viewModel.getPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            if (playlist != null && !playlist.isEmpty()) {
                musicList = playlist;
                updateMusicListUI();

                if (binding.noElementsLinearLayout.getVisibility() == View.VISIBLE) {
                    binding.noElementsLinearLayout.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }

                // 当播放列表加载完成时，隐藏进度条
                binding.linearProgressIndicator.setVisibility(View.GONE);
            } else {
                showEmptyUI();
            }
        });

        // 观察扫描进度
        viewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
            binding.linearProgressIndicator.setVisibility(isScanning ? View.VISIBLE : View.GONE);
        });

        // 观察数据库加载状态
        viewModel.getIsLoadingFromDatabase().observe(getViewLifecycleOwner(), isLoadingFromDb -> {
            if (isLoadingFromDb) {
                binding.linearProgressIndicator.setVisibility(View.VISIBLE);
            }
        });

        // 观察加载状态消息
        viewModel.getLoadingStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.isEmpty()) {
                Logs.d("MusicListFragment", "加载状态: " + status);

                // 只有在确实需要加载时才显示加载UI
                Boolean isLoading = viewModel.getIsLoadingFromDatabase().getValue();
                Boolean isScanning = viewModel.getIsScanning().getValue();
                Boolean isSorting = viewModel.getIsSorting().getValue();

                if ((isLoading != null && isLoading) ||
                    (isScanning != null && isScanning) ||
                    (isSorting != null && isSorting)) {
                    showLoadingUI(status);
                }
            }
        });

        // 观察排序进度
        viewModel.getIsSorting().observe(getViewLifecycleOwner(), isSorting -> {
            binding.linearProgressIndicator.setVisibility(isSorting ? View.VISIBLE : View.GONE);
        });

        // 观察当前排序规则
        viewModel.getCurrentSortRule().observe(getViewLifecycleOwner(), rule -> {
            binding.musicSortingChip.setSortingRule(rule);
        });
    }

    private void setSortingChips() {
        SortedMap<SongSortingStrategy, Integer> sortingOptions = new TreeMap<>();
        sortingOptions.put(SongSortingStrategy.ArtistName, R.string.sort_by_artist_name);
        sortingOptions.put(SongSortingStrategy.CreatedAt, R.string.sort_by_release_date);
        sortingOptions.put(SongSortingStrategy.Title, R.string.sort_by_title);
        sortingOptions.put(SongSortingStrategy.PlayCount, R.string.sort_by_play_count);

        binding.musicSortingChip.setSortingStrategies(sortingOptions);

        // 设置默认排序规则
        SongSortingRule defaultRule = new SongSortingRule(SongSortingStrategy.Title, false);
        binding.musicSortingChip.setSortingRule(defaultRule);

        // 设置排序监听
        binding.musicSortingChip.setOnSortingRuleSelectedListener(rule -> {
            // 调用ViewModel进行排序
            viewModel.sortMusic(rule);
        });
    }

    private void checkPermissionAndLoadMusic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
                // 已有权限，直接加载
                Logs.d("MusicListFragment", "READ_MEDIA_AUDIO权限已授予");
                if (viewModel.getPlaylist().getValue() == null
                    || viewModel.getPlaylist().getValue().isEmpty()) {
                    loadMusic();
                }
            } else {
                // 请求权限
                Logs.d("MusicListFragment", "请求READ_MEDIA_AUDIO权限");
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
                // 已有权限，直接加载
                Logs.d("MusicListFragment", "READ_EXTERNAL_STORAGE权限已授予");
                if (viewModel.getPlaylist().getValue() == null
                    || viewModel.getPlaylist().getValue().isEmpty()) {
                    loadMusic();
                }
            } else {
                // 请求权限
                Logs.d("MusicListFragment", "请求READ_EXTERNAL_STORAGE权限");
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void loadMusic() {
        // 双重检查权限
        if (!viewModel.hasStoragePermission(requireContext())) {
            Logs.w("MusicListFragment", "加载音乐时发现权限不足");
            showNoPermissionUI();
            return;
        }

        binding.linearProgressIndicator.setVisibility(View.VISIBLE);
        binding.noElementsLinearLayout.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);

        // 检查是否已加载音乐
        if (viewModel.getPlaylist().getValue() != null
            && !viewModel.getPlaylist().getValue().isEmpty()) {
            // 使用现有列表
            Logs.d("MusicListFragment", "使用已存在的播放列表");
            musicList = viewModel.getPlaylist().getValue();
            updateMusicListUI();
        } else {
            // 使用ViewModel的权限检查方法
            Logs.d("MusicListFragment", "开始加载音乐库（权限已确认）");
            viewModel.loadMusicLibraryIfPermitted(requireContext());
        }
    }

    private void updateMusicListUI() {
        if (isAdded() && !isRemoving()) {
            binding.linearProgressIndicator.setVisibility(View.GONE);

            if (musicList.isEmpty()) {
                binding.noElementsLinearLayout.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            } else {
                binding.noElementsLinearLayout.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);

                // 更新适配器数据
                adapter.setData(musicList);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void showNoPermissionUI() {
        binding.linearProgressIndicator.setVisibility(View.GONE);
        binding.noElementsLinearLayout.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.noElementsTextView.setText("需要存储权限来访问音乐文件。请在设置中授予权限后重试。");
        Logs.w("MusicListFragment", "显示无权限UI");
    }

    private void showLoadingUI(String message) {
        binding.linearProgressIndicator.setVisibility(View.VISIBLE);

        // 只有在列表为空时才显示加载文本
        if (musicList.isEmpty()) {
            binding.noElementsLinearLayout.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.noElementsTextView.setText(message);
        }
    }

    private void showEmptyUI() {
        binding.noElementsLinearLayout.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.linearProgressIndicator.setVisibility(View.GONE);
        Logs.w("MusicListFragment", "显示空UI");
    }

    @Override
    public void onItemClick(MediaItem item, int position) {
        // 处理歌曲点击事件
        // 例如: 通过接口传递回Activity或启动播放服务
        Logs.d("MusicListFragment", "Song clicked: " + item.mediaMetadata.title);
        // 使用ViewModel播放音乐
        viewModel.playMusic(musicList, position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
