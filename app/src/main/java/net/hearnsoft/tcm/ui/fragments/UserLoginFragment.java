package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentUserLoginBinding;
import net.hearnsoft.tcm.domain.model.user.UserAuthenticationType;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.UserLoginPortal;
import net.hearnsoft.tcm.utils.ViewModelUtils;

import net.hearnsoft.thcdb_sdk.model.AuthCredential;

public class UserLoginFragment extends Fragment {
    private static final String TAG = UserLoginFragment.class.getSimpleName();
    private FragmentUserLoginBinding binding;
    private UserLoginPortal portal;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        userViewModel = ViewModelUtils.getViewModel(requireActivity(), UserViewModel.class);
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
        binding.userLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
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
            binding.userLogin.setEnabled(false);

            // 执行登录操作
            AuthCredential auth = new AuthCredential(username, password);
            userViewModel.login(auth)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        onSuccess();
                    } else {
                        onError(result.getError());
                    }
                });
        }
    }

    private void onError(String msg) {
        // 确保在UI线程中执行
        if (isAdded() && !isRemoving()) {
            requireActivity().runOnUiThread(() -> {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
                Logs.e(TAG, msg);
                binding.userLogin.setEnabled(true);
            });
        }
    }

    public void onSuccess() {
        if (isAdded() && !isRemoving()) {
            // 确保在UI线程中执行
            requireActivity().runOnUiThread(() -> {
                Snackbar.make(
                    binding.getRoot(),
                    "Ok ," + getString(R.string.toast_user_login_succ),
                    Snackbar.LENGTH_SHORT
                ).show();
                binding.userLogin.setEnabled(true);
                ((UserLoginActivity) requireActivity()).onLoginSuccess(UserAuthenticationType.LOGIN);
            });
        }
    }

}
