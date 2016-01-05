package com.oakkub.chat.views.adapters;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oakkub.chat.R;
import com.oakkub.chat.views.adapters.viewholders.ProgressBarHolder;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 10/28/2015.
 */
public abstract class RecyclerViewAdapter<I> extends RecyclerView.Adapter {

    protected static final int LOAD_MORE_TYPE = 100;
    protected static final int NO_INTERNET_TYPE = 101;

    protected ArrayList<I> items;

    @State
    boolean loadMore;
    @State
    boolean noInternet;

    public RecyclerViewAdapter() {
        items = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onSaveInstanceState(String key, Bundle outState) {
        if (items == null) return;

        outState.putParcelable(key, Parcels.wrap(items));
        Icepick.saveInstanceState(this, outState);
    }

    public void onRestoreInstanceState(String key, Bundle savedInstanceState) {
        Parcelable parceledItems = savedInstanceState.getParcelable(key);
        items = Parcels.unwrap(parceledItems);

        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    public View inflateLayout(ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void addLast(I item) {
        items.add(item);
        notifyItemInserted(getLastPosition());
    }

    public void add(int position, I item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void addFirst(I item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    public void addLastAll(List<I> list) {
        final int startPosition = getItemCount();

        items.addAll(list);
        notifyItemRangeInserted(startPosition, list.size());
    }

    public void addFirstAll(List<I> list) {
        items.addAll(0, list);
        notifyItemRangeInserted(0, list.size());
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

    public void removeLast() {
        int lastPosition = getLastPosition();

        items.remove(lastPosition);
        notifyItemRemoved(lastPosition);
    }

    public void removeAll() {
        final int itemCount = items.size();

        if (itemCount > 0) {
            items.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    public int moveItem(I item, int destinationPosition) {
        final int index = findPosition(item);

        if (index != destinationPosition) {
            I itemToBeMoved = items.remove(index);
            items.add(destinationPosition, itemToBeMoved);

            notifyItemMoved(index, destinationPosition);
        }

        return index;
    }

    public void moveItemAndReplace(I item, int destinationPosition) {
        final int index = moveItem(item, destinationPosition);

        if (index >= 0) {
            replace(item);
        }
    }

    public I getItem(int position) {
        if (position >= getItemCount()) return null;
        else return items.get(position);
    }

    public int findPosition(I item) {
        return items.indexOf(item);
    }

    public I getLastItem() {
        return getItem(getLastPosition());
    }

    public I getFirstItem() {
        if (isEmpty()) return null;
        else return items.get(0);
    }

    public int getLastPosition() {
        return getItemCount() - 1;
    }

    public void addFooterProgressBar() {
        noInternet = false;
        loadMore = true;
        addLast(null);
    }

    public void addFooterNoInternet() {
        noInternet = true;
        loadMore = false;
        addLast(null);
    }

    public void removeFooter() {
        reset();
        removeLast();
    }

    private void reset() {
        noInternet = false;
        loadMore = false;
    }

    public ProgressBarHolder getProgressBarHolder(ViewGroup parent) {
        View view = inflateLayout(parent, R.layout.load_more_progress_bar);
        return new ProgressBarHolder(view);
    }

}
