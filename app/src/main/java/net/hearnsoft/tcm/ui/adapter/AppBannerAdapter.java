package net.hearnsoft.tcm.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhpan.bannerview.BaseBannerAdapter;
import com.zhpan.bannerview.BaseViewHolder;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.domain.model.banner.BannerImage;

public class AppBannerAdapter<T extends BannerImage> extends BaseBannerAdapter<T> {
    @Override
    protected void bindData(BaseViewHolder<T> holder, T data,
                            int position, int pageSize) {
        ImageView bannerView = holder.findViewById(R.id.banner_image);
        //如果ImgResUrl是空
        if (TextUtils.isEmpty(data.getResourceUrl())) {
            Glide.with(bannerView)
                .load(data.getId())
                .into(bannerView);
        } else {
            Glide.with(bannerView)
                .load(data.getResourceUrl())
                .into(bannerView);
        }
        if (!TextUtils.isEmpty(data.getAltText())) {
            bannerView.setContentDescription(data.getAltText());
        }
        holder.setOnClickListener(R.id.banner_image, v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = data.getContentUrl();
            if (url != null) {
                intent.setData(Uri.parse(url));
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.banner_item;
    }
}
