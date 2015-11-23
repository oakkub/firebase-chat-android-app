package com.oakkub.chat.views.widgets.recyclerview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by OaKKuB on 11/5/2015.
 */
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_THRESHOLD = 4;

    private LinearLayoutManager layoutManager;

    private int previousItemCount;
    private int page;
    private boolean isLoadMore;
    private boolean noMoreData;

    /**
     * For RecyclerView,
     * Only LinearLayoutManager and GridLayoutManager can use this scroll listener.
     */
    public InfiniteScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (noMoreData) return;

        final int totalItem = layoutManager.getItemCount();
        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (totalItem > previousItemCount) {
            // if totalItem is greater than previousItemCount = new item inserted.
            previousItemCount = totalItem;
            isLoadMore = true;
        }

        if (isLoadMore && lastVisibleItemPosition >= (totalItem - VISIBLE_THRESHOLD)) {
            isLoadMore = false;
            onLoadMore(++page);
        }
    }

    public void noMoreData() {
        noMoreData = true;
    }

    public abstract void onLoadMore(int page);
}
