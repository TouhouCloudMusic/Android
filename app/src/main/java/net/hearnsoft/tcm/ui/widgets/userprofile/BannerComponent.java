package net.hearnsoft.tcm.ui.widgets.userprofile;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;

import java.util.ArrayList;
import java.util.List;

public class BannerComponent extends BaseProfileComponent {

    private ActivityResultLauncher<PickVisualMediaRequest> pickBanner;
    private final BannerCallback callback;

    public interface BannerCallback {
        void onPickBannerImage(Uri uri);
    }

    public BannerComponent(Context context, UserViewModel userViewModel,
        ActivityResultLauncher<PickVisualMediaRequest> pickBanner,
        BannerCallback callback) {
        super(context, userViewModel);
        this.pickBanner = pickBanner;
        this.callback = callback;
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public List<ProfileItem> getItems(Context context) {
        List<ProfileItem> items = new ArrayList<>();
        UserProfileModel data = userViewModel.getUserProfile().getValue();

        if (data != null) {
            items.add(
                new ProfileItem(
                    ProfileItem.TYPE_BANNER,
                    context.getString(R.string.profile_title_banner_content),
                    getBannerUrl(data.getBannerUrl()),
                    true,
                    item -> pickBanner.launch(new PickVisualMediaRequest.Builder().build())
                )
            );
        }

        return items;
    }

    private String getBannerUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return Constants.API_STATIC_IMAGE_URL + fileName;
    }
}
