package net.hearnsoft.tcm.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhpan.bannerview.BaseBannerAdapter;
import com.zhpan.bannerview.BaseViewHolder;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.beans.BannerDataBean;

public class AppBannerAdapter extends BaseBannerAdapter<BannerDataBean> {

    @Override
    protected void bindData(BaseViewHolder<BannerDataBean> holder, BannerDataBean data, int position, int pageSize) {
        ImageView bannerView = holder.findViewById(R.id.banner_image);
        //如果ImgResUrl是空
        if (TextUtils.isEmpty(data.getImgResUrl())) {
            Glide.with(bannerView)
                    .load(data.getImgRes())
                    .into(bannerView);
        } else {
            Glide.with(bannerView)
                    .load(data.getImgResUrl())
                    .into(bannerView);
        }
        if (!TextUtils.isEmpty(data.getImgDesc())) {
            bannerView.setContentDescription(data.getImgDesc());
        }
        holder.setOnClickListener(R.id.banner_image, v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = data.getImgContentUrl();
            if (url != null){
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
