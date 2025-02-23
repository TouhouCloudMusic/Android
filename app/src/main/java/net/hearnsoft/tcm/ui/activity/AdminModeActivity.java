package net.hearnsoft.tcm.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ActivityAdminModeBinding;
import net.hearnsoft.tcm.ui.fragments.apitest.MainAdminFragment;

public class AdminModeActivity extends AppCompatActivity {
    private ActivityAdminModeBinding binding;

    private String userToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userToken = getIntent().getStringExtra("user_token");
        binding = ActivityAdminModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            MainAdminFragment fragment = new MainAdminFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_main_view, fragment)
                    .commit();
            fragment.setFragmentChangeListener(this::replaceFragment);
        }


        Toast.makeText(this, userToken, Toast.LENGTH_SHORT).show();
    }

    private void replaceFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_main_view, newFragment)
                .addToBackStack(null) // 添加回退栈
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 处理返回按钮的导航
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 处理返回按钮的导航
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return true;
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
