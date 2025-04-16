package net.hearnsoft.tcm.ui.widgets;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.hearnsoft.tcm.R;


public abstract class BaseSheetDialog extends BottomSheetDialogFragment {

    private BottomSheetBehavior<FrameLayout> behavior;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.getDelegate()
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
            params.width = getWidth();

            behavior = BottomSheetBehavior.from(bottomSheet);
            if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    private int getWidth() {
        int width = 1080;
        if (getContext() != null) {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Point point = new Point();
            if (windowManager != null) {
                windowManager.getDefaultDisplay().getSize(point);
                if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    width = point.x;
                } else if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    width = point.y;
                }
            }
        }
        return width;
    }

    protected void setRecyclerViewDynamicHeight(RecyclerView recyclerView) {
        if (recyclerView == null || getContext() == null || recyclerView.getAdapter() == null) return;

        // 获取项目数量
        int itemCount = recyclerView.getAdapter().getItemCount();

        // 获取项目高度（从dimens.xml中获取）
        int itemHeight = (int) getResources().getDimension(R.dimen.playlist_item_height);

        // 获取屏幕高度
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point displaySize = new Point();
        windowManager.getDefaultDisplay().getSize(displaySize);
        int screenHeight = displaySize.y;

        // 计算最大高度（屏幕高度的2/3）
        int maxHeight = (int)(screenHeight * 2.0 / 3.0);

        // 设置最小高度
        int minHeight = (int) getResources().getDimension(R.dimen.min_playlist_height);

        // 计算期望高度
        int desiredHeight = itemCount * itemHeight;

        // 确保高度在最小值和最大值之间
        int finalHeight = Math.min(maxHeight, Math.max(minHeight, desiredHeight));

        ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
        layoutParams.height = finalHeight;
        recyclerView.setLayoutParams(layoutParams);
    }

    public BottomSheetBehavior<FrameLayout> getBehavior() {
        return behavior;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}
