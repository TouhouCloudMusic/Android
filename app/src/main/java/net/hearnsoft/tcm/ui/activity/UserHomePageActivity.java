package net.hearnsoft.tcm.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.flyjingfish.openimagelib.OpenImage;
import com.flyjingfish.openimagelib.enums.MediaType;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ActivityUserHomepageBinding;
import net.hearnsoft.tcm.databinding.UserHomepageHeaderBinding;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.utils.OffsetDateTimeFormater;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.ViewModelUtils;
import net.hearnsoft.thcdb_sdk.model.UserProfile;

public class UserHomePageActivity extends BaseActivity {
    private static final String TAG = UserHomePageActivity.class.getSimpleName();
    private static final String EXTRA_USERNAME = "username";

    private ActivityUserHomepageBinding binding;
    private UserHomepageHeaderBinding headerBinding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置工具栏
        setSupportActionBar(binding.userHomepageToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 获取头部布局的绑定
        headerBinding = UserHomepageHeaderBinding.bind(binding.userHomePageHeaderContainer.getRoot());

        // 调整 banner 高度
        adjustBannerHeight();

        // 初始化ViewModel
        userViewModel = ViewModelUtils.getViewModel(this, UserViewModel.class);

        // 设置观察者
        setupObservers();

        // 加载用户资料
        loadUserProfile();
    }

    private void setupObservers() {
        // 观察查看的用户资料变化
        userViewModel.getUserProfile().observe(this, this::updateUserProfileUI);

        // 观察错误信息
        userViewModel.getError().observe(this, error -> {
            if (error != null) {
                Logs.e(TAG, "Failed to load user profile: " + error);
                Toast.makeText(this, getString(R.string.profile_dialog_err_msg) + "\n" + error,
                    Toast.LENGTH_LONG).show();
            }
        });

        // 观察加载状态
        userViewModel.isLoading().observe(this, isLoading -> {
            // 可以在这里添加加载指示器
        });
    }

    private void loadUserProfile() {
        // 从Intent获取用户名
        String username = getIntent().getStringExtra(EXTRA_USERNAME);

        if (TextUtils.isEmpty(username)) {
            // 如果没有传入用户名，加载当前登录用户的资料
            userViewModel.loadCurrentUserProfile();
        } else {
            // 加载指定用户名的资料
            userViewModel.getProfileByUsername(username);
        }
    }

    private void updateUserProfileUI(UserProfile profile) {
        if (profile == null) {
            Logs.e(TAG, "Received null user profile");
            finish();
            return;
        }

        // 设置用户头像
        String avatarUrl = getImageUrl(profile.getAvatarUrl());
        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.default_avatar)
            .into(headerBinding.userAvatarImage);

        // 设置用户头像点击事件
        headerBinding.userAvatarImage.setOnClickListener(v -> {
            OpenImage.with(this)
                .setClickImageView(headerBinding.userAvatarImage)
                .setImageUrl(avatarUrl, MediaType.IMAGE)
                .show();
        });

        // 设置背景横幅
        String bannerUrl = getImageUrl(profile.getBannerUrl());
        Glide.with(this)
            .load(bannerUrl)
            .placeholder(R.drawable.test_res2) // 使用默认背景
            .into(headerBinding.userProfileBannerImage);

        // 设置背景横幅点击事件
        headerBinding.userProfileBannerImage.setOnClickListener(v -> {
            OpenImage.with(this)
                .setClickImageView(headerBinding.userProfileBannerImage)
                .setImageUrl(bannerUrl, MediaType.IMAGE)
                .show();
        });

        // 设置用户名称
        headerBinding.userDisplayName.setText(profile.getName());
        getSupportActionBar().setTitle(profile.getName());

        // 设置用户ID（这里显示角色信息）
        String roles = TextUtils.join(", ", profile.getRoles());
        headerBinding.userHandle.setText(roles);

        // 设置用户简介（显示最后登录时间）
        String lastLoginText = getString(R.string.usercard_last_login_desc,
            OffsetDateTimeFormater.format(profile.getLastLogin(),
                getString(R.string.date_format_str)));
        headerBinding.userBio.setText(lastLoginText);

        // 设置关注状态
        headerBinding.followUserButton.setText(R.string.full_player_artist_follow);

        // 只在查看其他用户时显示关注按钮
        String viewingUsername = getIntent().getStringExtra(EXTRA_USERNAME);
        headerBinding.followUserButton.setVisibility(
            !TextUtils.isEmpty(viewingUsername) ?
                android.view.View.VISIBLE : android.view.View.GONE);

        headerBinding.editProfileButton.setOnClickListener(v -> {
            Intent profilePage = new Intent(this, UserProfileActivity.class);
            startActivity(profilePage);
        });

        // 设置页面标题
        getSupportActionBar().setTitle(profile.getName());
    }

    private void adjustBannerHeight() {
        // 获取屏幕高度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        // 计算需要的高度（屏幕高度的五分之二）
        int bannerHeight = screenHeight / 5;

        // 设置 banner 的高度
        ViewGroup.LayoutParams layoutParams = headerBinding.userProfileBannerImage.getLayoutParams();
        layoutParams.height = bannerHeight;
        headerBinding.userProfileBannerImage.setLayoutParams(layoutParams);
    }

    private String getImageUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        return Constants.API_STATIC_IMAGE_URL + fileName;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}