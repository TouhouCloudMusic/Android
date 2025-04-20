package net.hearnsoft.tcm.ui.widgets.userprofile;

import android.content.Context;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.domain.model.user.UserRole;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;
import net.hearnsoft.tcm.utils.OffsetDateTimeFormater;

import java.util.ArrayList;
import java.util.List;

public class UserInfoComponent extends BaseProfileComponent {

    public interface UserInfoCallback {
        void onEditName(String currentName);
    }

    private final UserInfoCallback callback;

    public UserInfoComponent(Context context, UserViewModel userViewModel, UserInfoCallback callback) {
        super(context, userViewModel);
        this.callback = callback;
    }

    @Override
    public int getPriority() {
        return 800; // 次优先级
    }

    @Override
    public List<ProfileItem> getItems(Context context) {
        List<ProfileItem> items = new ArrayList<>();
        UserProfileModel data = userViewModel.getUserProfile().getValue();

        if (data != null) {
            // 用户名
            items.add(new ProfileItem(
                ProfileItem.TYPE_INFO,
                context.getString(R.string.profile_title_username),
                data.getName(),
                true,
                item -> callback.onEditName(item.getContent())
            ));

            // 最后登录时间
            items.add(new ProfileItem(
                ProfileItem.TYPE_INFO,
                context.getString(R.string.profile_title_last_login),
                OffsetDateTimeFormater.format(data.getLastLogin(),
                    context.getString(R.string.date_format_str))
            ));

            // 用户权限
            items.add(new ProfileItem(
                ProfileItem.TYPE_INFO,
                context.getString(R.string.profile_title_user_permission),
                getUserRoleString(data.getRoles())
            ));
        }

        return items;
    }

    private String getUserRoleString(List<UserRole> rolesList) {
        if (rolesList == null || rolesList.isEmpty()) {
            return context.getString(R.string.profile_label_unset);
        }

        String[] roleNames = context.getResources().getStringArray(R.array.user_roles);
        StringBuilder sb = new StringBuilder();

        for (UserRole role : rolesList) {
            if (role != null && role.getIndex() > 0 && role.getIndex() <= roleNames.length) {
                sb.append(roleNames[role.getIndex() - 1]).append(", ");
            }
        }

        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) :
            context.getString(R.string.profile_label_unset);
    }
}
