package net.hearnsoft.tcm.ui.widgets.userprofile;

import android.content.Context;

import net.hearnsoft.tcm.ui.interfaces.IProfileComponent;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileComponentManager {
    private final List<IProfileComponent> components = new ArrayList<>();
    private final Context context;

    public ProfileComponentManager(Context context) {
        this.context = context;
    }

    public void addComponent(IProfileComponent component) {
        components.add(component);
        component.initialize();
    }

    public void removeComponent(IProfileComponent component) {
        component.onDestroy();
        components.remove(component);
    }

    public void clearComponents() {
        for (IProfileComponent component : components) {
            component.onDestroy();
        }
        components.clear();
    }

    public List<ProfileItem> updateEditMode(boolean isEditMode) {
        // 首先更新所有组件的编辑模式
        for (IProfileComponent component : components) {
            component.onEditModeChanged(isEditMode);
        }

        // 然后返回更新后的所有项目
        return getAllItems();
    }

    public List<ProfileItem> getAllItems() {
        List<ProfileItem> allItems = new ArrayList<>();

        // 按优先级排序组件
        List<IProfileComponent> sortedComponents = new ArrayList<>(components);
        Collections.sort(sortedComponents, (c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority()));

        // 收集所有组件的项目
        for (IProfileComponent component : sortedComponents) {
            allItems.addAll(component.getItems(context));
        }

        return allItems;
    }
}
