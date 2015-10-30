package com.oakkub.chat.managers;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.oakkub.chat.R;

/**
 * Created by OaKKuB on 10/26/2015.
 */
public class GridAutoFitLayoutManager extends GridLayoutManager {

    private int columnWidth;
    private boolean columnWidthChanged = true;

    public GridAutoFitLayoutManager(Context context, int columnWidth) {
        super(context, 1);
        setColumnWidth(checkedColumnWidth(context, columnWidth));
    }

    public GridAutoFitLayoutManager(Context context, int columnWidth, int orientation, boolean reverseLayout) {
        super(context, 1, orientation, reverseLayout);
        setColumnWidth(checkedColumnWidth(context, columnWidth));
    }

    private int checkedColumnWidth(Context context, int columnWidth) {

        if (columnWidth <= 0) {
            columnWidth = (int) context.getResources().getDimension(R.dimen.spacing_larger);
        }
        return columnWidth;
    }

    private void setColumnWidth(int newColumnWidth) {

        if (newColumnWidth > 0 && newColumnWidth != columnWidth) {
            columnWidth = newColumnWidth;
            columnWidthChanged = true;
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (columnWidthChanged && columnWidth > 0) {

            final int totalSpace = getTotalSpace();
            Log.e("TOTAL SPACE", String.valueOf(totalSpace));
            final int spanCount = Math.max(1, totalSpace / columnWidth);
            Log.e("SPAN COUNT", String.format("%d / %d = %d", totalSpace, columnWidth, spanCount));
            setSpanCount(spanCount);

            columnWidthChanged = false;
        }

        super.onLayoutChildren(recycler, state);
    }

    private int getTotalSpace() {
        Log.e("TOTALSPACE VERTICAL", String.format("Width:%d, PaddingLeft:%d, PaddingRight:%d", getWidth(), getPaddingLeft(), getPaddingRight()));
        Log.e("TOTALSPACE HORIZONTAL", String.format("Width:%d, PaddingTop:%d, PaddingBottom:%d", getWidth(), getPaddingTop(), getPaddingBottom()));
        if (getOrientation() == VERTICAL) return getWidth() - getPaddingLeft() - getPaddingRight();
        else return getHeight() - getPaddingTop() - getPaddingBottom();
    }
}
