package com.oakkub.chat.views.widgets.spinner;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 12/19/2015.
 */
public class MySpinner extends AppCompatSpinner {

    public MySpinner(Context context) {
        super(context);
    }

    public MySpinner(Context context, int mode) {
        super(context, mode);
    }

    public MySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable parcelable = super.onSaveInstanceState();
        BundleSavedState savedState = new BundleSavedState(parcelable);

        Bundle bundle = new Bundle();
        bundle.putInt("visibility", getVisibility());

        return savedState;
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        BundleSavedState savedState = (BundleSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setVisibility(savedState.bundle.getInt("visibility"));
    }

}
