package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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
import net.hearnsoft.tcm.databinding.FragmentUserRegisterBinding;
import net.hearnsoft.tcm.misc.UserLoginType;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.UserLoginPortal;


public class UserRegisterFragment extends Fragment {

    private static final String TAG = UserRegisterFragment.class.getSimpleName();
    private FragmentUserRegisterBinding binding;
    private UserLoginPortal portal;
    private UserAPI userAPI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        userAPI = UserAPI.getInstance(requireContext());
        binding = FragmentUserRegisterBinding.inflate(inflater, container, false);
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
        binding.registerHeaderTips.setMovementMethod(LinkMovementMethod.getInstance());
        binding.userBackLogin.setOnClickListener(v -> {
            portal.switchPages(0);
            portal.setActivityTitle(requireContext().getString(R.string.activity_user_login_title));
        });
        binding.userRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        if (!username.isEmpty() && !password.isEmpty()) {
            binding.userRegister.setEnabled(false);
            userAPI.register(username, password, new APICore.SessionTokenCallback<String>() {
                @Override
                public void onSuccess(String data, String sessionToken) {
                    Snackbar.make(binding.getRoot(), data + "，注册成功" + "\n" + sessionToken
                            , Snackbar.LENGTH_SHORT).show();
                    binding.userRegister.setEnabled(true);
                    ((UserLoginActivity) requireActivity()).onLoginSuccess(sessionToken,
                            UserLoginType.REGISTER);
                }

                @Override
                public void onSuccess(String data) {
                    //empty stub
                }

                @Override
                public void onError(APICore.ApiError error) {
                    Snackbar.make(binding.getRoot(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                    Logs.e(TAG, error.getMessage());
                    binding.userRegister.setEnabled(true);
                }
            });
        }
    }

}
