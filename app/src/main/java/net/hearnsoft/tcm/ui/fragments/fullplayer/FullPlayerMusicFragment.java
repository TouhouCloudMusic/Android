package net.hearnsoft.tcm.ui.fragments.fullplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;

import net.hearnsoft.tcm.databinding.FragmentFullPlayerMusicBinding;
import net.hearnsoft.tcm.databinding.FullPlayerMusicInfoBinding;

public class FullPlayerMusicFragment extends Fragment {
    private FragmentFullPlayerMusicBinding binding;
    private FullPlayerMusicInfoBinding bindingMusicInfo;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFullPlayerMusicBinding.inflate(inflater, container, false);
        bindingMusicInfo = binding.fullPlayerMusicInfo;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindingMusicInfo.fullPlayerMusicTagsAdd.setOnClickListener(v -> {
            Chip chip = new Chip(requireContext());
            chip.setText("baka");
            bindingMusicInfo.fullPlayerMusicTagsGroup.addView(chip);
        });
    }
}
