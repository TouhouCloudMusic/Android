package net.hearnsoft.tcm.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.hearnsoft.tcm.R;
import net.hearnsoft.uiwidgets.activity.MActivity;

import java.util.Objects;

public abstract class BaseActivity extends MActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置主题
        this.setTheme(R.style.Theme_TouhouCloudMusic);
        // 设置ActionBar可见
        this.setMActionBarVisible(false);

        // 小米 沉浸式状态栏
        if (Objects.equals(Build.BRAND, "Xiaomi") || Objects.equals(Build.BRAND, "Redmi")) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);       //设置沉浸式状态栏，在MIUI系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);   //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。
        }
    }
}
