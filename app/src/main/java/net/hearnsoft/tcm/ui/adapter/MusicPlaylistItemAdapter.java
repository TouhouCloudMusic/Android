package net.hearnsoft.tcm.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.ui.interfaces.OnMusicPlaylistItemClickListener;

import lombok.Getter;
import lombok.Setter;

public class MusicPlaylistItemAdapter extends BaseAdapter<MediaItem, MusicPlaylistItemAdapter.PlaylistItemViewHolder> {

    private OnMusicPlaylistItemClickListener listener;
    private View mItemView;

    @Getter
    @Setter
    private int currentPlayingIndex = -1;

    public MusicPlaylistItemAdapter(OnMusicPlaylistItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_playlist, parent, false);
        return new PlaylistItemViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistItemViewHolder holder, int position) {
        MediaMetadata metadata = dataList.get(position).mediaMetadata;
        holder.itemTitle.setText(metadata.title);
        holder.itemSubtitle.setText(metadata.artist);

        // 设置当前播放的 item 样式
        if (position == currentPlayingIndex) {
            holder.itemTitle.setTypeface(null, Typeface.BOLD);
            holder.itemTitle.setTextColor(
                holder.itemView.getContext().getColor(R.color.system_accent)
            );
            holder.itemSubtitle.setTextColor(
                holder.itemView.getContext().getColor(R.color.system_accent)
            );
            holder.itemContainer.setBackgroundResource(R.drawable.bg_playlist_current_item);
        } else {
            // 非当前播放的 item 样式
            holder.itemTitle.setTypeface(null, Typeface.NORMAL);
            holder.itemTitle.setTextColor(
                holder.itemView.getContext().getColor(R.color.black)
            );
            holder.itemSubtitle.setTextColor(
                holder.itemView.getContext().getColor(R.color.black)
            );
            holder.itemContainer.setBackgroundResource(R.drawable.bg_playlist_item_background);
        }

        holder.itemContainer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        holder.itemRemoveButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoveClick(position);
            }
        });
    }

    public class PlaylistItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemTitle;
        private final TextView itemSubtitle;
        private final ConstraintLayout itemContainer;
        private final ImageButton itemRemoveButton;

        public PlaylistItemViewHolder(View itemView) {
            super(itemView);
            this.itemTitle = itemView.findViewById(R.id.itemMusicPlaylistTitle);
            this.itemSubtitle = itemView.findViewById(R.id.itemMusicPlaylistSubtitle);
            this.itemContainer = itemView.findViewById(R.id.itemMusicPlaylistContainer);
            this.itemRemoveButton = itemView.findViewById(R.id.itemMusicPlaylistRemove);
        }
    }
}