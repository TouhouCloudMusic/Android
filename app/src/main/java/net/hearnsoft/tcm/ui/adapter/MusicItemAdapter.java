package net.hearnsoft.tcm.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ItemMusicItemListBinding;
import net.hearnsoft.tcm.ui.interfaces.OnMusicItemClickListener;

import lombok.Getter;
import lombok.Setter;

public class MusicItemAdapter extends BaseAdapter<MediaItem, MusicItemAdapter.MusicItemHolder> {

    private OnMusicItemClickListener listener;

    @Getter
    @Setter
    private int currentPlayingIndex = -1;

    public MusicItemAdapter(OnMusicItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMusicItemListBinding binding = ItemMusicItemListBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MusicItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicItemHolder holder, int position) {
        holder.bind(dataList.get(position), position);
    }

    public class MusicItemHolder extends RecyclerView.ViewHolder {
        private ItemMusicItemListBinding binding;

        public MusicItemHolder(ItemMusicItemListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MediaItem song, int position) {
            if (song.mediaMetadata.artworkUri != null) {
                Glide.with(binding.coverImageView)
                        .load(song.mediaMetadata.artworkUri)
                        .placeholder(R.drawable.ic_nav_music)
                        .into(binding.coverImageView);
            } else {
                binding.coverImageView.setImageResource(R.drawable.ic_nav_music);
            }

            binding.headlineTextView.setText(song.mediaMetadata.title);
            binding.supportingTextView.setText(song.mediaMetadata.artist);

            if (listener != null) {
                binding.itemContainer.setOnClickListener(v -> {
                    listener.onItemClick(song, position);
                });
            }

            // 设置当前播放的 item 样式
            if (position == currentPlayingIndex) {
                binding.headlineTextView.setTypeface(null, Typeface.BOLD);
                binding.headlineTextView.setTextColor(
                    binding.getRoot().getContext().getColor(R.color.primary)
                );
                binding.supportingTextView.setTextColor(
                    binding.getRoot().getContext().getColor(R.color.primary)
                );
            } else {
                // 非当前播放的 item 样式
                binding.headlineTextView.setTypeface(null, Typeface.NORMAL);
                binding.headlineTextView.setTextColor(
                    binding.getRoot().getContext().getColor(R.color.text_primary)
                );
                binding.supportingTextView.setTextColor(
                    binding.getRoot().getContext().getColor(R.color.text_primary)
                );
            }
        }
    }

}
