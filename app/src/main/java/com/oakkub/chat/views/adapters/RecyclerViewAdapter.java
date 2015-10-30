package com.oakkub.chat.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.magicwerk.brownies.collections.GapList;

import java.util.List;

/**
 * Created by OaKKuB on 10/28/2015.
 */
public abstract class RecyclerViewAdapter<I, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {

    protected static final int LOAD_MORE_TYPE = 100;
    protected static final int NO_INTERNET_TYPE = 101;

    protected GapList<I> items = new GapList<>();
    protected boolean loadMore;
    protected boolean noInternet;

    public View inflateLayout(ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    public void add(I item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void add(int position, I item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void addFirst(I item) {
        items.addFirst(item);
        notifyItemInserted(0);
    }

    public void addAll(List<I> list) {
        items.addAll(list);
        notifyItemRangeInserted(items.size() - 1, list.size());
    }

    public boolean replace(I item) {
        final int index = findPosition(item);

        if (index >= 0) {
            if (items.remove(index) != null) {

                items.add(index, item);
                notifyItemChanged(index);

                return true;
            }
            return false;
        }
        return false;
    }

    public I remove(I item) {
        final int index = findPosition(item);

        if (index >= 0) {
            I removedItem = items.remove(index);

            if (removedItem != null) {
                notifyItemRemoved(index);
                return removedItem;
            }
            return null;
        }

        return null;
    }

    public void removeAll() {
        final int itemCount = items.size();

        if (itemCount > 0) {
            items.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    public I getItem(int position) {
        return items.get(position);
    }

    public int findPosition(I item) {
        return items.indexOf(item);
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
    public int getItemCount() {
        return items.size();
    }
}
