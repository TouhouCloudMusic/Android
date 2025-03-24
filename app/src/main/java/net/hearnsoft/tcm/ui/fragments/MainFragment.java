package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentMainBinding;
import net.hearnsoft.tcm.ui.activity.NewMainActivity;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.interfaces.OnNowPlayingClickListener;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private AppViewPagerAdapter adapter;
    private OnNowPlayingClickListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NewMainActivity) {
            listener = ((NewMainActivity) context).getNowPlayingClickListener();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        adapter = new AppViewPagerAdapter(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());

        initPager();
        initNavBar();
        binding.nowPlayingBar.setVisibility(View.VISIBLE);
        binding.nowPlayingBar.setOnPlayingBarClickListener(v -> {listener.onNowPlayingClick();});
    }

    private void initPager() {
        binding.mainView.setAdapter(adapter);
        // 添加Fragments
        adapter.addFragment(new ExploreFragment());
        adapter.addFragment(new RadioFragment());
        adapter.addFragment(new LibraryFragment());
        adapter.addFragment(new MusicFragment());
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
            }
        });
    }

    private void initNavBar() {
        binding.navBar.setOnItemSelectedListener(menuItem -> {
            int position = 0;
            if (menuItem.getItemId() == R.id.nav_explore) {
                position = 0;
            } else if (menuItem.getItemId() == R.id.nav_music_library) {
                position = 1;
            } else if (menuItem.getItemId() == R.id.nav_statistics) {
                position = 2;
            } else if (menuItem.getItemId() == R.id.nav_music) {
                position = 3;
            } else if (menuItem.getItemId() == R.id.nav_account) {
                position = 4;
            }
            binding.mainView.setCurrentItem(position, true);
            return true;
        });
    }
}
