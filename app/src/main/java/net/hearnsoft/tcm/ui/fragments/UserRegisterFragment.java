package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.SettingsPrefUtils;
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
        binding.username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    binding.username.setError(null);
                }
            }
            // 其他回调方法保持空实现
        });

        binding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    binding.password.setError(null);
                }
            }
            // 其他回调方法保持空实现
        });
        binding.userRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        boolean hasError = false;

        // 清除旧错误提示
        binding.username.setError(null);
        binding.password.setError(null);

        // 双重空值检查
        if (username.isEmpty() && password.isEmpty()) {
            binding.username.setError(getString(R.string.error_username_empty));
            binding.password.setError(getString(R.string.error_password_empty));
            hasError = true;
        } else {
            // 单独字段检查
            if (username.isEmpty()) {
                binding.username.setError(getString(R.string.error_username_empty));
                binding.username.requestFocus();
                hasError = true;
            }
            if (password.isEmpty()) {
                binding.password.setError(getString(R.string.error_password_empty));
                if (!hasError) { // 避免覆盖前一个焦点
                    binding.password.requestFocus();
                }
                hasError = true;
            }
        }
        if (!hasError) {
            binding.userRegister.setEnabled(false);
            userAPI.register(username, password, new APICore.SessionTokenCallback<String>() {
                @Override
                public void onSuccess(String data, String sessionToken) {
                    Snackbar.make(binding.getRoot(),
                            data + "," + getString(R.string.toast_user_register_succ),
                            Snackbar.LENGTH_SHORT).show();
                    SettingsPrefUtils.getInstance(requireActivity())
                            .writeStringSettings(Constants.KEY_USER_ID, username);
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
