package com.oakkub.chat.views.widgets.spinner;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 12/19/2015.
 */
public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener {

    @State
    int previousPosition = -1;

    private OnSpinnerClickListener onSpinnerClickListener;

    public SpinnerInteractionListener(OnSpinnerClickListener onSpinnerClickListener) {
        this.onSpinnerClickListener = onSpinnerClickListener;
    }

    public void onSaveInstanceState(Bundle outState) {
        Icepick.saveInstanceState(this, outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (previousPosition == position) return;

        onSpinnerClickListener.onSpinnerItemClick(parent, view, position, id);
        previousPosition = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface OnSpinnerClickListener {
        void onSpinnerItemClick(AdapterView<?> parent, View view, int position, long id);
    }
}
