package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.api.APICore;
import net.hearnsoft.tcm.api.UserAPI;
import net.hearnsoft.tcm.databinding.FragmentUserLoginBinding;
import net.hearnsoft.tcm.misc.UserLoginType;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.UserLoginPortal;

public class UserLoginFragment extends Fragment {

    private static final String TAG = UserLoginFragment.class.getSimpleName();
    private FragmentUserLoginBinding binding;
    private UserLoginPortal portal;
    private UserAPI userAPI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        userAPI = UserAPI.getInstance(requireContext());
        binding = FragmentUserLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            portal = (UserLoginPortal) requireActivity();
        } catch (ClassCastException e) {
            Logs.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.loginHeaderTips.setMovementMethod(LinkMovementMethod.getInstance());
        binding.userRegister.setOnClickListener(v -> {
            portal.switchPages(1);
            portal.setActivityTitle(requireContext().getString(R.string.activity_user_register_title));
        });
        binding.userLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        if (!username.isEmpty() && !password.isEmpty()) {
            binding.userLogin.setEnabled(false);
            userAPI.login(username, password, new APICore.SessionTokenCallback<String>() {
                @Override
                public void onSuccess(String data, String sessionToken) {
                    Snackbar.make(binding.getRoot(), data + "，登录成功" + "\n" + sessionToken
                            , Snackbar.LENGTH_SHORT).show();
                    binding.userLogin.setEnabled(true);
                    ((UserLoginActivity) requireActivity()).onLoginSuccess(sessionToken,
                            UserLoginType.LOGIN);
                }

                @Override
                public void onSuccess(String data) {
                    //empty stub
                }

                @Override
                public void onError(APICore.ApiError error) {
                    Snackbar.make(binding.getRoot(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                    Logs.e(TAG, error.getMessage());
                    binding.userLogin.setEnabled(true);
                }
            });
        }
    }

}
