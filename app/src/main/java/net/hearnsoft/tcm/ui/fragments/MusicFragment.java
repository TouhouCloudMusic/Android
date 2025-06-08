package net.hearnsoft.tcm.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentMusicBinding;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.fragments.music.MusicListFragment;

@UnstableApi
public class MusicFragment extends Fragment {
    private FragmentMusicBinding binding;
    private AppViewPagerAdapter adapter;
    private int currentTabPosition = 0;

    private static int[] tabs = {
            R.string.music_tab_music_title,
            R.string.music_tab_artists_title,
            R.string.music_tab_album_title,
            R.string.music_tab_playlist_title,
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            outState.putInt("selected_tab", binding.musicTab.getSelectedTabPosition());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            currentTabPosition = savedInstanceState.getInt("selected_tab", 0);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);
        adapter = AppViewPagerAdapter.create(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTabs();
        initPager();
    }

    private void initTabs() {
        for (int tab : tabs) {
            TabLayout.Tab musicTab = binding.musicTab.newTab();
            musicTab.setText(tab);
            binding.musicTab.addTab(musicTab);
        }
        binding.musicTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.musicViewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initPager() {
        binding.musicViewPager.setAdapter(adapter);

        if (adapter.getItemCount() == 0) {
            adapter.addFragment(new MusicListFragment());
            adapter.addFragment(new Fragment());
            adapter.addFragment(new Fragment());
            adapter.addFragment(new Fragment());
        }

        binding.musicViewPager.setCurrentItem(currentTabPosition,true);
        binding.musicViewPager.setUserInputEnabled(true);
        binding.musicViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.musicTab.getTabAt(position).select();
            }
        });

        binding.musicTab.getTabAt(currentTabPosition).select();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
