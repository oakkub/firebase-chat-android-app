package com.oakkub.chat.views.adapters;

import android.os.Bundle;
import android.util.SparseIntArray;

import com.oakkub.chat.managers.SparseIntArrayParcelableWrapper;

/**
 * Created by OaKKuB on 12/22/2015.
 */
public abstract class RecyclerViewMultipleSelectionAdapter<I> extends RecyclerViewAdapter<I> {

    private static final String STATE_SELECTED_ITEMS = "state:selectedItems";

    private SparseIntArray selectedItems = new SparseIntArray();

    @Override
    public void onSaveInstanceState(String key, Bundle outState) {
        super.onSaveInstanceState(key, outState);
        outState.putParcelable(STATE_SELECTED_ITEMS, new SparseIntArrayParcelableWrapper(selectedItems));
    }

    @Override
    public void onRestoreInstanceState(String key, Bundle savedInstanceState) {
        super.onRestoreInstanceState(key, savedInstanceState);
        selectedItems = savedInstanceState.getParcelable(STATE_SELECTED_ITEMS);
    }

    public boolean isSelected(int position) {
        return selectedItems.get(position, -1) != -1;
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void toggleSelection(int position, int hashCode) {
        setSelection(position, hashCode);
        notifyItemChanged(position);
    }

    public void setSelection(int position, int hashCode) {
        if (isSelected(hashCode)) {
            selectedItems.delete(hashCode);
        } else {
            selectedItems.put(hashCode, position);
        }
    }

    public void clearSelection() {
        /*int[] selectedItemsPosition = getSelectedItemsKey();
        selectedItems.clear();

        for (int itemPosition : selectedItemsPosition) {
            notifyItemChanged(itemPosition);
        }*/
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int[] getSelectedItemsKey() {
        int size = selectedItems.size();

        int[] totalSelectedPosition = new int[size];
        for (int i = 0; i < size; i++) {
            totalSelectedPosition[i] = selectedItems.keyAt(i);
        }

        return totalSelectedPosition;
    }

}
