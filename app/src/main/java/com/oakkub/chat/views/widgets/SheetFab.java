package com.oakkub.chat.views.widgets;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.gordonwong.materialsheetfab.AnimatedFab;
import com.oakkub.chat.managers.BundleSavedState;

/**
 * Created by OaKKuB on 1/29/2016.
 */
public class SheetFab extends FloatingActionButton implements AnimatedFab {

    private static final String STATE_IS_SHOWN = "state:isShown";

    public SheetFab(Context context) {
        super(context);
    }

    public SheetFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SheetFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        BundleSavedState savedState = new BundleSavedState(superState);

        Bundle bundle = new Bundle();
        bundle.putBoolean(STATE_IS_SHOWN, isShown());
        savedState.bundle = bundle;

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BundleSavedState savedState = (BundleSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.bundle.getBoolean(STATE_IS_SHOWN)) {
            show();
        } else {
            hide();
        }
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void show(float translationX, float translationY) {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        super.hide();
    }
}
