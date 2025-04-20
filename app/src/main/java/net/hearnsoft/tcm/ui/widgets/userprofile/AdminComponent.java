package net.hearnsoft.tcm.ui.widgets.userprofile;

import android.content.Context;
import android.content.Intent;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.domain.model.user.UserRole;
import net.hearnsoft.tcm.ui.activity.AdminModeActivity;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;

import java.util.ArrayList;
import java.util.List;

public class AdminComponent extends BaseProfileComponent {

    public AdminComponent(Context context, UserViewModel userViewModel) {
        super(context, userViewModel);
    }

    @Override
    public int getPriority() {
        return 500; // 中等优先级
    }

    @Override
    public List<ProfileItem> getItems(Context context) {
        List<ProfileItem> items = new ArrayList<>();
        UserProfileModel data = userViewModel.getUserProfile().getValue();

        if (data != null && isAdminUser(data.getRoles())) {
            items.add(new ProfileItem(
                ProfileItem.TYPE_PREFERENCE_ITEM,
                context.getString(R.string.profile_title_admin_mode),
                null,
                true,
                item -> {
                    Intent intent = new Intent(context, AdminModeActivity.class);
                    context.startActivity(intent);
                }
            ));
        }

        return items;
    }

    private boolean isAdminUser(List<UserRole> rolesList) {
        if (rolesList == null || rolesList.isEmpty()) {
            return false;
        }

        for (UserRole role : rolesList) {
            if (role != null && role.getIndex() == 1) { // 假设1是管理员角色
                return true;
            }
        }
        return false;
    }
}
