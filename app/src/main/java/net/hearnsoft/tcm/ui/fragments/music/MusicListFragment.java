package net.hearnsoft.tcm.ui.fragments.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.beans.SortingRule;
import net.hearnsoft.tcm.databinding.FragmentMusicListBinding;
import net.hearnsoft.tcm.enums.SortingStrategy;
import net.hearnsoft.tcm.ui.adapter.MusicItemAdapter;
import net.hearnsoft.tcm.ui.adapter.OnMusicItemClickListener;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;
import net.hearnsoft.tcm.utils.LocalMusicSorter;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.MusicPlayerController;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@UnstableApi
public class MusicListFragment extends Fragment implements OnMusicItemClickListener {
    private FragmentMusicListBinding binding;
    private MusicItemAdapter adapter;
    private List<MediaItem> musicList = new ArrayList<>();
    private PlaybackViewModel viewModel;
    // 拼音缓存
    private final Map<String, String> pinyinCache = new LinkedHashMap<String, String>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            // 限制缓存大小为100个项目
            return size() > 100;
        }
    };

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadMusic();
                } else {
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

        //设置排序chips
        setSortingChips();

        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        setViewModelObserver();

        // 检查权限
        checkPermissionAndLoadMusic();
    }

    private void setViewModelObserver() {
        // 观察排序进度
        viewModel.getIsSorting().observe(getViewLifecycleOwner(), isSorting -> {
            binding.linearProgressIndicator.setVisibility(isSorting ? View.VISIBLE : View.GONE);
        });

        // 观察扫描进度
        viewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
            binding.linearProgressIndicator.setVisibility(isScanning ? View.VISIBLE : View.GONE);

            if (!isScanning) {
                List<MediaItem> scannedMusic = viewModel.getPlaylist().getValue();
                if (scannedMusic != null && !scannedMusic.isEmpty()) {
                    musicList = scannedMusic;
                    updateMusicListUI();
                }
            }
        });

        // 观察播放列表变化
        viewModel.getPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            if (playlist != null && !playlist.isEmpty()) {
                musicList = playlist;
                adapter.setData(musicList);
                adapter.notifyDataSetChanged();

                if (binding.noElementsLinearLayout.getVisibility() == View.VISIBLE) {
                    binding.noElementsLinearLayout.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }
            } else {
                binding.noElementsLinearLayout.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        });

        // 观察当前排序规则
        viewModel.getCurrentSortRule().observe(getViewLifecycleOwner(), rule -> {
            binding.musicSortingChip.setSortingRule(rule);
        });
    }

    private void setSortingChips() {
        SortedMap<SortingStrategy, Integer> sortingOptions = new TreeMap<>();
        sortingOptions.put(SortingStrategy.ARTIST_NAME, R.string.sort_by_artist_name);
        sortingOptions.put(SortingStrategy.CREATION_DATE, R.string.sort_by_release_date);
        sortingOptions.put(SortingStrategy.NAME, R.string.sort_by_title);
        sortingOptions.put(SortingStrategy.PLAY_COUNT, R.string.sort_by_play_count);

        binding.musicSortingChip.setSortingStrategies(sortingOptions);

        // 设置默认排序规则
        SortingRule defaultRule = new SortingRule(SortingStrategy.NAME, false);
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
                // 已有权限
                loadMusic();
            } else {
                // 请求权限
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // 已有权限
                loadMusic();
            } else {
                // 请求权限
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void loadMusic() {
        binding.linearProgressIndicator.setVisibility(View.VISIBLE);
        binding.noElementsLinearLayout.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);

        // 检查是否已加载音乐
        if (viewModel.getPlaylist().getValue() != null
                && !viewModel.getPlaylist().getValue().isEmpty()) {
            // 使用现有列表
            Logs.d("MusicListFragment","使用已存在的播放列表");
            musicList = viewModel.getPlaylist().getValue();
            updateMusicListUI();
        } else {
            // 让ViewModel处理扫描
            viewModel.scanAndLoadMusic(requireContext());
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

                // 应用默认排序
                viewModel.sortMusic(new SortingRule(SortingStrategy.NAME, false));

                // 更新适配器数据
                adapter.setData(musicList);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void showNoPermissionUI() {
        binding.linearProgressIndicator.setVisibility(View.GONE);
        binding.noElementsLinearLayout.setVisibility(View.VISIBLE);
        binding.noElementsTextView.setText("需要存储权限来访问音乐");
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
