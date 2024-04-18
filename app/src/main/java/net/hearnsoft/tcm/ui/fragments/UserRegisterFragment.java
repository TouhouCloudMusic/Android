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

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentUserRegisterBinding;
import net.hearnsoft.tcm.utils.UserLoginPortal;


public class UserRegisterFragment extends Fragment {

    private static final String TAG = UserRegisterFragment.class.getSimpleName();
    private FragmentUserRegisterBinding binding;
    private UserLoginPortal portal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            portal = (UserLoginPortal) requireActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
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
    }

}
