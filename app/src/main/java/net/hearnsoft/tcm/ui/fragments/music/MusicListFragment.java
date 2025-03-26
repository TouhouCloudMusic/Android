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

        // 检查权限
        checkPermissionAndLoadMusic();
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
        binding.musicSortingChip.setOnSortingRuleSelectedListener(this::sortMusicList);
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

        // Check if music is already loaded
        if (viewModel.getPlaylist().getValue() != null
                && !viewModel.getPlaylist().getValue().isEmpty()) {
            // Use existing playlist
            Logs.d("MusicListFragment","Using existing playlist");
            musicList = viewModel.getPlaylist().getValue();
            updateMusicListUI();
        } else {
            // Let ViewModel handle scanning
            viewModel.scanAndLoadMusic(requireContext());

            // Observe scanning state
            viewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
                if (!isScanning) {
                    // When scanning is finished, get playlist from ViewModel
                    List<MediaItem> scannedMusic = viewModel.getPlaylist().getValue();
                    if (scannedMusic != null) {
                        musicList = scannedMusic;
                        updateMusicListUI();
                    }
                }
            });

            // Observe playlist changes
            viewModel.getPlaylist().observe(getViewLifecycleOwner(), playlist -> {
                if (playlist != null && !playlist.isEmpty()) {
                    Logs.d("MusicListFragment","Playlist updated: " + playlist.size());
                    musicList = playlist;
                    updateMusicListUI();
                }
            });
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

                // Apply default sorting
                SortingRule currentRule = new SortingRule(SortingStrategy.NAME, false);
                sortMusicList(currentRule);

                // Update UI to show current sort state
                binding.musicSortingChip.setSortingRule(currentRule);
            }
        }
    }

    /**
     * 根据所选规则对音乐列表进行排序
     * @param sortingRule 排序规则，包含排序策略和方向
     */
    private void sortMusicList(SortingRule sortingRule) {
        if (musicList.isEmpty()) return;

        // 显示进度条
        binding.linearProgressIndicator.setVisibility(View.VISIBLE);

        // 在后台线程中执行排序
        new Thread(() -> {
            SortingStrategy strategy = sortingRule.getStrategy();
            boolean isReverse = sortingRule.isReverse();
            Comparator<MediaItem> comparator = getComparatorForStrategy(strategy);

            if (comparator != null) {
                // 创建列表副本进行排序，避免并发修改问题
                List<MediaItem> sortedList = new ArrayList<>(musicList);
                Collections.sort(sortedList, isReverse ? comparator.reversed() : comparator);

                // 更新UI需要在主线程执行
                // 这里防止Fragment没有attach到activity
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        musicList = sortedList;
                        adapter.setData(musicList);
                        adapter.notifyDataSetChanged();

                        // 更新排序选项显示
                        binding.musicSortingChip.setSortingRule(sortingRule);

                        // 隐藏进度条
                        binding.linearProgressIndicator.setVisibility(View.GONE);

                        Logs.d("MusicListFragment", "Applied sorting: " + strategy +
                                (isReverse ? " (descending)" : " (ascending)"));
                    });
                }
            } else {
                requireActivity().runOnUiThread(() -> {
                    binding.linearProgressIndicator.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private Comparator<MediaItem> getComparatorForStrategy(SortingStrategy strategy) {
        switch (strategy) {
            case ARTIST_NAME:
                return (item1, item2) -> {
                    String artist1 = item1.mediaMetadata.artist != null ?
                            item1.mediaMetadata.artist.toString() : "";
                    String artist2 = item2.mediaMetadata.artist != null ?
                            item2.mediaMetadata.artist.toString() : "";
                    return compareChinese(artist1, artist2);
                };
            case NAME:
                return (item1, item2) -> {
                    String title1 = item1.mediaMetadata.title != null ?
                            item1.mediaMetadata.title.toString() : "";
                    String title2 = item2.mediaMetadata.title != null ?
                            item2.mediaMetadata.title.toString() : "";
                    return compareChinese(title1, title2);
                };
            case CREATION_DATE:
                return (item1, item2) -> {
                    String id1 = item1.mediaId.substring(item1.mediaId.lastIndexOf("/") + 1);
                    String id2 = item2.mediaId.substring(item2.mediaId.lastIndexOf("/") + 1);
                    try {
                        return Long.compare(Long.parseLong(id1), Long.parseLong(id2));
                    } catch (NumberFormatException e) {
                        return id1.compareTo(id2);
                    }
                };
            case PLAY_COUNT:
                Logs.d("MusicListFragment", "Play count sorting not implemented yet");
                return null;
            default:
                return null;
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

    /**
     * 比较两个可能包含中文的字符串
     * 先按拼音排序，如果拼音相同则按原字符串排序
     */
    private int compareChinese(String str1, String str2) {
        try {
            // 创建HanyuPinyinOutputFormat对象
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            // 设置拼音输出格式
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            format.setVCharType(HanyuPinyinVCharType.WITH_V);

            // 获取字符串的拼音
            String pinyin1 = getPinyin(str1, format);
            String pinyin2 = getPinyin(str2, format);

            // 先按拼音比较
            int result = pinyin1.compareToIgnoreCase(pinyin2);

            // 如果拼音相同则按原始字符串比较
            if (result == 0) {
                return str1.compareToIgnoreCase(str2);
            }

            return result;
        } catch (Exception e) {
            Logs.e("MusicListFragment", "Error comparing Chinese strings: " + e.getMessage());
            // 如果出现异常，返回原始比较结果
            return str1.compareToIgnoreCase(str2);
        }
    }

    /**
     * 获取字符串的拼音表示
     */
    /**
     * 获取字符串的拼音表示（带缓存）
     */
    private String getPinyin(String str, HanyuPinyinOutputFormat format) throws BadHanyuPinyinOutputFormatCombination {
        if (str == null || str.isEmpty()) {
            return "";
        }

        // 检查缓存中是否已存在
        if (pinyinCache.containsKey(str)) {
            return pinyinCache.get(str);
        }

        StringBuilder pinyin = new StringBuilder();
        char[] chars = str.toCharArray();

        for (char c : chars) {
            // 判断是否为汉字
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                // 中文字符，获取拼音
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    pinyin.append(pinyinArray[0]);
                } else {
                    pinyin.append(c);
                }
            } else {
                // 非中文字符，直接添加
                pinyin.append(c);
            }
        }

        String result = pinyin.toString();
        // 存入缓存
        pinyinCache.put(str, result);

        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
