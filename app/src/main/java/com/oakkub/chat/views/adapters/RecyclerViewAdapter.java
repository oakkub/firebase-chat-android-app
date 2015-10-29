package com.oakkub.chat.views.adapters;

import android.support.v7.widget.RecyclerView;

import org.magicwerk.brownies.collections.GapList;

import java.util.List;

/**
 * Created by OaKKuB on 10/28/2015.
 */
public abstract class RecyclerViewAdapter<I> extends RecyclerView.Adapter {

    protected static final int LOAD_MORE_TYPE = 100;
    protected static final int NO_INTERNET_TYPE = 101;

    protected GapList<I> adapterList = new GapList<>();
    protected boolean loadMore;
    protected boolean noInternet;

    public void add(I item) {
        adapterList.add(item);
        notifyItemInserted(adapterList.size() - 1);
    }

    public void addFirst(I item) {
        adapterList.addFirst(item);
        notifyItemInserted(0);
    }

    public void addAll(List<I> list) {
        adapterList.addAll(list);
        notifyItemRangeInserted(adapterList.size() - 1, list.size());
    }

    public I remove(I item) {
        final int index = adapterList.indexOf(item);

        if (index >= 0) {
            I removedItem = adapterList.remove(index);

            if (removedItem != null) {
                notifyItemRemoved(index);
                return removedItem;
            }
            return null;
        }

        return null;
    }

    public void removeAll() {
        final int itemCount = adapterList.size();

        adapterList.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    public void addProgressBar() {
        add(null);
        loadMore = true;
    }

    public void addNoInternet() {
        add(null);
        noInternet = true;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return adapterList.size();
    }
}
