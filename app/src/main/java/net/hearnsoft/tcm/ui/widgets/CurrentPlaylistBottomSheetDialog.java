package net.hearnsoft.tcm.ui.widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.DialogCurrentPlaylistBinding;
import net.hearnsoft.tcm.ui.adapter.MusicPlaylistItemAdapter;
import net.hearnsoft.tcm.ui.interfaces.OnMusicPlaylistItemClickListener;
import net.hearnsoft.tcm.ui.model.PlaybackViewModel;
import net.hearnsoft.tcm.ui.utils.LinearTopSmoothScroller;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.uiwidgets.dialog.MDialog;

@UnstableApi
public class CurrentPlaylistBottomSheetDialog extends BaseSheetDialog implements
    OnMusicPlaylistItemClickListener {
    private DialogCurrentPlaylistBinding binding;
    private PlaybackViewModel viewModel;
    private MusicPlaylistItemAdapter adapter;
    private LinearTopSmoothScroller scroller;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        binding = DialogCurrentPlaylistBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化RecyclerView
        adapter = new MusicPlaylistItemAdapter(this);

        // 初始化ViewModel - 从Activity获取
        viewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        // 设置观察者
        setupObservers();

        // 初始化其他组件
        initComponents();
    }

    private void initComponents() {
        binding.dialogPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.dialogPlaylistRecyclerView.setAdapter(adapter);

        // 创建LinearTopSmoothScroller
        scroller = new LinearTopSmoothScroller(requireContext(), true);

        // 设置清空按钮事件
        binding.dialogClearPlaylistButton.setOnClickListener(v -> {
            MDialog dialog = createClearPlaylistDialog();
            dialog.show();
        });

        // 设置洗牌播放列表按钮点击事件
        binding.dialogPlaylistShuffleButton.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.shuffleCurrentPlaylist();
            }
        });

        // 设置RecyclerView的高度
        setRecyclerViewDynamicHeight(binding.dialogPlaylistRecyclerView);

        // 对话框显示后滚动到当前播放项目
        scrollToCurrentPlayingItem();

        // 添加点击索引跳转功能
        binding.dialogPlaylistIndexCounter.setOnClickListener(v -> scrollToCurrentPlayingItem());
    }

    private void setupObservers() {
        // 观察当前播放列表
        viewModel.getCurrentPlaylist().observe(getViewLifecycleOwner(), mediaItems -> {
            if (mediaItems != null) {
                adapter.setData(mediaItems);
                Logs.d("CurrentPlaylistBottomSheetDialog", "adapter.setData: " + mediaItems.size());

                // 更新播放列表计数器
                int currentIndex = viewModel.getCurrentIndex().getValue() != null ?
                    viewModel.getCurrentIndex().getValue() : 0;
                binding.dialogPlaylistIndexCounter.setText(String.format("%d/%d",
                    currentIndex + 1, mediaItems.size()));
            } else {
                binding.dialogPlaylistIndexCounter.setText("0/0");
            }
        });

        // 观察当前播放索引变化
        viewModel.getCurrentIndex().observe(getViewLifecycleOwner(), index -> {
            if (index != null) {
                int size = adapter.getItemCount();
                binding.dialogPlaylistIndexCounter.setText(String.format("%d/%d",
                    index + 1, size));
                adapter.setCurrentPlayingIndex(index);
                adapter.notifyDataSetChanged();

                // 添加滚动到当前播放项目的逻辑
                if (binding.dialogPlaylistRecyclerView.getLayoutManager() != null) {
                    // 平滑滚动到指定位置并置顶
                    binding.dialogPlaylistRecyclerView.getLayoutManager()
                        .scrollToPosition(index);
                }
            }
        });
    }

    private MDialog createClearPlaylistDialog() {
        return new MDialog.Builder(requireContext())
            .setTitle(R.string.dialog_playlist_clear_title)
            .setMessage(R.string.dialog_playlist_clear_msg)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                if (viewModel != null) {
                    viewModel.clearCurrentPlaylist();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }

    private void scrollToCurrentPlayingItem() {
        // 获取当前播放索引
        Integer currentIndex = viewModel.getCurrentIndex().getValue();
        if (currentIndex != null && binding.dialogPlaylistRecyclerView.getLayoutManager() != null) {
            binding.dialogPlaylistRecyclerView.post(() -> {
                // 平滑滚动到当前位置
                scroller.setTargetPosition(currentIndex);
                binding.dialogPlaylistRecyclerView.getLayoutManager().startSmoothScroll(scroller);
            });
        }
    }

    @Override
    public void onItemClick(int position) {
        // 点击列表项播放对应音乐
        if (viewModel != null) {
            viewModel.skipToQueueItem(position);
        }
    }

    @Override
    public void onItemRemoveClick(int position) {
        // 从列表中移除该项
        if (viewModel != null && viewModel.getCurrentPlaylist().getValue() != null) {
            viewModel.removeFromCurrentPlaylistByPosition(position);
        }
    }
}