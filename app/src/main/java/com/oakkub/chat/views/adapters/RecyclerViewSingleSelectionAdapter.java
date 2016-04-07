package com.oakkub.chat.views.adapters;

import android.os.Bundle;

import com.oakkub.chat.managers.SparseIntArrayParcelable;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 12/22/2015.
 */
public abstract class RecyclerViewSingleSelectionAdapter<I> extends RecyclerViewAdapter<I> {

    private static final String TAG = RecyclerViewSingleSelectionAdapter.class.getSimpleName();
    private static final String PACKAGE_NAME = RecyclerViewSingleSelectionAdapter.class.getPackage().getName();

    @State
    SparseIntArrayParcelable selectedItems = new SparseIntArrayParcelable();

    @Override
    public void onSaveInstanceState(String key, Bundle outState) {
        super.onSaveInstanceState(key, outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onRestoreInstanceState(String key, Bundle savedInstanceState) {
        super.onRestoreInstanceState(key, savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    public boolean isSelected(int hashCode) {
        return selectedItems.get(hashCode, -1) != -1;
    }

    public void toggleSelection(int position, int hashCode) {
        setSelection(hashCode, position);
    }

    public void setSelection(int hashCode, int position) {
        if (!isSelected(hashCode)) {
            // clear previous selected item
            if (isSelected(selectedItems.keyAt(0))) {
                notifyItemChanged(selectedItems.valueAt(0));
            }
            selectedItems.clear();

            selectedItems.put(hashCode, position);
            notifyItemChanged(position);
        }
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemValue() {
        int size = selectedItems.size();

        int[] totalSelectedPosition = new int[size];
        for (int i = 0; i < size; i++) {
            totalSelectedPosition[i] = selectedItems.valueAt(i);
        }

        return totalSelectedPosition[0];
    }

}
