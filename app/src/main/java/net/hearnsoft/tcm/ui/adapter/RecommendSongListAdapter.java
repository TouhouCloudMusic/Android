package net.hearnsoft.tcm.ui.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.hearnsoft.tcm.beans.RecommendSongBean;
import net.hearnsoft.tcm.databinding.ItemRecommendSongBinding;
import net.hearnsoft.tcm.R;

public class RecommendSongListAdapter
        extends BaseAdapter<RecommendSongBean, RecommendSongListAdapter.SongViewHolder>{

    private OnItemClickListener<RecommendSongBean> listener;

    public void setOnItemClickListener(OnItemClickListener<RecommendSongBean> listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecommendSongBinding binding = ItemRecommendSongBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SongViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bind(dataList.get(position), position);
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecommendSongBinding binding;

        public SongViewHolder(ItemRecommendSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecommendSongBean song, int position) {
            binding.tvSongName.setText(song.getTitle());
            binding.tvArtistName.setText(song.getArtist());

            // 显示加载进度条
            binding.loadingProgress.setVisibility(View.VISIBLE);
            binding.ivSongCover.setImageDrawable(null);

            Glide.with(binding.ivSongCover)
                    .load(song.getCoverUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            binding.loadingProgress.setVisibility(View.GONE);
                            binding.ivSongCover.setImageResource(R.drawable.ic_close_24px);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            binding.loadingProgress.setVisibility(View.GONE);
                            binding.ivSongCover.setImageDrawable(resource);
                            return true;
                        }
                    })
                    .into(binding.ivSongCover);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(song, position);
                }
            });
        }
    }
}
