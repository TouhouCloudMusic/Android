package net.hearnsoft.tcm.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentAccountBinding;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.ui.activity.UserHomePageActivity;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;
import net.hearnsoft.tcm.ui.activity.UserProfileActivity;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.UserCardView;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.OffsetDateTimeFormater;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;
import net.hearnsoft.tcm.utils.ViewModelUtils;

import net.hearnsoft.thcdb_sdk.model.UserProfile;

public class AccountFragment extends Fragment {

    private static final String TAG = AccountFragment.class.getSimpleName();
    private FragmentAccountBinding binding;
    private UserCardView userCard;
    private UserViewModel userViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get ViewModel instance
        userViewModel = ViewModelUtils.getViewModel(requireActivity(), UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUserCard();

        // Set up observers for the shared LiveData
        userViewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateUserProfile);
        userViewModel.getError().observe(getViewLifecycleOwner(), this::handleError);

        // Check if we need to refresh the profile
        if (userViewModel.getUserProfile().getValue() == null && userViewModel.isLoggedIn()) {
            userViewModel.getCurrentUserProfile();
        } else {
            // Use cached profile if available
            UserProfile cachedProfile = userViewModel.getUserProfile().getValue();
            if (cachedProfile != null) {
                updateUserProfile(cachedProfile);
            } else {
                setUserCardForLoggedOutState();
            }
        }
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only refresh if necessary (e.g., after logout)
        if (userViewModel.getUserProfile().getValue() == null && userViewModel.isLoggedIn()) {
            userViewModel.getCurrentUserProfile();
        }
    }

    private void updateUserProfile(UserProfile data) {
        if (data == null || !isAdded()) {
            setUserCardForLoggedOutState();
            return;
        }

        String lastLoginDate = getString(
            R.string.usercard_last_login_desc,
            OffsetDateTimeFormater.format(data.getLastLogin(), getString(R.string.date_format_str))
        );
        setUserCardInfo(data.getName(), lastLoginDate, null);
        userCard.setOnUserCardClickListener(v -> {
            Intent userHomePage = new Intent(getContext(), UserHomePageActivity.class);
            startActivity(userHomePage);
        });
        userCard.setUserCardEditClickListener(v -> openUserProfile());
        userCard.setUserAvatarFromUrl(getAvatarUrl(data.getAvatarUrl()));
        userCard.setUserCardBackgroundFromUrl(getAvatarUrl(data.getAvatarUrl()));
    }

    private void handleError(String errorMessage) {
        Logs.e(TAG, "refreshUserProfile error: " + errorMessage);
        setUserCardForLoggedOutState();
    }

    private String getAvatarUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return Constants.API_STATIC_IMAGE_URL + fileName;
    }

    private void setUserCardForLoggedOutState() {
        if (!isAdded()) return;

        setUserCardInfo(
            getString(R.string.usercard_default_name),
            getString(R.string.usercard_default_desc),
            null
        );

        userCard.setOnUserCardClickListener(v -> {
            Intent loginPage = new Intent(getContext(), UserLoginActivity.class);
            getContext().startActivity(loginPage);
        });
        userCard.setUserCardEditClickListener(v -> {
            // empty click
        });
    }

    private void setUserCardInfo(String userName, String userDesc, String userAvatarUrl) {
        if (userCard != null) {
            userCard.setUserName(TextUtils.isEmpty(userName) ? getString(R.string.usercard_default_name) : userName);
            userCard.setUserDescription(TextUtils.isEmpty(userDesc) ? getString(R.string.usercard_default_desc) : userDesc);
            if (TextUtils.isEmpty(userAvatarUrl)) {
                userCard.setUserAvatarFromRes(R.drawable.default_avatar);
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
