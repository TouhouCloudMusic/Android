package net.hearnsoft.tcm.ui.widgets;

import android.content.Context;
import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.ui.activity.UserLoginActivity;

import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class UserCardView extends LinearLayout {

    private static final String TAG = UserCardView.class.getSimpleName();

    private MaterialCardView userCard;
    private ShapeableImageView userAvatar;
    private ImageView userBackground;
    private TextView userName;
    private TextView userDescription;

    public UserCardView(Context context) {
        this(context, null);
    }

    public UserCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.user_card, this);
        userCard = findViewById(R.id.user_card);
        userAvatar = findViewById(R.id.user_avatar);
        userBackground = findViewById(R.id.user_card_background);
        userName = findViewById(R.id.username_text);
        userDescription = findViewById(R.id.user_description);

        // 初始化默认的信息
        setUserName("");
        setUserDescription("");
        setUserAvatarFromRes(R.drawable.ic_account_circle);
        setUserCardBackgroundFromRes(R.drawable.test_res2);
    }

    /**
     * 设置UserCard的点击监听器
     * @param listener 点击监听器
     */
    public void setOnUserCardClickListener(OnClickListener listener) {
        userCard.setOnClickListener(listener);
    }

    /**
     * 设置UserCard展示的用户名
     * @param userNameText 用户名文本
     */
    public void setUserName(String userNameText) {
        if (TextUtils.isEmpty(userNameText)) {
            userName.setText("Default User");
            return;
        }
        userName.setText(userNameText);
    }

    /**
     * 设置UserCard展示的用户简介
     * @param userDescriptionText 用户简介文本
     */
    public void setUserDescription(String userDescriptionText) {
        if (TextUtils.isEmpty(userDescriptionText)) {
            userDescription.setText("I'm a touhou fans~");
            return;
        }
        userDescription.setText(userDescriptionText);
    }

    /**
     * 从本地Resource加载用户头像，一般用于测试，上线时建议使用
     * {@link UserCardView#setUserAvatarFromUrl(String)} 方法。
     * @param avatarResId 用户头像的resId
     */
    public void setUserAvatarFromRes(int avatarResId) {
        if (Objects.isNull(avatarResId)) {
            userAvatar.setImageResource(R.drawable.ic_account_circle);
            Log.e(TAG, "avatarRes is null!");
            return;
        }
        userAvatar.setImageResource(avatarResId);
    }

    /**
     * 从网络URL加载用户头像，上线时建议使用本方法。
     * @param avatarResUrl 用户头像的资源URL
     */
    public void setUserAvatarFromUrl(String avatarResUrl) {
        if (TextUtils.isEmpty(avatarResUrl)) {
            userAvatar.setImageResource(R.drawable.ic_account_circle);
            Log.e(TAG, "avatarResUrl is empty!");
            return;
        }
        Glide.with(this)
                .load(avatarResUrl)
                .placeholder(R.drawable.ic_account_circle)
                .into(userAvatar);
    }

    /**
     * 从本地Resource加载用户卡片背景，一般用于测试，上线时建议使用
     * {@link UserCardView#setUserCardBackgroundFromUrl(String)} 方法。
     * @param backgroundResId 用户卡片背景的resId
     */
    public void setUserCardBackgroundFromRes(int backgroundResId) {
        if (Objects.isNull(backgroundResId)) {
            setBlurCardFromRes(R.drawable.test_res2);
            Log.e(TAG, "backgroundRes is null!");
            return;
        }
        setBlurCardFromRes(backgroundResId);
    }

    /**
     * 从网络URL加载用户卡片背景，上线时建议使用本方法。
     * @param backgroundUrl 用户卡片背景的资源URL
     */
    public void setUserCardBackgroundFromUrl(String backgroundUrl) {
        if (TextUtils.isEmpty(backgroundUrl)) {
            setBlurCardFromRes(R.drawable.test_res2);
            Log.e(TAG, "backgroundUrl is empty!");
            return;
        }
        setBlurCardFromUrl(backgroundUrl);
    }


    /**
     * 传入本地图片res资源设置高斯模糊化
     * @param resId 传入的资源ID
     */
    private void setBlurCardFromRes(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            userBackground.setImageResource(resId);
            userBackground.setRenderEffect(RenderEffect.createBlurEffect(
                            100F,
                            100F,
                            Shader.TileMode.CLAMP)
                    );
            return;
        }
        // Android 11- 模糊方法
        Glide.with(this)
                .load(resId)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(40,3)))
                .into(userBackground);
    }

    /**
     * 传入网络图片URL资源设置高斯模糊化
     * @param imageUrl 传入的图片URL
     */
    private void setBlurCardFromUrl(String imageUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.test_res2)
                    .into(userBackground);
            userBackground.setRenderEffect(RenderEffect.createBlurEffect(
                    100F,
                    100F,
                    Shader.TileMode.CLAMP)
            );
            return;
        }
        // Android 11- 模糊方法
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.test_res2)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(40,3)))
                .into(userBackground);
    }

}
