package net.hearnsoft.tcm.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;

import java.util.ArrayList;
import java.util.List;

public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ProfileItem> items = new ArrayList<>();
    private boolean isEditMode = false;

    public void setItems(List<ProfileItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case ProfileItem.TYPE_BANNER:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile_banner, parent, false);
                holder = new BannerViewHolder(view);
                break;
            case ProfileItem.TYPE_AVATAR:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile_avatar, parent, false);
                holder = new AvatarViewHolder(view);
                break;
            case ProfileItem.TYPE_BUTTON:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile_button, parent, false);
                holder = new ButtonViewHolder(view);
                break;
            case ProfileItem.TYPE_INFO:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile, parent, false);
                holder = new InfoViewHolder(view);
                break;
            case ProfileItem.TYPE_PREFERENCE_ITEM:
            default:
                view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile_preference, parent, false);
                holder = new PreferenceViewHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileItem item = items.get(position);
        if (holder instanceof AvatarViewHolder) {
            bindAvatarViewHolder((AvatarViewHolder) holder, item);
        } else if (holder instanceof InfoViewHolder) {
            bindInfoViewHolder((InfoViewHolder) holder, item);
        } else if (holder instanceof ButtonViewHolder) {
            bindButtonViewHolder((ButtonViewHolder) holder, item);
        } else if (holder instanceof PreferenceViewHolder) {
            bindPreferenceViewHolder((PreferenceViewHolder) holder, item);
        } else if (holder instanceof BannerViewHolder) {
            bindBannerViewHolder((BannerViewHolder) holder, item);
        }
    }

    private void bindAvatarViewHolder(AvatarViewHolder holder, ProfileItem item) {
        Glide.with(holder.itemView.getContext())
            .load(item.getContent())
            .placeholder(R.drawable.default_avatar)
            .into(holder.avatarImageView);
        setClickListener(holder.itemView, item, isEditMode);

        if (isEditMode && item.isClickable()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_editable_item);
        } else {
            holder.itemView.setBackgroundResource(0);
        }
    }

    private void bindBannerViewHolder(BannerViewHolder holder, ProfileItem item) {
        Glide.with(holder.itemView.getContext())
            .load(item.getContent())
            .into(holder.bannerImageView);
        setClickListener(holder.itemView, item, isEditMode);

        if (isEditMode && item.isClickable()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_editable_item);
        } else {
            holder.itemView.setBackgroundResource(0);
        }
    }

    private void bindInfoViewHolder(InfoViewHolder holder, ProfileItem item) {
        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());
        setClickListener(holder.itemView, item, isEditMode);

        if (isEditMode && item.isClickable()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_editable_item);
        } else {
            holder.itemView.setBackgroundResource(0);
        }
    }

    private void bindButtonViewHolder(ButtonViewHolder holder, ProfileItem item) {
        holder.button.setText(item.getTitle());
        setClickListener(holder.button, item, false);
    }

    private void bindPreferenceViewHolder(PreferenceViewHolder holder, ProfileItem item) {
        holder.titleTextView.setText(item.getTitle());
        if (TextUtils.isEmpty(item.getContent())) {
            holder.contentTextView.setVisibility(View.GONE);
        } else {
            holder.contentTextView.setVisibility(View.VISIBLE);
            holder.contentTextView.setText(item.getContent());
        }
        holder.itemView.setBackgroundResource(R.drawable.bg_preference_item);
        setClickListener(holder.itemView, item, true);
    }

    private void setClickListener(View view, ProfileItem item, boolean shouldRespond) {
        if (item.isClickable() && item.getClickListener() != null &&
            (shouldRespond || item.getType() == ProfileItem.TYPE_BUTTON)) {
            view.setOnClickListener(v -> item.getClickListener().onItemClick(item));
            view.setClickable(true);
        } else {
            view.setOnClickListener(null);
            view.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder 类定义...
    static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;

        AvatarViewHolder(View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
        }
    }

    static class InfoViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;

        InfoViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }

    static class PreferenceViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;

        PreferenceViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titlePrefTextView);
            contentTextView = itemView.findViewById(R.id.contentPrefTextView);
        }
    }

    static class ButtonViewHolder extends RecyclerView.ViewHolder {
        MaterialButton button;

        ButtonViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.profileButton);
        }
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImageView;

        BannerViewHolder(View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.profileBannerImageView);
        }
    }
}
