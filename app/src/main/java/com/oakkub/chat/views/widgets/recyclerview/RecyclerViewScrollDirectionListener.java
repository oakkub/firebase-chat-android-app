package com.oakkub.chat.views.widgets.recyclerview;


import android.support.v7.widget.RecyclerView;

/**
 * Created by OaKKuB on 2/1/2016.
 */
public abstract class RecyclerViewScrollDirectionListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy > 0) onScrollDown();
        else if (dy < 0) onScrollUp();
    }

    public abstract void onScrollUp();
    public abstract void onScrollDown();
}
