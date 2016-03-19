package com.oakkub.chat.views.adapters;

import android.os.Bundle;

import com.oakkub.chat.managers.MySparseBooleanArray;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 12/22/2015.
 */
public abstract class RecyclerViewSingleSelectionAdapter<I> extends RecyclerViewAdapter<I> {

    private static final String TAG = RecyclerViewSingleSelectionAdapter.class.getSimpleName();
    private static final String PACKAGE_NAME = RecyclerViewSingleSelectionAdapter.class.getPackage().getName();

    @State
    MySparseBooleanArray selectedItems = new MySparseBooleanArray();

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

    public boolean isSelected(int position) {
        return selectedItems.get(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void toggleSelection(int position, int hashCode) {
        setSelection(hashCode);
        notifyItemChanged(position);
    }

    public void setSelection(int hashCode) {
        if (isSelected(hashCode)) {
            selectedItems.delete(hashCode);
        } else {
            selectedItems.put(hashCode, true);
        }
    }

    public void clearSelection() {
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
