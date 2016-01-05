package com.oakkub.chat.views.adapters;

import android.os.Bundle;
import android.util.SparseBooleanArray;

import com.oakkub.chat.managers.SparseBooleanArrayParcelableWrapper;

/**
 * Created by OaKKuB on 12/22/2015.
 */
public abstract class RecyclerViewMultipleSelectionAdapter<I> extends RecyclerViewAdapter<I> {

    private static final String STATE_SELECTED_ITEMS = "state:selectedItems";

    private SparseBooleanArray selectedItems;

    public RecyclerViewMultipleSelectionAdapter() {
        super();
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public void onSaveInstanceState(String key, Bundle outState) {
        super.onSaveInstanceState(key, outState);
        outState.putParcelable(STATE_SELECTED_ITEMS, new SparseBooleanArrayParcelableWrapper(selectedItems));
    }

    @Override
    public void onRestoreInstanceState(String key, Bundle savedInstanceState) {
        super.onRestoreInstanceState(key, savedInstanceState);
        selectedItems = savedInstanceState.getParcelable(STATE_SELECTED_ITEMS);
    }

    public boolean isSelected(int position) {
        return selectedItems.get(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void toggleSelection(int position) {
        if (isSelected(position)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        int[] totalSelectedItemToBeCleared = getSelectedItems();
        selectedItems.clear();

        for (int itemPositionToBeCleared : totalSelectedItemToBeCleared) {
            notifyItemChanged(itemPositionToBeCleared);
        }
    }

    public int[] getSelectedItems() {
        final int size = selectedItems.size();

        int[] totalSelectedPosition = new int[size];
        for (int i = 0; i < size; i++) {
            totalSelectedPosition[i] = selectedItems.keyAt(i);
        }

        return totalSelectedPosition;
    }

}
