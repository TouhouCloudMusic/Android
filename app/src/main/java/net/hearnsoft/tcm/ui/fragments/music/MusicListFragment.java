package net.hearnsoft.tcm.ui.fragments.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.DialogAlistConfigBinding;
import net.hearnsoft.tcm.databinding.FragmentMusicListBinding;
import net.hearnsoft.tcm.domain.model.song.SongSortingRule;
import net.hearnsoft.tcm.domain.model.song.SongSortingStrategy;
import net.hearnsoft.tcm.domain.repository.MediaRepository;
import net.hearnsoft.tcm.ui.adapter.MusicItemAdapter;
import net.hearnsoft.tcm.ui.interfaces.OnMusicItemClickListener;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;
import net.hearnsoft.tcm.ui.utils.LinearTopSmoothScroller;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;

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

    // 当前选择的仓库类型
    private MediaRepository.RepositoryType currentRepositoryType = MediaRepository.RepositoryType.LOCAL;

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
        // 创建LinearTopSmoothScroller
        scroller = new LinearTopSmoothScroller(requireContext(), true);

        //设置排序chips
        setSortingChips();

        // 设置仓库选择按钮
        setRepositorySelectorButton();

        // 获取ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

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

        // 从 SharedPreferences 加载 Alist 配置
        loadAlistConfigsFromPrefs();

        // 检查权限
        checkPermissionAndLoadMusic();
    }

    // 添加仓库选择按钮
    private void setRepositorySelectorButton() {
        // 假设我们在布局中添加了一个切换仓库的按钮
        binding.musicRepositorySelect.setOnClickListener(v -> {
            showRepositoryMenu(v);
        });
    }

    private void showRepositoryMenu(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        Menu menu = popup.getMenu();

        // 添加默认选项 - 本地
        menu.add(Menu.NONE, 1, Menu.NONE, "本地音乐");

        // 检查是否有 Alist 仓库
        List<MediaRepository> alistRepos = viewModel.getRepositoriesByType(MediaRepository.RepositoryType.ALIST);

        // 只有当有 Alist 仓库时才显示 Alist 选项
        if (!alistRepos.isEmpty()) {
            // 为每个 Alist 仓库创建菜单项
            int id = 100; // 使用 100 以上的 ID，避免与固定菜单项冲突
            for (MediaRepository repo : alistRepos) {
                menu.add(Menu.NONE, id++, Menu.NONE, repo.getRepositoryName() + " (Alist)");
            }

            // 添加 Alist 总选项（所有 Alist 仓库）
            menu.add(Menu.NONE, 2, Menu.NONE, "全部 Alist 音乐");
        }

        // 添加全部选项
        menu.add(Menu.NONE, 3, Menu.NONE, "全部音乐");

        // 添加设置 Alist 选项
        menu.add(Menu.NONE, 4, Menu.NONE, "添加 Alist 仓库...");

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == 1) {
                // 本地音乐
                viewModel.loadMediaByType(MediaRepository.RepositoryType.LOCAL);
                binding.musicRepositorySelect.setText("本地音乐");
                return true;
            } else if (itemId == 2) {
                // 全部 Alist 音乐
                currentRepositoryType = MediaRepository.RepositoryType.ALL_ALIST;
                viewModel.loadMediaByType(MediaRepository.RepositoryType.ALL_ALIST);
                binding.musicRepositorySelect.setText("全部 Alist 音乐");
                return true;
            } else if (itemId == 3) {
                // 全部音乐
                currentRepositoryType = MediaRepository.RepositoryType.ALL;
                viewModel.loadMediaByType(MediaRepository.RepositoryType.ALL);
                binding.musicRepositorySelect.setText("全部音乐");
                return true;
            } else if (itemId == 4) {
                // 添加 Alist 仓库
                showAlistConfigDialog();
                return true;
            } else if (itemId >= 100) {
                // 选择特定的 Alist 仓库
                int index = itemId - 100;
                if (index < alistRepos.size()) {
                    MediaRepository selectedRepo = alistRepos.get(index);
                    viewModel.loadMediaByType(selectedRepo);
                    currentRepositoryType = selectedRepo.getRepositoryType();
                    binding.musicRepositorySelect.setText(selectedRepo.getRepositoryName() + " (Alist)");
                }
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showAlistConfigDialog() {
        // 创建对话框布局绑定
        DialogAlistConfigBinding dialogBinding = DialogAlistConfigBinding.inflate(LayoutInflater.from(requireContext()));

        // 监听匿名访问勾选状态
        dialogBinding.anonymousCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.GONE : View.VISIBLE;
            dialogBinding.usernameEditText.setVisibility(visibility);
            dialogBinding.passwordEditText.setVisibility(visibility);
        });

        // 默认勾选匿名访问
        dialogBinding.anonymousCheckBox.setChecked(true);
        dialogBinding.usernameEditText.setVisibility(View.GONE);
        dialogBinding.passwordEditText.setVisibility(View.GONE);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("添加Alist仓库")
            .setView(dialogBinding.getRoot())
            .setPositiveButton("添加", (dialog, which) -> {
                // 获取输入的配置信息
                String name = dialogBinding.nameEditText.getText().toString();
                String host = dialogBinding.hostEditText.getText().toString();
                String username = dialogBinding.usernameEditText.getText().toString();
                String password = dialogBinding.passwordEditText.getText().toString();
                String rootPath = dialogBinding.rootPathEditText.getText().toString();
                boolean isAnonymous = dialogBinding.anonymousCheckBox.isChecked();

                // 检查输入是否有效
                if (name.isEmpty() || host.isEmpty()) {
                    Toast.makeText(requireContext(), "名称和主机地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 添加新的Alist仓库并自动激活
                viewModel.initDefaultAlistRepository(name, host, username, password, rootPath, isAnonymous, true);

                // 切换到Alist仓库类型并加载
                currentRepositoryType = MediaRepository.RepositoryType.ALIST;
                viewModel.setCurrentRepositoryType(MediaRepository.RepositoryType.ALIST);
                binding.musicRepositorySelect.setText(name + " (Alist)");
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void loadAlistConfigsFromPrefs() {
        SettingsPrefUtils prefUtils = SettingsPrefUtils.getInstance(requireContext());

        // 获取保存的 Alist 仓库数量
        int count = prefUtils.readIntSettings("alist_repo_count");

        Logs.d("MusicListFragment", "从 SharedPreferences 中加载了 " + count + " 个 Alist 仓库配置");

        // 如果有保存的仓库，初始化它们
        for (int i = 0; i < count; i++) {
            String name = prefUtils.readStringSettings("alist_repo_" + i + "_name", "");
            String host = prefUtils.readStringSettings("alist_repo_" + i + "_host", "");
            String username = prefUtils.readStringSettings("alist_repo_" + i + "_username", "");
            String password = prefUtils.readStringSettings("alist_repo_" + i + "_password", "");
            String rootPath = prefUtils.readStringSettings("alist_repo_" + i + "_rootPath", "/");
            boolean isAnonymous = prefUtils.readBooleanSettings("alist_repo_" + i + "_anonymous", true);

            // 只有当名称和主机不为空时才添加仓库，但不自动激活
            if (!name.isEmpty() && !host.isEmpty()) {
                // 在冷启动时不自动激活Alist仓库
                viewModel.initDefaultAlistRepository(name, host, username, password, rootPath, isAnonymous, false);
            }
        }
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

        // 确保冷启动时只加载本地仓库
        viewModel.setCurrentRepositoryType(MediaRepository.RepositoryType.LOCAL);

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
            Logs.d("MusicListFragment", "使用已存在的播放列表");
            musicList = viewModel.getPlaylist().getValue();
            updateMusicListUI();
        } else {
            // 让ViewModel处理扫描
            viewModel.setCurrentRepositoryType(currentRepositoryType);
            viewModel.scanAndLoadMusic();
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
                viewModel.sortMusic(new SongSortingRule(SongSortingStrategy.Title, false));

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
