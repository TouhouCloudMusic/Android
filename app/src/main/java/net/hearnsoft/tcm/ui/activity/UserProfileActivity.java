package net.hearnsoft.tcm.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yalantis.ucrop.UCrop;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.api.APICore;
import net.hearnsoft.tcm.api.UserAPI;
import net.hearnsoft.tcm.beans.UserProfile;
import net.hearnsoft.tcm.databinding.ActivityUserProfileBinding;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private static final String tempAvatarImageName = "avatar.jpg";
    private ActivityUserProfileBinding binding;
    private UserAPI userAPI;
    private UserProfileAdapter adapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickAvatar;

    private String userToken;
    private boolean isEditMode = false;
    private boolean isProfileUpdated = false; // 用于跟踪资料是否更新

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userAPI = UserAPI.getInstance(this);

        setSupportActionBar(binding.topAppbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProfileAdapter();
        binding.recyclerView.setAdapter(adapter);

        userToken = SettingsPrefUtils.getInstance(this).readStringSettings(Constants.KEY_USER_TOKEN);

        pickAvatar = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        String mimeType = getContentResolver().getType(uri);
                        if (isValidImageType(mimeType)) {
                            UCrop.Options options = new UCrop.Options();
                            // 图片格式
                            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                            // 设置图片压缩质量
                            options.setCompressionQuality(100);
                            UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), tempAvatarImageName)))
                                    .withAspectRatio(1, 1)
                                    .withMaxResultSize(500, 500)
                                    .withOptions(options)
                                    .start(UserProfileActivity.this);
                        } else {
                            runOnUiThread(() -> Toast.makeText(this,
                                    "请选择 JPG、JPEG 或 PNG 格式的图片!!", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.d(TAG, "No media selected");
                    }
                });

        loadUserProfile(userToken);
    }

    private void loadUserProfile(String userToken) {
        if (adapter != null && adapter.items.size() != 0) {
            adapter.items.clear();
        }
        userAPI.getUserProfile(userToken, new APICore.APICallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                List<ProfileItem> items = new ArrayList<>();
                items.add(new ProfileItem(ProfileItem.TYPE_AVATAR, "头像", getAvatarUrl(data.getAvatar()), true, item -> {
                    // 处理头像点击事件，例如打开图片选择器
                    openImagePicker();
                }));
                items.add(new ProfileItem(ProfileItem.TYPE_INFO, "名字", data.getName(), true, item -> {
                    // 处理名字点击事件，例如打开编辑对话框
                    openEditNameDialog(item.getContent());
                }));
                items.add(new ProfileItem(ProfileItem.TYPE_INFO, "标签", TextUtils.isEmpty(data.getLocation()) ? "未设置" : data.getLocation()));
                items.add(new ProfileItem(ProfileItem.TYPE_INFO, "E-mail", TextUtils.isEmpty(data.getEmail()) ? "未设置" : data.getEmail()));
                SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format_str), Locale.CHINA);
                items.add(new ProfileItem(ProfileItem.TYPE_INFO, "账户创建时间", dateFormat.format(data.getCreated_at())));
                items.add(new ProfileItem(ProfileItem.TYPE_INFO, "账户更新时间", dateFormat.format(data.getUpdated_at())));
                items.add(new ProfileItem(ProfileItem.TYPE_INFO, "权限", "Admin"));
                items.add(new ProfileItem(ProfileItem.TYPE_BUTTON,
                        getString(R.string.profile_title_logout), "", true, item -> logout()));
                adapter.setItems(items);
            }

            @Override
            public void onError(APICore.ApiError error) {
                Log.e(TAG, "failed to load profile, msg: " + error.getMessage());
                new MaterialAlertDialogBuilder(UserProfileActivity.this)
                        .setTitle("Error!")
                        .setMessage("Failed to load user profile!")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .show();
            }
        });
    }

    private void openImagePicker() {
        // TODO: 待实现
        pickAvatar.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private boolean isValidImageType(String mimeType) {
        return "image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType) || "image/png".equals(mimeType);
    }

    private void openEditNameDialog(String name) {
        // TODO: 待实现
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            uploadAvatar(userToken, UCrop.getOutput(data));
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(UserProfileActivity.this,
                    "Failed to crop avatar", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadAvatar(String userToken, Uri uri) {
        if (uri != null && !TextUtils.isEmpty(userToken)) {
            Log.d(TAG, "Selected URI: " + uri);
            userAPI.uploadAvatar(userToken, uri, getContentResolver(), new APICore.APICallback<String>() {
                @Override
                public void onSuccess(String data) {
                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this,
                            "上传头像成功！", Toast.LENGTH_SHORT).show());
                    loadUserProfile(userToken);
                    isProfileUpdated = true; // 标记资料已更新
                }

                @Override
                public void onError(APICore.ApiError error) {
                    Log.e(TAG, error.getMessage());
                    runOnUiThread(() ->Toast.makeText(UserProfileActivity.this,
                            "Failed to upload avatar, reason:\n"+ error.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void sendRefreshBroadcast() {
        Intent intent = new Intent("net.hearnsoft.tcm.ACTION_REFRESH_USER_PROFILE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getAvatarUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return Constants.API_HOST + "/image/" + fileName;
    }

    private void logout() {
        String token = SettingsPrefUtils.getInstance(this)
                .readStringSettings(Constants.KEY_USER_TOKEN);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.profile_title_logout);
        builder.setMessage(R.string.profile_title_logout_content);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            userAPI.logout(token, new APICore.APICallback<String>() {
                @Override
                public void onSuccess(String data) {
                    Toast.makeText(UserProfileActivity.this,
                            "注销登录成功",
                            Toast.LENGTH_SHORT).show();
                    // 发送广播通知 AccountFragment 刷新
                    Intent intent = new Intent("net.hearnsoft.tcm.ACTION_REFRESH_USER_PROFILE");
                    LocalBroadcastManager.getInstance(UserProfileActivity.this)
                            .sendBroadcast(intent);
                    dialog.dismiss();
                    finish();
                }

                @Override
                public void onError(APICore.ApiError error) {
                    Log.e(TAG, "failed to logout, msg: " + error.getMessage());
                    Toast.makeText(UserProfileActivity.this,
                            "注销登录失败",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (isProfileUpdated) {
            sendRefreshBroadcast();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isProfileUpdated) {
                sendRefreshBroadcast();
            }
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_profile_edit) {
            toggleEditMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateEditModeUI();
        adapter.notifyDataSetChanged();
    }

    private void updateEditModeUI() {
        MenuItem editItem = binding.topAppbar.getMenu().findItem(R.id.menu_profile_edit);
        if (isEditMode) {
            editItem.setIcon(R.drawable.ic_check_finish_24px); // 使用一个"完成"图标
            editItem.setTitle(R.string.menu_profile_edit_done);
        } else {
            editItem.setIcon(R.drawable.ic_edit_24px); // 使用一个"编辑"图标
            editItem.setTitle(R.string.menu_profile_edit);
        }
    }

    private class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<ProfileItem> items = new ArrayList<>();

        void setItems(List<ProfileItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == ProfileItem.TYPE_AVATAR) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_profile_avatar, parent, false);
                return new AvatarViewHolder(view);
            } else if (viewType == ProfileItem.TYPE_BUTTON) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_profile_button, parent, false);
                return new ButtonViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_profile, parent, false);
                return new InfoViewHolder(view);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).getType();
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ProfileItem item = items.get(position);
            if (holder instanceof AvatarViewHolder) {
                AvatarViewHolder avatarHolder = (AvatarViewHolder) holder;
                Glide.with(avatarHolder.itemView.getContext())
                        .load(item.getContent())
                        .placeholder(R.drawable.test_avatar)
                        .into(avatarHolder.avatarImageView);
                setItemClickListener(avatarHolder.itemView, item, isEditMode);
                // 根据编辑模式更新 UI
                if (isEditMode && item.isClickable()) {
                    avatarHolder.itemView.setBackgroundResource(R.drawable.bg_editable_item);
                } else {
                    avatarHolder.itemView.setBackgroundResource(0);
                }
            } else if (holder instanceof InfoViewHolder) {
                InfoViewHolder infoHolder = (InfoViewHolder) holder;
                infoHolder.titleTextView.setText(item.getTitle());
                infoHolder.contentTextView.setText(item.getContent());
                setItemClickListener(infoHolder.itemView, item, isEditMode);
                // 根据编辑模式更新 UI
                if (isEditMode && item.isClickable()) {
                    infoHolder.itemView.setBackgroundResource(R.drawable.bg_editable_item);
                } else {
                    infoHolder.itemView.setBackgroundResource(0);
                }
            } else if (holder instanceof ButtonViewHolder) {
                ButtonViewHolder buttonHolder = (ButtonViewHolder) holder;
                buttonHolder.button.setText(item.getTitle());
                setItemClickListener(buttonHolder.button, item, false);
            }
        }

        private void setItemClickListener(View itemView, ProfileItem item, boolean editMode) {
            if (item.isClickable() && item.getClickListener() != null && (editMode || item.getType() == ProfileItem.TYPE_BUTTON)) {
                itemView.setOnClickListener(v -> item.getClickListener().onItemClick(item));
                itemView.setClickable(true);
            } else {
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class AvatarViewHolder extends RecyclerView.ViewHolder {
            ImageView avatarImageView;

            AvatarViewHolder(View itemView) {
                super(itemView);
                avatarImageView = itemView.findViewById(R.id.avatarImageView);
            }
        }

        class InfoViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView contentTextView;

            InfoViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
            }
        }

        class ButtonViewHolder extends RecyclerView.ViewHolder {
            MaterialButton button;

            ButtonViewHolder(View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.profileButton);
            }
        }
    }
}
