package net.hearnsoft.tcm.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import net.hearnsoft.tcm.BuildConfig;
import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ActivityMainBinding;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.fragments.AccountFragment;
import net.hearnsoft.tcm.ui.fragments.ExploreFragment;
import net.hearnsoft.tcm.ui.fragments.LibraryFragment;
import net.hearnsoft.tcm.ui.fragments.RadioFragment;
import net.hearnsoft.tcm.utils.Logs;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private AppViewPagerAdapter adapter;

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
        adapter = new AppViewPagerAdapter(this);
        initPager();
        initNavBar();
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
                binding.navBar.getMenu().getItem(position).setChecked(true);
                updateToolbarTitle(position);
            }
        });
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}