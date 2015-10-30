package com.oakkub.chat.views.widgets.recyclerview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by OaKKuB on 11/5/2015.
 */
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_THRESHOLD = 5;

    private LinearLayoutManager layoutManager;

    private int previousItemCount;
    private int page;
    private boolean isLoadMore;

    /**
     * For recyclerview,
     * Only LinearLayoutManager and GridLayoutManager can use this scroll listener.
     *
     * @param layoutManager only LinearLayoutManager and GridLayoutManager.
     */
    public InfiniteScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        final int itemCount = layoutManager.getItemCount();
        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (previousItemCount < itemCount) {
            previousItemCount = itemCount;
            isLoadMore = true;
        }

        if (isLoadMore && lastVisibleItemPosition <= (itemCount - VISIBLE_THRESHOLD)) {
            // load more
            isLoadMore = false;
            onLoadMore(++page);
        }
    }

    public abstract void onLoadMore(int page);
}