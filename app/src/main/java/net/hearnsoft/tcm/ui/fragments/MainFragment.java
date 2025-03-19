package net.hearnsoft.tcm.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentMainBinding;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private AppViewPagerAdapter adapter;
    private boolean isAccountPage = false;

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
}
