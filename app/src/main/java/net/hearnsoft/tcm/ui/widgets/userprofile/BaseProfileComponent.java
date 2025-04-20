package net.hearnsoft.tcm.ui.widgets.userprofile;

import android.content.Context;

import net.hearnsoft.tcm.ui.interfaces.IProfileComponent;
import net.hearnsoft.tcm.ui.model.UserViewModel;

// 基础组件抽象类
public abstract class BaseProfileComponent implements IProfileComponent {
    protected Context context;
    protected UserViewModel userViewModel;
    protected boolean isEditMode = false;

    public BaseProfileComponent(Context context, UserViewModel userViewModel) {
        this.context = context;
        this.userViewModel = userViewModel;
    }

    @Override
    public void onEditModeChanged(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    public void initialize() {
        // 基类中的默认实现
    }

    @Override
    public void onDestroy() {
        // 基类中的默认实现
    }
}
