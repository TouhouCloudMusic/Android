package net.hearnsoft.tcm.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yalantis.ucrop.UCrop;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ActivityUserProfileBinding;
import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.ui.adapter.UserProfileAdapter;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;
import net.hearnsoft.tcm.ui.widgets.userprofile.AdminComponent;
import net.hearnsoft.tcm.ui.widgets.userprofile.AvatarComponent;
import net.hearnsoft.tcm.ui.widgets.userprofile.BannerComponent;
import net.hearnsoft.tcm.ui.widgets.userprofile.LogoutComponent;
import net.hearnsoft.tcm.ui.widgets.userprofile.ProfileComponentManager;
import net.hearnsoft.tcm.ui.widgets.userprofile.UserInfoComponent;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.ViewModelUtils;

import java.io.File;
import java.util.List;

public class UserProfileActivity extends BaseActivity implements
    AvatarComponent.AvatarCallback,
    UserInfoComponent.UserInfoCallback,
    LogoutComponent.LogoutCallback,
    BannerComponent.BannerCallback {

    private static final String tempAvatarImageName = "avatar.jpg";
    private static final String tempBannerImageName = "banner.jpg";
    private static final int REQUEST_FROM_AVATAR = 0;
    private static final int REQUEST_FROM_BANNER = 1;

    private final String TAG = this.getClass().getSimpleName();
    private ActivityUserProfileBinding binding;
    private UserViewModel userViewModel;
    private UserProfileAdapter adapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickAvatar, pickBanner;
    private AlertDialog errorDialog;
    private boolean isEditMode = false;

    // 组件管理
    private ProfileComponentManager componentManager;

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

        // 初始化Toolbar
        setSupportActionBar(binding.topAppbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProfileAdapter();
        binding.recyclerView.setAdapter(adapter);

        // 初始化图片选择器回调
        setupAvatarPicker();
        // 设置Banner图片选择器
        setupBannerPicker();

        // 初始化组件管理器
        setupComponentManager();
        // 载入用户资料
        loadUserProfile();
    }


    private void setupComponentManager() {
        componentManager = new ProfileComponentManager(this);

        // 添加各个组件
        componentManager.addComponent(new BannerComponent(this, userViewModel, pickBanner, this));
        componentManager.addComponent(new AvatarComponent(this, userViewModel, pickAvatar, this));
        componentManager.addComponent(new UserInfoComponent(this, userViewModel, this));
        componentManager.addComponent(new AdminComponent(this, userViewModel));
        componentManager.addComponent(new LogoutComponent(this, userViewModel, this));
    }

    private void setupAvatarPicker() {
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
                            .start(UserProfileActivity.this, REQUEST_FROM_AVATAR);
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
    }

    private void setupBannerPicker() {
        pickBanner = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    String mimeType = getContentResolver().getType(uri);
                    if (isValidImageType(mimeType)) {
                        UCrop.Options options = new UCrop.Options();
                        // 图片格式
                        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                        // 设置图片压缩质量
                        options.setCompressionQuality(100);
                        UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), tempBannerImageName)))
                            .withAspectRatio(3, 1)
                            .withMaxResultSize(1500, 500)
                            .withOptions(options)
                            .start(UserProfileActivity.this, REQUEST_FROM_BANNER);
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
    }

    private void loadUserProfile() {
        // Set up observers
        userViewModel.getUserProfile().observe(this, this::setupUserProfileList);
    }

    private void setupUserProfileList(UserProfileModel data) {
        if (data != null) {
            adapter.setItems(componentManager.getAllItems());
        } else {
            // 只在登录时才显示错误对话框
            if (userViewModel.isLoggedIn()) {
                showErrorDialog(getString(R.string.profile_dialog_err_msg));
            }
        }
    }

    private void showErrorDialog(String error) {
        errorDialog = new MaterialAlertDialogBuilder(UserProfileActivity.this)
            .setTitle(R.string.profile_dialog_err_title)
            .setMessage(getString(R.string.profile_dialog_err_msg) + "\n" + error)
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

    private boolean isValidImageType(String mimeType) {
        return "image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType) || "image/png".equals(
            mimeType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FROM_AVATAR) {
                uploadAvatar(UCrop.getOutput(data));
            } else if (requestCode == REQUEST_FROM_BANNER) {
                uploadBanner(UCrop.getOutput(data));
            }
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
                userViewModel.uploadAvatar(uri, getContentResolver())
                    .thenApply(result -> {
                        if (result.isSuccess()) {
                            Toast.makeText(
                                UserProfileActivity.this,
                                R.string.toast_profile_upload_avatar_succ,
                                Toast.LENGTH_SHORT
                            ).show();
                            // 上传完成后刷新用户信息
                            userViewModel.loadCurrentUserProfile();
                        } else {
                            Logs.e(TAG, result.getError());
                            Toast.makeText(
                                UserProfileActivity.this,
                                getString(R.string.toast_profile_upload_avatar_err) + "\n" + result.getError(),
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                        return result;
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

    private void uploadBanner(Uri uri) {
        if (uri != null) {
            Logs.d(TAG, "Selected URI: " + uri);

            try {
                userViewModel.uploadProfileBanner(uri, getContentResolver())
                    .thenAccept(result -> {
                        if (result.isSuccess()) {
                            Toast.makeText(
                                UserProfileActivity.this,
                                R.string.toast_profile_upload_banner_succ,
                                Toast.LENGTH_SHORT
                            ).show();
                            // 上传完成后刷新用户信息
                            userViewModel.loadCurrentUserProfile();
                        } else {
                            Logs.e(TAG, result.getError());
                            Toast.makeText(
                                UserProfileActivity.this,
                                getString(R.string.toast_profile_upload_banner_err) + "\n" + result.getError(),
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
            } catch (Exception e) {
                Logs.e(TAG, "Error creating file from Uri: " + e.getMessage());
                Toast.makeText(
                    UserProfileActivity.this,
                    R.string.toast_profile_upload_banner_err,
                    Toast.LENGTH_SHORT
                ).show();
            }
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

        List<ProfileItem> updatedItems = componentManager.updateEditMode(isEditMode);
        adapter.setEditMode(isEditMode);
        adapter.setItems(updatedItems);

        updateEditModeUI();
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

    @Override
    public void onPickAvatarImage(Uri uri) {
        pickAvatar.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build());
    }

    @Override
    public void onPickBannerImage(Uri uri) {
        pickBanner.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build());
    }

    @Override
    public void onLogoutRequested() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.profile_title_logout);
        builder.setMessage(R.string.profile_title_logout_content);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            onLogout();
            dialog.dismiss();
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) ->
            dialog.dismiss());
        AlertDialog dialog = builder.create();
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void onLogout() {
        // Execute logout
        userViewModel.logout().thenAccept(result -> {
            runOnUiThread(() -> {
                if (result.isSuccess()) {
                    Toast.makeText(
                        UserProfileActivity.this,
                        R.string.toast_profile_logout_succ,
                        Toast.LENGTH_SHORT
                    ).show();
                    finish();
                } else {
                    Logs.e(TAG, "failed to logout, msg: " + result.getError());
                    Toast.makeText(
                        UserProfileActivity.this,
                        R.string.toast_profile_logout_err,
                        Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });
    }

    @Override
    public void onEditName(String currentName) {
        // TODO: 待实现
    }

    @Override
    public void onEditBio(String bio) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText editText = new EditText(this);
        editText.setText(bio);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(1);
        editText.setMaxLines(5);
        editText.setGravity(Gravity.TOP);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setPadding(16, 16, 16, 16);
        layout.addView(editText);

        AlertDialog bioEditDialog = new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_title_edit_bio)
            .setView(layout)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                String newBio = editText.getText().toString().trim();
                if (userViewModel != null) {
                    userViewModel.updateBio(newBio).thenAccept(result -> {
                        if (result.isSuccess()) {
                            Toast.makeText(
                                UserProfileActivity.this,
                                R.string.toast_profile_edit_bio_succ,
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                        dialog.dismiss();
                    });
                }
            })
            .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                dialog.dismiss())
            .create();
        if (!bioEditDialog.isShowing()) {
            bioEditDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (componentManager != null) {
            componentManager.clearComponents();
        }
    }
}
