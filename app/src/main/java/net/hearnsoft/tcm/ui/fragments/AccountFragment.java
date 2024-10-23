package net.hearnsoft.tcm.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.card.MaterialCardView;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.api.APICore;
import net.hearnsoft.tcm.api.UserAPI;
import net.hearnsoft.tcm.beans.UserProfile;
import net.hearnsoft.tcm.databinding.FragmentAccountBinding;
import net.hearnsoft.tcm.databinding.UserCardBinding;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;
import net.hearnsoft.tcm.ui.activity.UserProfileActivity;
import net.hearnsoft.tcm.ui.widgets.UserCardView;
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;

public class AccountFragment extends Fragment {

    private static final String TAG = AccountFragment.class.getSimpleName();
    private FragmentAccountBinding binding;
    private UserCardView userCard;
    private BroadcastReceiver refreshReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshUserProfile();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshReceiver,
                new IntentFilter("net.hearnsoft.tcm.ACTION_REFRESH_USER_PROFILE"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshReceiver);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUserCard();
    }

    private void initUserCard() {
        userCard = new UserCardView(requireContext());
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

        // 测试信息设置，非实际用户 Yuyuko1024 add code start
        // 上线时需移除该测试代码为实际代码
        refreshUserProfile();
        // Yuyuko1024 add code end
    }

    private void refreshUserProfile() {
        Log.d(TAG, "refreshUserProfile: ");
        String userToken = SettingsPrefUtils.getInstance(requireContext())
                .readStringSettings(Constants.KEY_USER_TOKEN);
        if (TextUtils.isEmpty(userToken)) {
            setUserCardForLoggedOutState();
        } else {
            UserAPI.getInstance(requireContext())
                    .getUserProfile(userToken, new APICore.APICallback<UserProfile>() {
                        @Override
                        public void onSuccess(UserProfile data) {
                            setUserCardInfo(data.getName(),
                                    data.getCreated_at().toString(),
                                    null);
                            userCard.setUserCardEditClickListener(v -> openUserProfile());
                            Toast.makeText(requireContext(), "刷新用户信息成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(APICore.ApiError error) {
                            setUserCardForLoggedOutState();
                        }
                    });
        }
    }

    private void setUserCardForLoggedOutState() {
        setUserCardInfo("点击登录", "大地に咲く旋律", null);
        userCard.setOnUserCardClickListener(v -> {
            Intent loginPage = new Intent(getContext(), UserLoginActivity.class);
            getContext().startActivity(loginPage);
        });
    }

    private void setUserCardInfo(String userName,
                                 String userDesc, String userAvatarUrl){
        if (userCard != null) {
            userCard.setUserName(TextUtils.isEmpty(userName) ? "点击登录" : userName);
            userCard.setUserDescription(TextUtils.isEmpty(userDesc) ? "大地に咲く旋律" : userDesc);
            if (TextUtils.isEmpty(userAvatarUrl)) {
                userCard.setUserAvatarFromRes(R.drawable.test_avatar);
            } else {
                userCard.setUserAvatarFromUrl(userAvatarUrl);
            }
        }
    }

    private void openUserProfile() {
        Intent profilePage = new Intent(getContext(), UserProfileActivity.class);
        requireContext().startActivity(profilePage);
    }

}
