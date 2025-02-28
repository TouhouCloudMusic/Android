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
import net.hearnsoft.tcm.enums.ErrorCode;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;
import net.hearnsoft.tcm.ui.activity.UserProfileActivity;
import net.hearnsoft.tcm.ui.widgets.UserCardView;
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
        if (refreshReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshReceiver,
                    new IntentFilter(Constants.ACTION_REFRESH_USER_PROFILE));
        }
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
        Logs.d(TAG, "refreshUserProfile: ");
        String userToken = SettingsPrefUtils.getInstance(requireContext())
                .readStringSettings(Constants.KEY_USER_TOKEN);
        if (TextUtils.isEmpty(userToken)) {
            setUserCardForLoggedOutState();
        } else {
            UserAPI.getInstance(requireContext())
                    .getUserProfile(userToken, new APICore.APICallback<UserProfile>() {
                        @Override
                        public void onSuccess(UserProfile data) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format_str), Locale.CHINA);
                            String lastLoginDate = getString(R.string.usercard_last_login_desc,
                                    dateFormat.format(data.getLast_login()));
                            setUserCardInfo(data.getName(),
                                    lastLoginDate,
                                    null);
                            userCard.setOnUserCardClickListener(v -> {
                                // empty click
                            });
                            userCard.setUserCardEditClickListener(v -> openUserProfile());
                            userCard.setUserAvatarFromUrl(getAvatarUrl(data.getAvatar_url()));
                            userCard.setUserCardBackgroundFromUrl(
                                    getAvatarUrl(data.getAvatar_url()));
                            Toast.makeText(requireContext(), R.string.toast_refresh_profile_succ,
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(APICore.ApiError error) {
                            Logs.e(TAG, "refreshUserProfile error: " + error.getMessage());
                            if (error.getError_code() == ErrorCode.RiskControlError) {
                                Toast.makeText(requireContext(),
                                        R.string.toast_api_reach_risk_control + error.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                setUserCardForLoggedOutState();
                            }
                        }
                    });
        }
    }

    private String getAvatarUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return Constants.API_STATIC_IMAGE_URL + fileName;
    }

    private void setUserCardForLoggedOutState() {
        setUserCardInfo(getString(R.string.usercard_default_name),
                getString(R.string.usercard_default_desc),  null);
        userCard.setOnUserCardClickListener(v -> {
            Intent loginPage = new Intent(getContext(), UserLoginActivity.class);
            getContext().startActivity(loginPage);
        });
        userCard.setUserCardEditClickListener(v -> {
            // empty click
        });
    }

    private void setUserCardInfo(String userName,
                                 String userDesc, String userAvatarUrl){
        if (userCard != null) {
            userCard.setUserName(TextUtils.isEmpty(userName) ?
                    getString(R.string.usercard_default_name) : userName);
            userCard.setUserDescription(TextUtils.isEmpty(userDesc) ?
                    getString(R.string.usercard_default_desc) : userDesc);
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
