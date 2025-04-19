package net.hearnsoft.tcm.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yalantis.ucrop.UCrop;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ActivityUserProfileBinding;
import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.domain.model.user.UserRole;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.OffsetDateTimeFormater;
import net.hearnsoft.tcm.utils.ViewModelUtils;
import net.hearnsoft.thcdb_sdk.model.UserProfile;
import net.hearnsoft.thcdb_sdk.model.UserProfileRoles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends BaseActivity {

    private static final String tempAvatarImageName = "avatar.jpg";
    private final String TAG = this.getClass().getSimpleName();
    private ActivityUserProfileBinding binding;
    private UserViewModel userViewModel;
    private UserProfileAdapter adapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickAvatar;
    private AlertDialog errorDialog;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });

        // 初始化UserViewModel
        userViewModel = ViewModelUtils.getViewModel(this, UserViewModel.class);

        setSupportActionBar(binding.topAppbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProfileAdapter();
        binding.recyclerView.setAdapter(adapter);

        pickAvatar = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
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
                        runOnUiThread(() -> Toast.makeText(
                            this,
                            R.string.toast_err_image_wrong,
                            Toast.LENGTH_SHORT
                        ).show());
                    }
                } else {
                    Logs.d(TAG, "No media selected");
                }
            }
        );
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (adapter != null && !adapter.items.isEmpty()) {
            adapter.items.clear();
        }
        // Set up observers
        userViewModel.getUserProfile().observe(this, this::setupUserProfileList);

        userViewModel.getError().observe(this, error -> {
            if (error != null) {
                Logs.e(TAG, "failed to load profile, msg: " + error);
                errorDialog = new MaterialAlertDialogBuilder(UserProfileActivity.this)
                    .setTitle(R.string.profile_dialog_err_title)
                    .setMessage(getString(R.string.profile_dialog_err_msg)
                        +"\n"+error)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    })
                    .create();
                runOnUiThread(() -> {
                    if (errorDialog != null && !errorDialog.isShowing()) {
                        errorDialog.show();
                    }
                });
            }
        });
    }

    private void setupUserProfileList(UserProfileModel data) {
        if (data != null) {
            List<ProfileItem> items = new ArrayList<>();
            items.add(new ProfileItem(
                ProfileItem.TYPE_AVATAR,
                getString(R.string.profile_title_avatar),
                getAvatarUrl(data.getAvatarUrl()),
                true,
                item -> openImagePicker()
            ));
            items.add(new ProfileItem(
                ProfileItem.TYPE_INFO,
                getString(R.string.profile_title_username),
                data.getName(),
                true,
                item -> openEditNameDialog(item.getContent())
            ));
            items.add(new ProfileItem(
                ProfileItem.TYPE_INFO,
                getString(R.string.profile_title_last_login),
                OffsetDateTimeFormater.format(data.getLastLogin(), getString(R.string.date_format_str))
            ));
            items.add(new ProfileItem(
                ProfileItem.TYPE_INFO,
                getString(R.string.profile_title_user_permission),
                getUserRoleString(data.getRoles())));
            /*items.add(new ProfileItem(
                ProfileItem.TYPE_INFO,
                getString(R.string.profile_title_user_permission),
                getUserRoleString(data.getRoles()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .toArray())
            ));

            // 管理员功能
            // TODO: 使用新方式实现
            if (isAdminUser(data.getRoles()
                .stream()
                .mapToInt(Integer::intValue)
                .toArray())) {
                items.add(new ProfileItem(
                    ProfileItem.TYPE_PREFERENCE_ITEM,
                    getString(R.string.profile_title_admin_mode),
                    null,
                    true,
                    item -> {
                        Toast.makeText(
                            UserProfileActivity.this,
                            R.string.admin_mode_notice,
                            Toast.LENGTH_SHORT
                        ).show();
                        Intent intent = new Intent(
                            UserProfileActivity.this,
                            AdminModeActivity.class
                        );
                        startActivity(intent);
                    }
                ));
            }*/
            items.add(new ProfileItem(
                ProfileItem.TYPE_BUTTON,
                getString(R.string.profile_title_logout),
                "",
                true,
                item -> logout()
            ));
            adapter.setItems(items);
        } else {
            // 只在登录时才显示错误对话框
            if (userViewModel.isLoggedIn()) {
                errorDialog = new MaterialAlertDialogBuilder(UserProfileActivity.this)
                    .setTitle(R.string.profile_dialog_err_title)
                    .setMessage(getString(R.string.profile_dialog_err_msg))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    }).create();
                runOnUiThread(() -> {
                    if (errorDialog != null && !errorDialog.isShowing()) {
                        errorDialog.show();
                    }
                });
            }
        }
    }

    private boolean isAdminUser(int[] rolesList) {
        if (rolesList == null) {
            return false;
        }
        for (int role : rolesList) {
            if (role == 1) {
                return true;
            }
        }
        return false;
    }

    private String getUserRoleString(List<UserRole> rolesList) {
        if (rolesList == null || rolesList.isEmpty()) {
            return getString(R.string.profile_label_unset);
        }

        String[] roleNames = getResources().getStringArray(R.array.user_roles);
        StringBuilder sb = new StringBuilder();

        for (UserRole role : rolesList) {
            if (role != null && role.getIndex() > 0 && role.getIndex() <= roleNames.length) {
                // 用户权限索引从1开始，数组索引从0开始
                sb.append(roleNames[role.getIndex() - 1]).append(", ");
            }
        }

        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : getString(R.string.profile_label_unset);
    }

    private void openImagePicker() {
        // TODO: 待实现
        pickAvatar.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build());
    }

    private boolean isValidImageType(String mimeType) {
        return "image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType) || "image/png".equals(
            mimeType);
    }

    private void openEditNameDialog(String name) {
        // TODO: 待实现
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            uploadAvatar(UCrop.getOutput(data));
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(
                UserProfileActivity.this,
                R.string.toast_profile_err_crop_avatar,
                Toast.LENGTH_SHORT
            ).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadAvatar(Uri uri) {
        if (uri != null) {
            Logs.d(TAG, "Selected URI: " + uri);

            try {
                userViewModel.uploadAvatar(uri, getContentResolver());

                userViewModel.getSuccess().observe(this, success -> {
                    if (success != null && success) {
                        Toast.makeText(
                            UserProfileActivity.this,
                            R.string.toast_profile_upload_avatar_succ,
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                });

                userViewModel.getError().observe(this, error -> {
                    if (error != null) {
                        Logs.e(TAG, error);
                        Toast.makeText(
                            UserProfileActivity.this,
                            getString(R.string.toast_profile_upload_avatar_err) + "\n" + error,
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            } catch (Exception e) {
                Logs.e(TAG, "Error creating file from Uri: " + e.getMessage());
                Toast.makeText(
                    UserProfileActivity.this,
                    R.string.toast_profile_upload_avatar_err,
                    Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private String getAvatarUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return Constants.API_STATIC_IMAGE_URL + fileName;
    }

    private void logout() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.profile_title_logout);
        builder.setMessage(R.string.profile_title_logout_content);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            // Set up observers for logout
            userViewModel.getSuccess().observe(this, success -> {
                if (success != null && success) {
                    Toast.makeText(
                        UserProfileActivity.this,
                        R.string.toast_profile_logout_succ,
                        Toast.LENGTH_SHORT
                    ).show();

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    finish();
                }
            });

            userViewModel.getError().observe(this, error -> {
                if (error != null) {
                    Logs.e(TAG, "failed to logout, msg: " + error);
                    Toast.makeText(
                        UserProfileActivity.this,
                        R.string.toast_profile_logout_err,
                        Toast.LENGTH_SHORT
                    ).show();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Execute logout
            userViewModel.logout();
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) ->
            dialog.dismiss());
        AlertDialog dialog = builder.create();
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
            View view;
            RecyclerView.ViewHolder holder;
            switch (viewType) {
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
                AvatarViewHolder avatarHolder = (AvatarViewHolder) holder;
                Glide.with(avatarHolder.itemView.getContext())
                    .load(item.getContent())
                    .placeholder(R.drawable.default_avatar)
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
            } else if (holder instanceof PreferenceViewHolder) {
                PreferenceViewHolder preferenceHolder = (PreferenceViewHolder) holder;
                preferenceHolder.titleTextView.setText(item.getTitle());
                if (TextUtils.isEmpty(item.getContent())) {
                    preferenceHolder.contentTextView.setVisibility(View.GONE);
                } else {
                    preferenceHolder.contentTextView.setVisibility(View.VISIBLE);
                    preferenceHolder.contentTextView.setText(item.getContent());
                }
                preferenceHolder.itemView.setBackgroundResource(R.drawable.bg_preference_item);
                preferenceHolder.itemView.setOnClickListener(v -> item.getClickListener()
                    .onItemClick(item));
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

        class PreferenceViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView contentTextView;

            PreferenceViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titlePrefTextView);
                contentTextView = itemView.findViewById(R.id.contentPrefTextView);
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
