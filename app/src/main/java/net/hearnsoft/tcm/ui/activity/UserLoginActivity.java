package net.hearnsoft.tcm.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.application.usecase.UserAuthenticationType;
import net.hearnsoft.tcm.databinding.ActivityUserLoginBinding;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.ui.adapter.AppViewPagerAdapter;
import net.hearnsoft.tcm.ui.fragments.UserLoginFragment;
import net.hearnsoft.tcm.ui.fragments.UserRegisterFragment;
import net.hearnsoft.tcm.ui.model.AuthStateViewModel;
import net.hearnsoft.tcm.utils.UserLoginPortal;
import net.hearnsoft.tcm.utils.ViewModelUtils;

public class UserLoginActivity extends AppCompatActivity implements UserLoginPortal {

    private static final String TAG = UserLoginActivity.class.getSimpleName();

    public static UserLoginActivity loginActivity;

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
        loginActivity = this;
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

    public void onLoginSuccess(UserAuthenticationType loginType) {
        // 保存 session token
        // SettingsPrefUtils.getInstance(this).writeStringSettings(Constants.KEY_USER_TOKEN, sessionToken);

        // 展示Toast
        switch (loginType) {
            case REGISTER:
                Toast.makeText(this, R.string.toast_user_register_succ, Toast.LENGTH_SHORT).show();
                break;
            case LOGIN:
                Toast.makeText(this, R.string.toast_user_login_succ, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        // 发送广播通知 AccountFragment 刷新
        Intent intent = new Intent(Constants.ACTION_REFRESH_USER_PROFILE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // 重置ViewModel处理状态
        AuthStateViewModel authStateViewModel = ViewModelUtils.getViewModel(this, AuthStateViewModel.class);
        authStateViewModel.finishHandling401();

        // 关闭登录活动
        finish();
    }
}
