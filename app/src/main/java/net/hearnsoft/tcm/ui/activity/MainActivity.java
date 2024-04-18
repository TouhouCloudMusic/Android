package net.hearnsoft.tcm.ui.activity;

import android.os.Bundle;
import android.util.Log;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private AppViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) { Log.d(TAG, "onApp: MainActivity: onCreate:"); }
        //设置Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            Log.d(TAG, "cutout.top:" + cutout.top
                    + ",statusBar.top:" + statusBar.top);
            //对于没有屏幕切口的设备而言，无需设置切口padding
            if (cutout.top != 0) {
                v.setPadding(0, cutout.top, 0, 0);
            } else if (cutout.top == 0 && statusBar.top != 0) {
                v.setPadding(0, statusBar.top, 0, 0);
            }
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
        binding.mainView.setUserInputEnabled(true);
        // 设置onPageChangeCallback
        binding.mainView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.navBar.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private void initNavBar() {
        binding.navBar.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.nav_explore) {
                binding.mainView.setCurrentItem(0, true);
            } else if (menuItem.getItemId() == R.id.nav_radio) {
                binding.mainView.setCurrentItem(1, true);
            } else if (menuItem.getItemId() == R.id.nav_music_library) {
                binding.mainView.setCurrentItem(2, true);
            } else if (menuItem.getItemId() == R.id.nav_account) {
                binding.mainView.setCurrentItem(3, true);
            }
            return true;
        });
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