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
import net.hearnsoft.tcm.ui.widgets.UserCardView;

public class AccountFragment extends Fragment {

    private static final String TAG = AccountFragment.class.getSimpleName();
    private FragmentAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUserCard();
    }

    private void initUserCard() {
        UserCardView userCard = new UserCardView(requireContext());
        // 测试信息设置，非实际用户 Yuyuko1024 add code start
        // 上线时需移除该测试代码为实际代码
        userCard.setUserAvatarFromRes(R.drawable.test_avatar);
        userCard.setUserName("测试用户ABC");
        userCard.setUserDescription("大地に咲く旋律");
        // Yuyuko1024 add code end
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

    }

}
