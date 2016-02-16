package com.oakkub.chat.views.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 10/14/2015.
 */
public class MyProgressBar extends ProgressBar {

    public MyProgressBar(Context context) {
        super(context);
    }

    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable parcelable = super.onSaveInstanceState();
        BundleSavedState savedState = new BundleSavedState(parcelable);

        Bundle bundle = new Bundle();
        bundle.putInt("visibility", getVisibility());

        savedState.bundle = bundle;

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
