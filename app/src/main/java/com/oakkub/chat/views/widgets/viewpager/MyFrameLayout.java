package com.oakkub.chat.views.widgets.viewpager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 1/13/2016.
 */
public class MyFrameLayout extends FrameLayout {

    public MyFrameLayout(Context context) {
        super(context);
    }

    public MyFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MyFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        BundleSavedState savedState = new BundleSavedState(parcelable);

        Bundle bundle = new Bundle();
        bundle.putInt("visibility", getVisibility());
        savedState.bundle = bundle;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BundleSavedState savedState = (BundleSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        switch (savedState.bundle.getInt("visibility")) {
            case VISIBLE:
                setVisibility(VISIBLE);
                break;
            case INVISIBLE:
                setVisibility(INVISIBLE);
                break;
            case GONE:
                setVisibility(GONE);
                break;
        }
    }

}
