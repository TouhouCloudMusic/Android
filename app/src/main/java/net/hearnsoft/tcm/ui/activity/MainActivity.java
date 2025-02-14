package net.hearnsoft.tcm.ui.activity;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import net.hearnsoft.tcm.BuildConfig;
import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.api.APICore;
import net.hearnsoft.tcm.api.UserAPI;
import net.hearnsoft.tcm.beans.UserProfile;
import net.hearnsoft.tcm.databinding.ActivityMainBinding;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.fragments.AccountFragment;
import net.hearnsoft.tcm.ui.fragments.ExploreFragment;
import net.hearnsoft.tcm.ui.fragments.LibraryFragment;
import net.hearnsoft.tcm.ui.fragments.RadioFragment;
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private AppViewPagerAdapter adapter;
    private UserAPI userAPI;
    private BroadcastReceiver refreshReceiver;
    private boolean isAccountPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) { Logs.d(TAG, "onApp: MainActivity: onCreate:"); }
        //设置Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });
        setupBroadcast();
        adapter = new AppViewPagerAdapter(this);
        setSupportActionBar(binding.topAppbar);
        userAPI = UserAPI.getInstance(this);
        initPager();
        initNavBar();
        loadAvatar();
    }

    private void initPager() {
        binding.mainView.setAdapter(adapter);
        // 添加Fragments
        adapter.addFragment(new ExploreFragment());
        adapter.addFragment(new RadioFragment());
        adapter.addFragment(new LibraryFragment());
        adapter.addFragment(new AccountFragment());
        // ViewPager2属性设置
        // 设置当前页面以及是否启用丝滑滚动
        binding.mainView.setCurrentItem(0,true);
        // 设置是否启用用户切换手势
        binding.mainView.setUserInputEnabled(false);
        // 设置onPageChangeCallback
        binding.mainView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                isAccountPage = (position == 3);
                binding.toolbarProfile.setVisibility(isAccountPage ? View.GONE : View.VISIBLE);
                binding.navBar.getMenu().getItem(position).setChecked(true);
                updateToolbarTitle(position);
            }
        });

        binding.toolbarProfile.setOnClickListener(v -> {
            binding.mainView.setCurrentItem(3, true);
        });
    }

    private void setupBroadcast() {
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadAvatar();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshReceiver,
                new IntentFilter("net.hearnsoft.tcm.ACTION_REFRESH_USER_PROFILE"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            // 处理设置菜单点击事件
            return true;
        } else if (item.getItemId() == R.id.menu_search) {
            View searchMenuView = findViewById(R.id.menu_search);

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    searchMenuView,
                    "search_transition"
            );

            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent, options.toBundle());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initNavBar() {
        binding.navBar.setOnItemSelectedListener(menuItem -> {
            int position = 0;
            if (menuItem.getItemId() == R.id.nav_explore) {
                position = 0;
            } else if (menuItem.getItemId() == R.id.nav_radio) {
                position = 1;
            } else if (menuItem.getItemId() == R.id.nav_music_library) {
                position = 2;
            } else if (menuItem.getItemId() == R.id.nav_account) {
                position = 3;
            }
            binding.mainView.setCurrentItem(position, true);
            updateToolbarTitle(position); // 更新标题
            return true;
        });
    }

    private void updateToolbarTitle(int position) {
        switch (position) {
            case 0:
                binding.topAppbar.setTitle(R.string.nav_explore_title);
                break;
            case 1:
                binding.topAppbar.setTitle(R.string.nav_radio_title);
                break;
            case 2:
                binding.topAppbar.setTitle(R.string.nav_library_title);
                break;
            case 3:
                binding.topAppbar.setTitle(R.string.nav_account_title);
                break;
        }
    }

    private void loadAvatar() {
        String token = SettingsPrefUtils.getInstance(this)
                .readStringSettings(Constants.KEY_USER_TOKEN);
        if (!TextUtils.isEmpty(token)) {
            userAPI.getUserProfile(token, new APICore.APICallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile data) {
                    Glide.with(MainActivity.this)
                            .load(getAvatarUrl(data.getAvatar_url()))
                            .placeholder(R.drawable.test_avatar)
                            .into(binding.toolbarProfile);
                }

                @Override
                public void onError(APICore.ApiError error) {
                    Logs.e(TAG, "loadAvatarErr: " + error.getMessage());
                }
            });
        }
    }

    private String getAvatarUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return Constants.API_STATIC_IMAGE_URL + fileName;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshReceiver);
    }
}