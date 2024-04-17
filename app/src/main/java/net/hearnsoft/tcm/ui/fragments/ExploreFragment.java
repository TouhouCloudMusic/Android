package net.hearnsoft.tcm.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zhpan.bannerview.BannerViewPager;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.beans.BannerDataBean;
import net.hearnsoft.tcm.databinding.FragmentExploreBinding;
import net.hearnsoft.tcm.ui.adapter.AppBannerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private static final String TAG = ExploreFragment.class.getSimpleName();
    private FragmentExploreBinding binding;
    private BannerViewPager<BannerDataBean> mBanner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setUpBanner();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpBanner() {
        mBanner = requireView().findViewById(R.id.explore_banner);
        List<BannerDataBean> list = new ArrayList<>();
        list.add(new BannerDataBean(R.drawable.test_res1, "https://toho.moe/tbTHVFSS",
                "Test Resource 1"));
        list.add(new BannerDataBean(R.drawable.test_res2, "https://toho.moe/tbTHZDH",
                "Test Resource 2"));
        mBanner.setLifecycleRegistry(getLifecycle())
                .setAdapter(new AppBannerAdapter())
                .setAutoPlay(true)
                .create();
        mBanner.refreshData(list);
    }
}
