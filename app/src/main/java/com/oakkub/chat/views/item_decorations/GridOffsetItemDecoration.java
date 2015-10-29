package com.oakkub.chat.views.item_decorations;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by OaKKuB on 9/29/2015.
 */
public class GridOffsetItemDecoration extends RecyclerView.ItemDecoration {

    private final int spaceBetweenCell;
    private boolean allSpace;

    public GridOffsetItemDecoration(int spaceBetweenCell, boolean allSpace) {
        this.spaceBetweenCell = spaceBetweenCell / 2;
        this.allSpace = allSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final int viewPosition = parent.getChildAdapterPosition(view);
        final int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();

        if (allSpace) {
            outRect.left = spaceBetweenCell;
            outRect.top = spaceBetweenCell;
            outRect.right = viewPosition % spanCount == spanCount - 1 ? spaceBetweenCell : 0;
            outRect.bottom = viewPosition == parent.getChildCount() - 1 ? spaceBetweenCell : 0;
        } else {
            outRect.left = viewPosition % spanCount == 0 ? 0 : spaceBetweenCell;
            outRect.top = viewPosition < spanCount ? 0 : spaceBetweenCell * 2;
            outRect.right = viewPosition % spanCount == spanCount - 1 ? 0 : spaceBetweenCell;
        }
    }
}
