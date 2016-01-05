package com.oakkub.chat.views.widgets.recyclerview;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 11/5/2015.
 */
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_THRESHOLD = 4;

    @State
    int previousItemCount;
    @State
    int page;
    @State
    boolean isLoadMore;
    @State
    boolean noMoreData;

    private LinearLayoutManager layoutManager;

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

        int totalItem = layoutManager.getItemCount();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

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

    public void onSaveInstanceState(Bundle outState) {
        Icepick.saveInstanceState(this, outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    public void setLoadMore(boolean isLoadMore) {
        this.isLoadMore = isLoadMore;
    }

    public void noMoreData() {
        noMoreData = true;
    }

    public boolean isNoMoreData() {
        return noMoreData;
    }

    public abstract void onLoadMore(int page);
}
