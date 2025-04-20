package net.hearnsoft.tcm.ui.interfaces;

import android.content.Context;

import net.hearnsoft.tcm.ui.widgets.ProfileItem;

import java.util.List;

// 组件接口
public interface IProfileComponent {
    // 获取组件优先级，决定显示顺序
    int getPriority();
    // 获取组件提供的列表项
    List<ProfileItem> getItems(Context context);
    // 组件初始化
    void initialize();
    // 组件销毁时调用
    void onDestroy();
    // 编辑模式切换回调
    void onEditModeChanged(boolean isEditMode);
}
