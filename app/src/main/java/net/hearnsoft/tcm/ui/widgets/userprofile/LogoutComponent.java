package net.hearnsoft.tcm.ui.widgets.userprofile;

import android.content.Context;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;

import java.util.ArrayList;
import java.util.List;

public class LogoutComponent extends BaseProfileComponent {

    public interface LogoutCallback {
        void onLogoutRequested();
    }

    private final LogoutCallback callback;

    public LogoutComponent(Context context, UserViewModel userViewModel, LogoutCallback callback) {
        super(context, userViewModel);
        this.callback = callback;
    }

    @Override
    public int getPriority() {
        return 100; // 最低优先级，显示在最底部
    }

    @Override
    public List<ProfileItem> getItems(Context context) {
        List<ProfileItem> items = new ArrayList<>();

        items.add(new ProfileItem(
            ProfileItem.TYPE_BUTTON,
            context.getString(R.string.profile_title_logout),
            "",
            true,
            item -> callback.onLogoutRequested()
        ));

        return items;
    }
}
