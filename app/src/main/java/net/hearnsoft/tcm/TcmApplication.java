package net.hearnsoft.tcm;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.hearnsoft.tcm.ui.activity.MainActivity;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;
import net.hearnsoft.tcm.ui.model.AuthStateViewModel;
import net.hearnsoft.tcm.utils.Constants;
import net.hearnsoft.tcm.utils.Logs;
import net.hearnsoft.tcm.utils.ViewModelUtils;


public class TcmApplication extends Application {

    private static final String TAG = TcmApplication.class.getSimpleName();

    // 注册广播接收器
    private BroadcastReceiver unauthorizedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(TcmApplication.this, R.string.toast_profile_err_session_expired, Toast.LENGTH_SHORT).show();
            AuthStateViewModel viewModel =
                    ViewModelUtils.getViewModel(TcmApplication.this, AuthStateViewModel.class);

            if (viewModel.getIsHandling401().getValue() != Boolean.TRUE) {
                // 执行跳转逻辑
                if (UserLoginActivity.loginActivity != null) {
                    UserLoginActivity.loginActivity.finish();
                }
                Intent loginIntent = new Intent(context, UserLoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.d(TAG, "onApplicationInit:");
        if (unauthorizedReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(unauthorizedReceiver,
                    new IntentFilter(Constants.ACTION_UNAUTHORIZED));
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(unauthorizedReceiver);
    }
}
