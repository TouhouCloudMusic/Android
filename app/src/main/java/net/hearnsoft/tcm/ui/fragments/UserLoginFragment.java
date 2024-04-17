package net.hearnsoft.tcm.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.FragmentUserLoginBinding;
import net.hearnsoft.tcm.utils.UserLoginPortal;

public class UserLoginFragment extends Fragment {

    private static final String TAG = UserLoginFragment.class.getSimpleName();
    private FragmentUserLoginBinding binding;
    private UserLoginPortal portal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentUserLoginBinding.inflate(inflater, container, false);
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
        binding.userRegister.setOnClickListener(v -> {
            portal.switchPages(1);
            portal.setActivityTitle(requireContext().getString(R.string.activity_user_register_title));
        });
    }

}
