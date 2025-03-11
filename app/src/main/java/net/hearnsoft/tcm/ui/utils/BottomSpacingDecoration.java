package net.hearnsoft.tcm.ui.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.hearnsoft.tcm.ui.interfaces.OnScrollStateChangeListener;

public class BottomSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int bottomSpacing;
    private final OnScrollStateChangeListener scrollListener;

    public BottomSpacingDecoration(OnScrollStateChangeListener listener) {
        this.scrollListener = listener;
        this.bottomSpacing = scrollListener.getBottomNavHeight();
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == parent.getAdapter().getItemCount() - 1) {
            // 只给最后一个item添加底部间距
            outRect.bottom = bottomSpacing;
        }
    }
}