package net.hearnsoft.tcm.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zhpan.bannerview.BannerViewPager;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentExploreBinding;
import net.hearnsoft.tcm.domain.model.song.RecommendSong;
import net.hearnsoft.tcm.infrastructure.model.LocalImage;
import net.hearnsoft.tcm.ui.adapter.AppBannerAdapter;
import net.hearnsoft.tcm.ui.adapter.RecommendSongListAdapter;
import net.hearnsoft.tcm.ui.utils.HorizontalSpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private static final String TAG = ExploreFragment.class.getSimpleName();
    private FragmentExploreBinding binding;
    private BannerViewPager<LocalImage> mBanner;

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
        setUpRecommendSongList();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpBanner() {
        mBanner = requireView().findViewById(R.id.explore_banner);
        List<LocalImage> list = new ArrayList<>();
        list.add(new LocalImage(R.drawable.test_res1, "https://toho.moe/tbTHVFSS",
            "Test Resource 1"));
        list.add(new LocalImage(R.drawable.test_res2, "https://toho.moe/tbTHZDH",
            "Test Resource 2"));
        mBanner.registerLifecycleObserver(getLifecycle())
            .setAdapter(new AppBannerAdapter<>())
            .setAutoPlay(true)
            .create();
        mBanner.refreshData(list);
    }

    private void setUpRecommendSongList() {
        List<RecommendSong> sampleList = new ArrayList<>();
        sampleList.add(new RecommendSong(
            106939,
            "郷愁",
            "カナタノート",
            "https://gensokyoradio.net/images/albums/500/KNTN-0002_f51dcd99c0.png"
        ));
        sampleList.add(new RecommendSong(
            116573,
            "U・R・A",
            "ランコ",
            "https://gensokyoradio.net/images/albums/500/AREZ-0007_f5cc895200.jpg"
        ));
        sampleList.add(new RecommendSong(
            106031,
            "ユア･トリップ★ガールフレンド",
            "めらみぽっぷ",
            "https://gensokyoradio.net/images/albums/500/10611_2hPor8MshZ.png"
        ));
        sampleList.add(new RecommendSong(
            101296,
            "Twinkle Twinkle",
            "KUMI",
            "https://gensokyoradio.net/images/albums/500/AMRC-0011_849f5451d2.jpg"
        ));

        RecommendSongListAdapter adapter = new RecommendSongListAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        );
        binding.exploreRecommendList.setLayoutManager(layoutManager);
        binding.exploreRecommendList.setAdapter(adapter);
        binding.exploreRecommendList.addItemDecoration(new HorizontalSpaceItemDecoration(
            getResources().getDimensionPixelSize(R.dimen.card_spacing)
        ));

        adapter.setOnItemClickListener((item, position) -> {
            handleRecommendClick(item);
        });

        adapter.setData(sampleList);

    }

    private void handleRecommendClick(RecommendSong item) {
        // TODO: Handle click
    }
}
