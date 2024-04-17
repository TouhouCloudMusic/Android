package net.hearnsoft.tcm.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import net.hearnsoft.tcm.databinding.ActivityUserLoginBinding;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.fragments.UserLoginFragment;
import net.hearnsoft.tcm.ui.fragments.UserRegisterFragment;
import net.hearnsoft.tcm.utils.UserLoginPortal;

public class UserLoginActivity extends AppCompatActivity implements UserLoginPortal {

    private static final String TAG = UserLoginActivity.class.getSimpleName();

    private ActivityUserLoginBinding binding;
    private AppViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new AppViewPagerAdapter(this);
        initUserLoginPage();
        setSupportActionBar(binding.topAppbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initUserLoginPage() {
        binding.mainView.setAdapter(adapter);
        adapter.addFragment(new UserLoginFragment());
        adapter.addFragment(new UserRegisterFragment());
        binding.mainView.setCurrentItem(0, true);
        binding.mainView.setUserInputEnabled(false);
    }

    @Override
    public void switchPages(int pages) {
        if (!TextUtils.isEmpty(String.valueOf(pages))) {
            binding.mainView.setCurrentItem(pages, true);
        }
    }

    @Override
    public void setActivityTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            binding.topAppbar.setTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
