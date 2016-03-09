package com.oakkub.chat.views.widgets.recyclerview;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 11/5/2015.
 */
public abstract class RecyclerViewInfiniteScrollListener extends RecyclerView.OnScrollListener {

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
     * Only LinearLayoutManager and GridLayoutManager can use this scroll listener.
     */
    public RecyclerViewInfiniteScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (noMoreData) return;

        int totalItem = layoutManager.getItemCount();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        checkPreviousItemCount(totalItem);
        loadItem(totalItem, lastVisibleItemPosition);
    }

    private void checkPreviousItemCount(int totalItem) {
        if (totalItem > previousItemCount) {
            // if totalItem is greater than previousItemCount then there is new item.
            previousItemCount = totalItem;
            isLoadMore = true;
        }
    }

    private void loadItem(int totalItem, int lastVisibleItemPosition) {
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

    public void reset() {
        noMoreData = false;
        isLoadMore = false;
        page = 0;
        previousItemCount = 0;
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

    public void setPage(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public abstract void onLoadMore(int page);
}
