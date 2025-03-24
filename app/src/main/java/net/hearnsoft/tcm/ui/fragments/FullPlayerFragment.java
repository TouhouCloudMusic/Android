package net.hearnsoft.tcm.ui.fragments;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FullPlayerBinding;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.fragments.fullplayer.FullPlayerInfoFragment;
import net.hearnsoft.tcm.ui.fragments.fullplayer.FullPlayerMusicFragment;
import net.hearnsoft.tcm.ui.fragments.fullplayer.FullPlayerLyricsFragment;
import net.hearnsoft.tcm.utils.Logs;

public class FullPlayerFragment extends Fragment {
    private FullPlayerBinding binding;
    private static AppViewPagerAdapter adapter;
    private static final int[] fullPlayerTabs = {
            R.string.full_player_tabs_lyrics,
            R.string.full_player_tabs_music,
            R.string.full_player_tabs_info
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FullPlayerBinding.inflate(inflater, container, false);
        adapter = new AppViewPagerAdapter(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBar.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());

        binding.fullPlayerToolbar.setNavigationOnClickListener(v -> {
            Logs.d("FullPlayerFragment", "onViewCreated");
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        initFullPlayerTabs();
        initFullPlayerViewPager();
    }

    private void initFullPlayerTabs() {
        for (int fullPlayerTab : fullPlayerTabs) {
            TabLayout.Tab tab = binding.fullPlayerToolbarTabsContainer.newTab();
            tab.setText(fullPlayerTab);
            binding.fullPlayerToolbarTabsContainer.addTab(tab);
        }
        binding.fullPlayerToolbarTabsContainer.setTabMode(TabLayout.MODE_FIXED);
        binding.fullPlayerToolbarTabsContainer.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.fullPlayerPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.fullPlayerToolbarTabsContainer.getTabAt(1).select();
    }

    private void initFullPlayerViewPager() {
        binding.fullPlayerPager.setAdapter(adapter);
        adapter.addFragment(new FullPlayerLyricsFragment());
        adapter.addFragment(new FullPlayerMusicFragment());
        adapter.addFragment(new FullPlayerInfoFragment());
        binding.fullPlayerPager.setUserInputEnabled(true);
        binding.fullPlayerPager.setCurrentItem(1, true);
        binding.fullPlayerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.fullPlayerToolbarTabsContainer.getTabAt(position).select();
            }
        });
    }
}
