package net.hearnsoft.tcm.ui.fragments;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentAccountBinding;
import net.hearnsoft.tcm.databinding.UserCardBinding;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;

public class AccountFragment extends Fragment {

    private static final String TAG = AccountFragment.class.getSimpleName();
    private FragmentAccountBinding binding;
    private UserCardBinding userCardBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        userCardBinding = UserCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUserCard();
    }

    private void initUserCard() {
        MaterialCardView userCard = userCardBinding.getRoot();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = (int) requireContext().getResources()
                .getDimension(R.dimen.account_cardview_margin);
        params.rightMargin = (int) requireContext().getResources()
                .getDimension(R.dimen.account_cardview_margin);
        params.topMargin = (int) requireContext().getResources()
                .getDimension(R.dimen.account_cardview_margin);
        params.bottomMargin = (int) requireContext().getResources()
                .getDimension(R.dimen.account_cardview_margin);

        userCard.setLayoutParams(params);
        binding.getRoot().addView(userCard, 0);

        userCardBinding.userCardBackground.setImageResource(R.drawable.test_res2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            userCardBinding.userCardBackground
                    .setRenderEffect(RenderEffect.createBlurEffect(
                            150F,
                            150F,
                            Shader.TileMode.DECAL)
                    );
        }

        userCardBinding.userAvatar.setOnClickListener(v -> {
            Intent loginPage = new Intent(requireActivity(), UserLoginActivity.class);
            startActivity(loginPage);
        });
    }

}
