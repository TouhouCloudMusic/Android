package net.hearnsoft.tcm.ui.widgets.userprofile;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.ui.model.UserViewModel;
import net.hearnsoft.tcm.ui.widgets.ProfileItem;

import java.util.ArrayList;
import java.util.List;

public class AvatarComponent extends BaseProfileComponent {
    private ActivityResultLauncher<PickVisualMediaRequest> pickAvatar;

    public interface AvatarCallback {
        void onPickImage(Uri uri);
    }

    private final AvatarCallback callback;

    public AvatarComponent(
        Context context, UserViewModel userViewModel,
        ActivityResultLauncher<PickVisualMediaRequest> pickAvatar,
        AvatarCallback callback) {
        super(context, userViewModel);
        this.pickAvatar = pickAvatar;
        this.callback = callback;
    }

    @Override
    public int getPriority() {
        return 1000; // 最高优先级，显示在顶部
    }

    @Override
    public List<ProfileItem> getItems(Context context) {
        List<ProfileItem> items = new ArrayList<>();
        UserProfileModel data = userViewModel.getUserProfile().getValue();

        if (data != null) {
            items.add(new ProfileItem(
                ProfileItem.TYPE_AVATAR,
                context.getString(R.string.profile_title_avatar),
                getAvatarUrl(data.getAvatarUrl()),
                true,
                item -> openImagePicker()
            ));
        }

        return items;
    }

    private void openImagePicker() {
        pickAvatar.launch(new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build());
    }

    private String getAvatarUrl(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        return Constants.API_STATIC_IMAGE_URL + fileName;
    }
}
